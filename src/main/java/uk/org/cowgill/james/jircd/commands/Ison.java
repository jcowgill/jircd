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

import java.util.Iterator;

import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.Server;

/**
 * The ISON command - determines whether given clients are online
 *
 * @author James
 */
public class Ison implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Lookup each client
		StringBuilder clients = new StringBuilder();

		Iterator<String> paramIter = msg.paramIterator();
		while(paramIter.hasNext())
		{
			//Lookup client
			Client otherClient = Server.getServer().getClient(paramIter.next());

			if(otherClient != null)
			{
				clients.append(otherClient.id.nick);
				clients.append(' ');
			}
		}

		//Send reply
		client.send(client.newNickMessage("303").appendParam(clients.toString().trim()));
	}

	@Override
	public int getMinParameters()
	{
		return 1;
	}

	@Override
	public String getName()
	{
		return "ISON";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}
}
