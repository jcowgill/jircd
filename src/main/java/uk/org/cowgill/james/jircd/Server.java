package uk.org.cowgill.james.jircd;

import java.io.File;

/**
 * The main IRC Server class
 * 
 * Controls the server and contains the main global data
 * 
 * @author James
 */
public final class Server
{
	final File configFile;
	
	/**
	 * Creates a new IRC Server
	 * 
	 * @param configFile Configuration file location
	 */
	Server(File configFile)
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
}
