package uk.org.cowgill.james.jircd;

import java.io.File;
import java.net.URL;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	 * Currently running server
	 */
	private static Server globalServer;

	/**
	 * Class logger
	 */
	private final static Logger logger =
		LoggerFactory.getLogger(Server.class);
	
	/**
	 * Location of the server configuration file
	 */
	public final File configFile;
	
	/**
	 * Server configuration field
	 */
	private Config config;
	
	/**
	 * The type of stop the server should shutdown by
	 * 
	 * 0 = No shutdown
	 * 1 = Stop
	 * 2 = Restart
	 */
	private volatile int stopType = 0;
	
	/**
	 * Atomic updater for stopType
	 */
	private AtomicIntegerFieldUpdater<Server> stopTypeUpdater =
		AtomicIntegerFieldUpdater.newUpdater(Server.class, "stopType");
	
	/**
	 * Reason for stop / restart (shown to all users and logged)
	 */
	private String stopReason = null;
	
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
		//
		return false;
	}
	
	/**
	 * Requests a server stop
	 * 
	 * The stop will occur when the current command is finished.
	 * This method is thread-safe.
	 * 
	 * @param reason The reason for the stop (this should include the user who initiated it)
	 */
	public void requestStop(String reason)
	{
		//Check and update stop type
		if(stopTypeUpdater.compareAndSet(this, 0, 1))
		{
			stopReason = reason;
		}
	}

	/**
	 * Requests a server restart
	 * 
	 * The stop will occur when the current command is finished
	 * This method is thread-safe.
	 * 
	 * @param reason The reason for the restart (this should include the user who initiated it)
	 */
	public void requestRestart(String reason)
	{
		//Check and update stop type
		if(stopTypeUpdater.compareAndSet(this, 0, 2))
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
		//Check if running
		if(globalServer != null)
		{
			throw new UnsupportedOperationException("A server is already running");
		}
		globalServer = this;
		
		//Server notice
		logger.info("JIRC Server 1.0  By James Cowgill");
		
		//Rehash if not done already
		if(config == null && !rehash())
		{
			//Config error
			return false;
		}
		
		//Run server
		runServer();
		
		//Return reason
		globalServer = null;
		return stopType == 2;
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
	protected abstract void stopRequested();
	
	/**
	 * Called to run the server
	 */
	protected abstract void runServer();
	
	/**
	 * Checks weather the server should be stopped
	 * 
	 * This function also logs the stop and reports it to all users
	 * 
	 * @return true if the server should be stopped
	 */
	protected boolean checkAndNotifyStop()
	{
		//Check stop
		if(stopType == 0)
		{
			return false;
		}
		
		//Report stop
		// TODO Report stop
		return true;
	}
}
