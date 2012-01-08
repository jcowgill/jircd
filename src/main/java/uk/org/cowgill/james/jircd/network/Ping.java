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

import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.Server;

/**
 * PING command implementation
 * 
 * @author James
 */
class Ping implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Someone tried to ping us
		// Check for origin
		if(msg.paramCount() == 0)
		{
			client.send(Message.newStringFromServer("409 :No origin specified"));
		}
		
		// Pong back with server name
		Message reply = new Message("PONG")
				.appendParam(Server.getServer().getConfig().serverName);
		client.send(reply);
	}

	@Override
	public int getMinParameters()
	{
		return 0;
	}

	@Override
	public String getName()
	{
		return "PING";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL | FLAG_REGISTRATION;
	}
}
