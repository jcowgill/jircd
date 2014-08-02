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

import uk.org.cowgill.james.jircd.*;
import uk.org.cowgill.james.jircd.util.ChannelCheckError;
import uk.org.cowgill.james.jircd.util.ChannelChecks;
import uk.org.cowgill.james.jircd.util.NamesListBuilder;

/**
 * The NAMES command - views the memberlist of a channel
 *
 * @author James
 */
public class Names implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Specific channel?
		if (msg.paramCount() >= 1)
		{
			//Lookup channel
			Channel channel = Server.getServer().getChannel(msg.getParam(0));

			if(channel == null)
			{
				ChannelCheckError.GeneralNotInChannel.sendToClient(msg.getParam(0), client);
			}
			else
			{
				//Can view names?
				if(ChannelChecks.canGetNames(channel, client))
				{
					channel.sendNames(client);
				}
				else
				{
					ChannelCheckError.GeneralNotInChannel.sendToClient(msg.getParam(0), client);
				}
			}
		}
		else
		{
			// Send NAMES of all channels (may take some time :)
			for (Channel channel : Server.getServer().getChannels())
			{
				if (ChannelChecks.canGetNames(channel, client))
					channel.sendNamesWithoutEnd(client);
			}

			// Send NAMES for visible users not in a channel
			boolean seeInvisible = client.hasPermission(Permissions.seeInvisible);
			boolean hasUhNames = client.hasProtocolEnhancement(ProtocolEnhancements.UhNames);
			NamesListBuilder builder =
					new NamesListBuilder(client, client.newNickMessage("353").toString() + " * * :");

			for (Client other : Server.getServer().getRegisteredClients())
			{
				if (other.getChannels().size() == 0 &&
						(seeInvisible || !other.isModeSet('i')))
				{
					builder.addName(hasUhNames ? other.id.toString() :other.id.nick);
				}
			}

			builder.flush();

			// Send End of NAMES
			client.send(client.newNickMessage("366").
					appendParam("*").
					appendParam("End of NAMES list"));
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
		return "NAMES";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}
}
