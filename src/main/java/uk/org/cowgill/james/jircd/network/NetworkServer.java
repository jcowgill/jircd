package uk.org.cowgill.james.jircd.network;

import java.io.File;

import org.apache.mina.core.service.IoAcceptor;

import uk.org.cowgill.james.jircd.Server;

/**
 * Server which uses listeners to listen for remote connections
 * 
 * @author James
 */
class NetworkServer extends Server
{
	private IoAcceptor[] listeners;
	
	public NetworkServer(File configFile)
	{
		super(configFile);
	}

	@Override
	protected void rehashed()
	{
		//Update server listeners if running
		if(Server.getServer() == this)
		{
			createListeners();
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
		//Create listeners
		if(!createListeners())
		{
			//Oh noes
			return;
		}
		
		//Wait for io events
	}
	
	/**
	 * Creates listeners for the server
	 * @return false if there are no running listeners
	 */
	private boolean createListeners()
	{
		//
	}
}
