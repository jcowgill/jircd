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
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import uk.org.cowgill.james.jircd.util.CaseInsensitiveHashMap;
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
	public final static String VERSION = "jircd0.1";
	
	/**
	 * The longer IRC Server version
	 */
	public final static String VERSION_STR = "JIRC Server 0.1";
	
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
	private int stopType = 0;
	
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
	public Server(File configFile)
	{
		//Set global server
		if(globalServer == null)
		{
			globalServer = this;
		}
		
		//Set config file
		this.configFile = configFile;
	}
	
	/**
	 * Rehashes the server configuration
	 * 
	 * Returns false if the reload fails. In this case the server configuration is unmodified
	 * 
	 * @return True if the rehash suceeded with no errors
	 */
	public boolean rehash()
	{
		try
		{
			//TODO Should rehashing be done in a separate thread?
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
			logger.error("Config error: " + e.toString());
		}
		catch (FileNotFoundException e)
		{
			logger.error("Error reading config file: " + e.toString());
		}
		catch (IOException e)
		{
			logger.error("Error reading config file: " + e.toString());
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
		if(stopType == 0)
		{
			stopType = 1;
			stopReason = reason;
			stopRequested();
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
		if(stopType == 0)
		{
			stopType = 2;
			stopReason = reason;
			stopRequested();
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
		if(stopType != 0)
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
			return false;
		}
		
		//Startup modules
		moduleMan.serverStartupEvent();
		
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
		return stopType == 2;
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
			}
		}
		
		return false;
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
	 * Returns the number of unregisterd clients connected to the server
	 * 
	 * @return the number of unregisterd clients connected to the server
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
	 * Event occurs after a stop or restart has been requested
	 */
	protected void stopRequested()
	{
	}
	
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
		switch(stopType)
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
		@Override
		protected void append(LoggingEvent event)
		{
			String msg = Message.newStringFromServer("NOTICE ");
			String logMsg = ":" + layout.format(event);
			
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
