package uk.org.cowgill.james.jircd.network;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.org.cowgill.james.jircd.Server;

/**
 * Server which uses listeners to listen for remote connections
 * 
 * The NetworkServer class contains the main network loop which dispatches requests to other parts
 *  of the frameworK
 * 
 * @author James
 */
final class NetworkServer extends Server
{	
	private Logger logger = Logger.getLogger(NetworkServer.class);
	
	/*
	 * How to do idleness
	 * ---------
	 *  - Store time a command was last read from each socket in NetworkClient
	 *  - Store last checked time in NetworkServer
	 *  -  Every time an event occurs, if idleness has not be checked for 1s
	 *      check all sockets for idleness
	 *  - The selector has a timeout of 1s sepifically for this purpose
	 *  - If noone is connected, consider not using a timeout
	 */
	
	/**
	 * Server event selector (all events are handled by this)
	 */
	private Selector eventSelector;
	
	/**
	 * Listening channels
	 */
	private Set<ServerSocketChannel> listeners = new HashSet<ServerSocketChannel>();
	
	public NetworkServer(File configFile)
	{
		super(configFile);
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
	protected void runServer()
	{
		//Server startup
		// Create selector
		try
		{
			eventSelector = Selector.open();
		}
		catch (IOException e)
		{
			logger.fatal("Failed to open selector", e);
			return;
		}
		
		// Create listeners
		if(!setupPorts())
		{
			return;
		}
		
		//Process IO Events
		for(;;)
		{
			try
			{
				//Select anything to do
				eventSelector.select();
				
				//Check shutdown condition
				if(checkAndNotifyStop())
				{
					break;
				}
				
				//Check all selected keys
				for(SelectionKey key : eventSelector.selectedKeys())
				{
					if(key.isValid())
					{
						//Check accept
						if(key.isAcceptable())
						{
							//Get listener
							ServerSocketChannel channel = (ServerSocketChannel) key.channel();
							
							//Create new client from channel
							SocketChannel sockChannel = channel.accept();
							NetworkClient client = new NetworkClient(sockChannel);
							
							//Register channel and attach client to it
							SelectionKey clientKey = sockChannel.register(eventSelector, OP_READ);
							clientKey.attach(client);
						}
						else if(key.isReadable())
						{
							//Read event occured
							((NetworkClient) key.attachment()).processReadEvent();
						}
					}
				}
				
				//Wipe keys and repeat
				eventSelector.selectedKeys().clear();
			}
			catch (RuntimeException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Binds to the ports specified in the config file
	 * 
	 * @return true if ports have been bound, false if no ports could be bound
	 */
	private boolean setupPorts()
	{		
		//Copy ports set from config
		Set<Integer> ports = new HashSet<Integer>(getConfig().ports);
		
		//Close listeners not in newPorts
		Iterator<ServerSocketChannel> channelIter = listeners.iterator();
		ServerSocketChannel channel;
		
		while(channelIter.hasNext())
		{
			channel = channelIter.next();
			
			//Check if port is in ports
			if(!ports.contains(channel.socket().getLocalPort()))
			{
				//Close channel and remove
				// We ignore any.severes while closing
				try
				{
					channel.close();
				}
				catch (IOException e)
				{
				}
				
				channelIter.remove();
			}
			else
			{
				//Remove this port from the ports set so we don't recreate it
				ports.remove(channel.socket().getLocalPort());
			}
		}
		
		//Create listeners
		for(Integer port : ports)
		{
			InetSocketAddress sockAddr;
			
			//Create address
			try
			{
				sockAddr = new InetSocketAddress(port.intValue());
			}
			catch(IllegalArgumentException e)
			{
				//Port number out of range
				logger.error("Port number " + port + " out of range");
				continue;
			}

			//Create channel and configure socket
			channel = null;
			try
			{
				channel = ServerSocketChannel.open();
				channel.configureBlocking(false);
				
				channel.socket().setReuseAddress(true);
				channel.socket().bind(sockAddr);
				
				//Register channel with event selector
				channel.register(eventSelector, OP_ACCEPT);
			}
			catch(IOException e)
			{
				//Error binding to port
				logger.error("Could not bind to port " + sockAddr.getPort(), e);
				
				//Remove channel
				if(channel != null)
				{
					//We ignore any.severes while closing
					try
					{
						channel.close();
					}
					catch(IOException f)
					{
					}
				}
				
				continue;
			}
			
			//Add channel to listeners
			listeners.add(channel);
		}
		
		//No ports left?
		if(listeners.isEmpty())
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
