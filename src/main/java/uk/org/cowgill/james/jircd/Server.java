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
package uk.org.cowgill.james.jircd;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;

import uk.org.cowgill.james.jircd.util.CaseInsensitiveHashMap;
import uk.org.cowgill.james.jircd.util.ColourConsoleAppender;
import uk.org.cowgill.james.jircd.util.MutableInteger;

/**
 * The main IRC Server class
 *
 * Controls the server and contains the main global data
 *
 * @author James
 */
public abstract class Server
{
	/**
	 * The IRC Server version
	 */
	public final static String VERSION = "jircd1.1-snapshot";

	/**
	 * The longer IRC Server version
	 */
	public final static String VERSION_STR = "JIRC Server 1.1-SNAPSHOT";

	/**
	 * Currently running server
	 */
	private static Server globalServer;

	/**
	 * Class logger
	 */
	private final static Logger logger = Logger.getLogger(Server.class);

	/**
	 * Location of the server configuration file
	 */
	private final File configFile;

	/**
	 * Server configuration field
	 */
	private Config config;

	/**
	 * The server's module manager
	 */
	private final ModuleManager moduleMan = new ModuleManager();

	/**
	 * The server's supported modes and limits
	 */
	private final ServerISupport iSupport = new ServerISupport();

	/**
	 * The type of stop the server should shutdown by
	 *
	 * 0 = No shutdown
	 * 1 = Stop
	 * 2 = Restart
	 */
	private AtomicInteger stopType = new AtomicInteger();

	/**
	 * Reason for stop / restart (shown to all users and logged)
	 */
	private String stopReason = null;

	/**
	 * Set of all clients connected to the server
	 */
	protected Set<Client> clients = new HashSet<Client>();

	/**
	 * Map of all registered clients on the server stored by nickname
	 */
	Map<String, Client> clientsByNick = new CaseInsensitiveHashMap<Client>();

	/**
	 * Map of all channels on the server (all begin with #)
	 */
	Map<String, Channel> channels = new CaseInsensitiveHashMap<Channel>();

	/**
	 * Map of all ips and number of uses
	 */
	Map<String, MutableInteger> ipClones = new HashMap<String, MutableInteger>();

	/**
	 * Contains the set of IRC operators
	 */
	Set<Client> operators = new HashSet<Client>();

	/**
	 * Peek number of clients the server has served
	 */
	int peekClients = 0;

	/**
	 * The time this server was created
	 */
	public final Date creationTime = new Date();

	/**
	 * The time the server was created in string format
	 */
	public final String creationTimeStr = DATE_FORMAT.format(creationTime);

	/**
	 * The format dates are formatted in
	 */
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE dd MMM yyyy HH:mm:ss zzz");

	//--------------------------------------------

	/**
	 * Creates a new IRC Server
	 *
	 * @param configFile Configuration file location
	 */
	protected Server(File configFile)
	{
		//Set config file
		this.configFile = configFile;
	}

	/**
	 * Rehashes the server configuration
	 *
	 * <p>Returns false if the reload fails. In this case the server configuration is unmodified
	 *
	 * @return True if the rehash succeeded with no errors
	 */
	public boolean rehash()
	{
		try
		{
			//Open config file
			InputStream stream = new BufferedInputStream(new FileInputStream(configFile));

			//Parse configuration file
			Config config = Config.parse(stream, this.config);

			//Store config
			this.config = config;

			//Notify rehash
			moduleMan.serverRehashEvent();
			return true;
		}
		catch(ConfigException e)
		{
			logger.error("Config error: " + e.toString(), e);
		}
		catch (FileNotFoundException e)
		{
			logger.error("Error reading config file: " + e.toString(), e);
		}
		catch (IOException e)
		{
			logger.error("Error reading config file: " + e.toString(), e);
		}

		return false;
	}

	/**
	 * Requests a server stop
	 *
	 * The stop will occur when the current command is finished.
	 *
	 * @param reason The reason for the stop (this should include the user who initiated it)
	 */
	public void requestStop(String reason)
	{
		//Check and update stop type
		if(stopType.compareAndSet(0, 1))
		{
			stopReason = reason;
		}
	}

	/**
	 * Requests a server restart
	 *
	 * The stop will occur when the current command is finished
	 *
	 * @param reason The reason for the restart (this should include the user who initiated it)
	 */
	public void requestRestart(String reason)
	{
		//Check and update stop type
		if(stopType.compareAndSet(0, 2))
		{
			stopReason = reason;
		}
	}

