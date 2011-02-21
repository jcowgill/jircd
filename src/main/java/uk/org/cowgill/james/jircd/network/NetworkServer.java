package uk.org.cowgill.james.jircd.network;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Set;

import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.cowgill.james.jircd.ConnectionClass;
import uk.org.cowgill.james.jircd.Server;

/**
 * Server which uses listeners to listen for remote connections
 * 
 * @author James
 */
class NetworkServer extends Server
{	
	private Logger logger = LoggerFactory.getLogger(NetworkServer.class);
	
	/**
	 * Listener used for all incoming connections
	 */
	private SocketAcceptor listener;
	
	public NetworkServer(File configFile)
	{
		super(configFile);
		
		//Create and setup listener
		listener = new NioSocketAcceptor(1);
		
		//Setup constant listener options
		listener.setCloseOnDeactivation(false);
			//Clients are not disconnected when listener is closed
		
		listener.setReuseAddress(true);
			//Allows ports to be reused instantly on rehash and restart
		
		listener.getFilterChain().addLast("message",
				new ProtocolCodecFilter(new MessageCodecFactory()));
			//Message decoder / encoder filter
		
		listener.getSessionConfig().setReceiveBufferSize(NetworkClient.RECEIVE_SIZE);
		listener.getSessionConfig().setSendBufferSize(NetworkClient.START_SENDQ);
		listener.getSessionConfig().setMaxReadBufferSize(NetworkClient.START_READQ);
			//Initial buffer sizes
		
		listener.getSessionConfig().setReaderIdleTime(NetworkClient.START_TIMEOUT);
			//Initial ping timeout

		listener.setHandler(new NetworkEventHandler());
			//Set event handler
	}

	@Override
	protected void rehashed()
	{
		//Update server listener if running
		if(Server.getServer() == this)
		{
			setupPorts();
		}
	}

	@Override
	protected void stopRequested()
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void runServer()
	{
		//Bind all listeners
		if(!setupPorts())
		{
			return;
		}
		
		//Wait for io events
	}
	
	/**
	 * Binds to the ports specified in the config file
	 * 
	 * @return true if ports have been bound, false if no ports could be bound
	 */
	private boolean setupPorts()
	{
		Set<SocketAddress> currentPorts = listener.getLocalAddresses();
		Set<InetSocketAddress> newPorts = new HashSet<InetSocketAddress>();

		//Create socket addresses for new ports
		for(int port : getConfig().ports)
		{
			try
			{
				newPorts.add(new InetSocketAddress(port));
			}
			catch(IllegalArgumentException e)
			{
				//Port number out of range
				logger.error("Port number {} out of range", port);
			}
		}
		
		//Unbind ports not needed
		Set<SocketAddress> currentPorts2 = new HashSet<SocketAddress>(currentPorts);
		currentPorts.removeAll(newPorts);
		
		try
		{
			listener.unbind(currentPorts);
		}
		catch(RuntimeIoException e)
		{
			logger.error("Failed to unbind from some ports during rehash");
		}
		
		//Bind new ports
		boolean noPorts = true;
		newPorts.removeAll(currentPorts2);
		
		for(InetSocketAddress address : newPorts)
		{
			try
			{
				listener.bind(address);
				noPorts = false;
			}
			catch(IOException e)
			{
				//Error binding to port
				logger.error("Could not bind to port {}", address.getPort());
			}
		}
		
		//No ports left?
		if(noPorts)
		{
			logger.error("Failed to bind to any ports - no new connections will be accepted (try restarting)");
			return false;
		}
		else
		{
			return true;
		}
	}
}
