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
		
		// Add Ctrl-C Handler
		CtrlCHandler ctrlCHandler = new CtrlCHandler();
		Runtime.getRuntime().addShutdownHook(new Thread(ctrlCHandler));

		// Get config file
		File configFile = Server.getFileFromArgs(args);
		
		if(configFile != null)
		{
			//Run the server!
			// The loop handles server restarts
			while(new NetworkServer(configFile).run() && ctrlCHandler.canContinue)
				;
		}
	}

	/**
	 * Handles control c events
	 *
	 * @author James
	 */
	private static class CtrlCHandler implements Runnable
	{
		private final Thread parentThread;
		public boolean canContinue = true;

		public CtrlCHandler()
		{
			this.parentThread = Thread.currentThread();
		}

		@Override
		public void run()
		{
			//Mark as handled something
			canContinue = false;

			//Start loop to kill all servers!
			while(parentThread.isAlive())
			{
				//Get current server
				Server current = Server.getServer();

				if(current != null)
				{
					//Request a close
					current.requestStop("Control-C Pressed");
				}

				//Wait for parent to die
				try
				{
					parentThread.join(10);
				}
				catch (InterruptedException e)
				{
				}
			}
		}
	}
}