	/**
	 * This is the blocking call which actually runs the server
	 * @return true if a restart was requested, false if a stop was requested
	 */
	public boolean run()
	{
		//Check if running or run
		if(globalServer != null)
		{
			throw new UnsupportedOperationException("A server is already running");
		}
		if(stopType.get() != 0)
		{
			throw new UnsupportedOperationException("run() has already been called on this server");
		}
		globalServer = this;

		//Server notice
		logger.info(VERSION_STR + "  By James Cowgill");

		//Rehash if not done already
		if(config == null && !rehash())
		{
			//Config error
			logger.fatal("Config error - exiting");
			return false;
		}

		//Startup modules
		if(!moduleMan.serverStartupEvent())
		{
			//Module error
			logger.fatal("Module load failure - exiting");
			return false;
		}

		//Add operators to root logger
		OperLogger opLogger = new OperLogger();
		Logger.getRootLogger().addAppender(opLogger);

		//Run server
		runServer();

		//Remove operators logger
		Logger.getRootLogger().removeAppender(opLogger);

		//Stop modules
		moduleMan.serverStopEvent();

		//Return reason
		globalServer = null;
		return stopType.get() == 2;
	}

	/**
	 * Increments the number of ip usages
	 *
	 * @param ip ip address to increment
	 * @param maxClones maximum number of clones to allow
	 * @return false if the maximum number of clones has been reached
	 */
	boolean ipClonesIncrement(String ip, int maxClones)
	{
		MutableInteger clones = ipClones.get(ip);

		if(clones == null)
		{
			//Check stupid clones
			if(maxClones == 0)
			{
				return false;
			}
			else
			{
				ipClones.put(ip, new MutableInteger(1));
				return true;
			}
		}
		else
		{
			//Increment
			if(clones.intValue() >= maxClones)
			{
				return false;
			}
			else
			{
				clones.increment();
				return true;
			}
		}
	}

	/**
	 * Decrements the number of ip usages
	 *
	 * @param ip ip address to decrement
	 */
	void ipClonesDecrement(String ip)
	{
		MutableInteger clones = ipClones.get(ip);

		if(clones != null)
		{
			clones.decrement();

			if(clones.intValue() == 0)
			{
				ipClones.remove(ip);
			}
		}
	}

	/**
	 * Returns the peek number of clients to connect to the server
	 *
	 * @return the peek number of clients to connect to the server
	 */
	public int getClientCountPeek()
	{
		return peekClients;
	}

	/**
	 * Returns the number of unregistered clients connected to the server
	 *
	 * @return the number of unregistered clients connected to the server
	 */
	public int getUnregisteredClients()
	{
		return clientsByNick.size() - clients.size();
	}

	/**
	 * Returns the number of registered clients connected to the server
	 *
	 * @return the number of registered clients connected to the server
	 */
	public int getClientCount()
	{
		return clientsByNick.size();
	}

	/**
	 * Gets a client from the specified nickname
	 *
	 * @param nick nickname to search for
	 * @return client requested or null if the nick does not exist
	 */
	public Client getClient(String nick)
	{
		return clientsByNick.get(nick);
	}

	/**
	 * Gets an unmodifiable collection of the clients registered on this server
	 *
	 * @return an unmodifiable collection of the clients registered on this server
	 */
	public Collection<Client> getRegisteredClients()
	{
		return Collections.unmodifiableCollection(clientsByNick.values());
	}

	/**
	 * Gets a channel with the specified name
	 *
	 * @param name the name of the channel with a leading #
	 * @return the channel
	 */
	public Channel getChannel(String name)
	{
		return channels.get(name);
	}

	/**
	 * Gets an unmodifiable collection of all the channels on the server
	 *
	 * <p>The collection updates itself as channels are created and destroyed
	 *
	 * @return unmodifiable collection of all the channels
	 */
	public Collection<Channel> getChannels()
	{
		return Collections.unmodifiableCollection(channels.values());
	}

	/**
	 * Returns the server configuration filename
	 *
	 * @return configuration filename
	 */
	public File getConfigFile()
	{
		return configFile;
	}

	/**
	 * Returns the server configuration
	 *
	 * @return the server configuration
	 */
	public Config getConfig()
	{
		return config;
	}

	/**
	 * Returns the module manager for this server
	 *
	 * @return the module manager for this server
	 */
	public ModuleManager getModuleManager()
	{
		return moduleMan;
	}

