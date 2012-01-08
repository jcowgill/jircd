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

import uk.org.cowgill.james.jircd.Channel;
import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.Server;
import uk.org.cowgill.james.jircd.util.ChannelCheckError;
import uk.org.cowgill.james.jircd.util.ChannelChecks;

/**
 * The INVITE command - invites a client into a channel
 * 
 * @author James
 */
public class Invite implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Lookup client and channel
		Client other = Server.getServer().getClient(msg.getParam(0));
		Channel channel = Server.getServer().getChannel(msg.getParam(1));
		
		if(other == null)
		{
			//No such nickname
			client.send(client.newNickMessage("401").appendParam(msg.getParam(0)).
					appendParam("No such nick / channel"));
		}
		else
		{
			//If channel exists, check we can invite
			if(channel != null)
			{
				ChannelCheckError error = ChannelChecks.canInvite(channel, client, other);
				
				if(error == ChannelCheckError.OK)
				{
					//Away message
					other.sendAwayMsgTo(client);
					
					//Do the invite
					channel.invite(client, other);
				}
				else
				{
					//Display error
					error.sendToClient(channel, client);
				}
			}
			else
			{
				//Do a fake invite
				Channel.inviteFake(msg.getParam(1), client, other);
			}
		}
	}

	@Override
	public int getMinParameters()
	{
		return 2;
	}

	@Override
	public String getName()
	{
		return "INVITE";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}
}
