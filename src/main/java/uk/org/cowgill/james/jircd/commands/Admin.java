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
package uk.org.cowgill.james.jircd.commands;

import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.Server;

/**
 * The ADMIN command - displays server administrative info
 * 
 * @author James
 */
public class Admin implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Send admin information
		String serverName = Server.getServer().getConfig().serverName;
		String[] admin = Server.getServer().getConfig().admin;
		
		//Send admin start
		client.send(client.newNickMessage("256").
				appendParam(serverName).
				appendParam("Administrative info for server " + serverName));
		
		//Send messages
		for(int i = 0; i < admin.length; ++i)
		{
			//What line?
			String lineCode;
			
			if(i == 0)
			{
				lineCode = "257";
			}
			else if(i == admin.length)
			{
				lineCode = "259";
			}
			else
			{
				lineCode = "258";
			}
			
			//Send message
			client.send(client.newNickMessage(lineCode).appendParam(admin[i]));
		}
	}

	@Override
	public int getMinParameters()
	{
		return 0;
	}

	@Override
	public String getName()
	{
		return "ADMIN";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}
}
