package uk.org.cowgill.james.jircd;

import java.io.File;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import org.apache.mina.core.service.IoAcceptor;

/**
 * The main IRC Server class
 * 
 * Controls the server and contains the main global data
 * 
 * @author James
 */
public final class Server
{
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
	
	private IoAcceptor[] listeners;
	
	//--------------------------------------------
	
	/**
	 * Creates a new IRC Server
	 * 
	 * @param configFile Configuration file location
	 */
	public Server(File configFile)
	{
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
		//Rehash if not done already
		if(config == null && !rehash())
		{
			//Config error
			return false;
		}
		
		//
		listeners[0].
		
		return false;
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
}
