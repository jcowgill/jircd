package uk.org.cowgill.james.jircd.network;

import java.io.File;

import uk.org.cowgill.james.jircd.Server;

/**
 * Class responsible for starting the server in network mode
 * 
 * @author James
 */
class NetworkStarter
{
	public static void main(String[] args)
	{
		//Server entry point
		// Setup logger
		Server.ensureLoggerSetup();
		
		// Get config file
		File configFile = Server.getFileFromArgs(args);
		
		if(configFile != null)
		{
			for(;;)
			{
				//Create server
				Server server = new NetworkServer(configFile);
				
				//Run server
				if(!server.run())
				{
					//Stop server
					break;
				}
				else
				{
					//Restart server
					server = null;

					//Do some GC work
					System.gc();
				}
			}
		}
	}
}
