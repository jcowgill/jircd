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

import java.util.List;

import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Config;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.Server;

/**
 * The MOTD command - sends the server's message of the day
 *
 * @author James
 */
public class MotD implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Send MotD reply
		Config config = Server.getServer().getConfig();
		List<String> motd = config.motd;

		if(motd.isEmpty())
		{
			//No MotD
			client.send(client.newNickMessage("422").appendParam("No MotD"));
		}
		else
		{
			//Send MotD
			client.send(client.newNickMessage("375").
					appendParam("- " + config.serverName + " Message of the Day -"));

			//Get line prefix
			String prefix = client.newNickMessage("372") + " :- ";

			for(String line : motd)
			{
				client.send(prefix + line);
			}

			//Send end of MotD
			client.send(client.newNickMessage("376").appendParam("- End of MotD"));
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
		return "MOTD";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}
}
