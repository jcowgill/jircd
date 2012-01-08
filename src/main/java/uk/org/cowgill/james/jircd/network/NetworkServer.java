/*
   Copyright 2011 James Cowgill

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package uk.org.cowgill.james.jircd.network;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.log4j.Logger;

import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Config.Ban;
import uk.org.cowgill.james.jircd.IRCMask;
import uk.org.cowgill.james.jircd.ModuleLoadException;
import uk.org.cowgill.james.jircd.RegistrationFlags;
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
	private static final Logger logger = Logger.getLogger(NetworkServer.class);
	
	/**
	 * Server event selector (all events are handled by this)
	 */
	private Selector eventSelector;
	
	/**
	 * Listening channels
	 */
	private Set<ServerSocketChannel> listeners = new HashSet<ServerSocketChannel>();
	
	/**
	 * Time of the last ping check
	 */
	private long lastPingCheck;
	
	public NetworkServer(File configFile)
	{
		super(configFile);
	}
	
	/**
	 * Checks whether a client is ip banned
	 * 
	 * @param client client to check
	 * @return true if banned (informed)
	 */
	private boolean handleIPBans(SocketChannel channel) throws IOException
	{
		String ipAddress = NetworkClient.getIpAddress(channel);
		
		//Process everything in ip ban list
		for(Ban ipBan : getConfig().banIP)
		{
			//Compare
			if(IRCMask.wildcardCompare(ipAddress, ipBan.mask))
			{
				//Banned
				channel.close();
				return true;
			}
		}
		
		return false;
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
		try
		{
			// Register network commands
			getModuleManager().registerCommand(new Ping());
			getModuleManager().registerCommand(new Pong());
		}
		catch(ModuleLoadException e)
		{
			logger.error("Only the network subsystem can implement the PING and PONG commands", e);
			return;
		}
		
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
		
		// Create host resolver
		HostResolver resolver = new HostResolver(eventSelector);
		
		//Process IO Events
		int retryError = 0;
		
		for(;;)
		{
			try
			{
				//Select anything to do
				eventSelector.select(1000);
				
				//Check for host resolver requests
				NetworkClient client = resolver.drainOneFinished();
				
				while(client != null)
				{
					if(!client.isClosed() && !client.isRegistered())
					{
						//Set host bit
						client.setRegistrationFlag(RegistrationFlags.HostSet);
						
						//Check registered event
						client.registeredEvent();
					}
					
					//Next client
					client = resolver.drainOneFinished();
				}
				
				//Check all selected keys
				Iterator<SelectionKey> keyIter = eventSelector.selectedKeys().iterator();
				SelectionKey key;
				
				while(keyIter.hasNext())
				{
					//Get key
					key = keyIter.next();
					keyIter.remove();
					
					if(key.isValid())
					{
						//Check accept
						if(key.isAcceptable())
						{
							//Get listener
							ServerSocketChannel channel = (ServerSocketChannel) key.channel();
							
							//Create new client from channel
							SocketChannel sockChannel = channel.accept();
							
							if(handleIPBans(sockChannel))
							{
								//Ignore
								continue;
							}
							
							client = new NetworkClient(sockChannel);
							
							//Resolver host
							resolver.sumbitRequest(client);
							
							//Register channel and attach client to it
							try
							{
								SelectionKey clientKey = sockChannel.register(eventSelector, OP_READ);
								clientKey.attach(client);
							}
							catch(ClosedChannelException e)
							{
								logger.error("Accepted socket suddenly closed (WTF)", e);
							}
						}
						else if(key.isReadable())
						{
							//Read event occured
							((NetworkClient) key.attachment()).processReadEvent();
						}
					}
				}
				
				//Perform ping checks
				if(System.currentTimeMillis() - 1000 > lastPingCheck)
				{
					//Also process flood queue here
					FloodTimer.processFloodQueue();
					
					//Iterate over all clients and ping if nessesary
					for(Client locClient : clients)
					{
						if(locClient instanceof NetworkClient)
						{
							((NetworkClient) locClient).pingCheckEvent();
						}
					}
					
					lastPingCheck = System.currentTimeMillis();
					Client.processCloseQueue();
				}

				//Check shutdown condition
				if(checkAndNotifyStop())
				{
					break;
				}
				
				retryError = 0;
			}
			catch (Exception e)
			{
				//Pretty bad error
				logger.error("Exception in i/o loop", e);
				
				//Check for 5 errors in a row
				retryError++;
				if(retryError >= 5)
				{
					logger.fatal("5 loop errors in a row - exiting");
					break;
				}
			}
		}
		
		//Close all listeners
		for(ServerSocketChannel listener : listeners)
		{
			try
			{
				listener.close();
			}
			catch (IOException e)
			{	//Ignore errors on close
			}
		}
		
		//Close selector
		try
		{
			eventSelector.close();
		}
		catch (IOException e)
		{	//Ignore errors on close
		}
		
		//Shutdown resolver
		resolver.shutdown();
		
		//Wipe variables
		listeners = null;
		eventSelector = null;
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
			if(ports.isEmpty())
			{
				logger.warn("No listening ports in configuration file");
			}
			
			logger.fatal("Failed to bind to any ports");
			return false;
		}
		else
		{
			return true;
		}
	}
}
