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
import uk.org.cowgill.james.jircd.Permissions;
import uk.org.cowgill.james.jircd.Server;

/**
 * The WALL command - sends a message to all clients
 * 
 * @author James
 */
public class Wall implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Check permissions
		if(client.hasPermission(Permissions.wall))
		{
			//Send WALLOPS message
			Message wMsg = new Message("WALLOPS", client);
			wMsg.appendParam(msg.getParam(0));
			
			Client.sendTo(Server.getServer().getRegisteredClients(), wMsg);
		}
		else
		{
			client.send(client.newNickMessage("481").appendParam("WALL: Permission Denied"));
		}
	}

	@Override
	public int getMinParameters()
	{
		return 1;
	}

	@Override
	public String getName()
	{
		return "WALL";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}
}