	/**
	 * Returns an unmodifiable set of the IRC operators on the server
	 *
	 * @return the server's irc operators
	 */
	public Set<Client> getIRCOperators()
	{
		return Collections.unmodifiableSet(operators);
	}

	/**
	 * Returns the isupport object for this server
	 *
	 * @return the isupport object for this server
	 */
	public ServerISupport getISupport()
	{
		return iSupport;
	}

	/**
	 * Returns the currently running server
	 * @return the the currently running server
	 */
	public static Server getServer()
	{
		return globalServer;
	}

	/**
	 * Ensures that the log4j system is setup even if no log4j.xml / properties file is found
	 */
	public static void ensureLoggerSetup()
	{
		//Get root
		Logger root = Logger.getRootLogger();

		//Find appenders
		if(!root.getAllAppenders().hasMoreElements())
		{
			//Setup defaults
			PatternLayout layout = new PatternLayout("%-5p - %m%n");

			if(System.getProperty("os.name").toLowerCase().contains("win") || System.console() == null)
			{
				root.addAppender(new ConsoleAppender(layout));
			}
			else
			{
				root.addAppender(new ColourConsoleAppender(layout));
			}

			//Display warning
			logger.warn("Using basic console logging since log4j config file cannot be found");
		}
	}

	/**
	 * Utility to get the server config filename from the given command-line options
	 *
	 * @param args Arguments to check
	 * @return The server config file or null on error (reported)
	 */
	public static File getFileFromArgs(String[] args)
	{
		//Config file on command line?
		String configFileName;
		if(args.length != 0)
		{
			configFileName = args[0];
		}
		else
		{
			configFileName = "ircd.conf";
		}

		//Attempt to open config file
		File configFile;

		//Try to read from current directory first, then from JAR directory
		configFile = new File(configFileName);
		if(!configFile.canRead())
		{
			//Try jar path
			URL jarFile = Server.class.getProtectionDomain().getCodeSource().getLocation();
			if(jarFile.getProtocol().equals("file"))
			{
				configFile = new File(jarFile.getPath() + "/" + configFileName);
				if(!configFile.canRead())
				{
					//Cannot find config file
					ensureLoggerSetup();
					logger.error("Cannot open configuration file \"" + configFileName + "\"");
					return null;
				}
			}
		}

		return configFile;
	}

	//-------------------------------
	// Abstract methods and abstract helpers

	/**
	 * Event occurs after the server has been rehashed
	 */
	protected abstract void rehashed();

	/**
	 * Called to run the server
	 */
	protected abstract void runServer();

	/**
	 * Checks weather the server should be stopped
	 *
	 * <p>If it should be shutdown, it logs the event, notifies all clients and terminates their connections.
	 *
	 * @return true if the server should be stopped
	 */
	protected boolean checkAndNotifyStop()
	{
		String stopTypeStr;

		//Check stop type
		switch(stopType.get())
		{
		case 1:
			stopTypeStr = "shutdown";
			break;

		case 2:
			stopTypeStr = "restart";
			break;

		default:
			return false;
		}

		//Log the stop
		String stopMsg = "The server is about to " + stopTypeStr + ". Reason:" + stopReason;
		logger.warn(stopMsg);

		//Report stop
		Message msg = Message.newMessageFromServer("NOTICE");
		msg.appendParam("*");
		msg.appendParam(stopMsg);

		Client.sendTo(this.clients, msg);

		//Close all clients
		String quitMsg = "Server " + stopTypeStr;
		for(Client client : this.clients)
		{
			client.closeForShutdown(quitMsg);
		}

		return true;
	}

	/**
	 * Sends logging events to IRC operators
	 *
	 * @author James
	 */
	private class OperLogger extends AppenderSkeleton
	{
		public OperLogger()
		{
			setLayout(new PatternLayout("%-5p - %m%n"));
		}

		@Override
		protected void append(LoggingEvent event)
		{
			String msg = Message.newStringFromServer("NOTICE ");
			String logMsg = " :" + layout.format(event);

			//Go though everyone in the operator cache
			for(Client client : operators)
			{
				if(client.hasPermission(Permissions.seeServerNotices))
				{
					client.send(msg + client.id.nick + logMsg);
				}
			}
		}

		@Override
		public void close()
		{
		}

		@Override
		public boolean requiresLayout()
		{
			return false;
		}
	}
}
