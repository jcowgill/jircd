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
 * The KICK command - ejects a client from a channel
 * 
 * @author James
 */
public class Kick implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Parse channels and users to kick
		String[] channels = msg.getParam(0).split(",");
		String[] clients = msg.getParam(1).split(",");
		String kickMsg;
		
		//Get kick message
		if(msg.paramCount() >= 3)
		{
			kickMsg = msg.getParam(2);
		}
		else
		{
			kickMsg = client.id.nick;
		}
		
		//Prelookup for single channel
		Channel singleChannel = null;
		
		if(channels.length == 1)
		{
			singleChannel = Server.getServer().getChannel(channels[0]);
			
			if(singleChannel == null)
			{
				//No such channel
				client.send(client.newNickMessage("403").appendParam(channels[0]).
						appendParam("No such channel"));
				return;
			}
		}
		
		//Process kicks
		for(int i = 0; i < clients.length; ++i)
		{
			Channel channel;
			
			if(singleChannel == null)
			{
				//Lookup channel
				channel = Server.getServer().getChannel(channels[i]);
				
				if(channel == null)
				{
					//No such channel
					client.send(client.newNickMessage("403").appendParam(channels[i]).
							appendParam("No such channel"));
					continue;
				}
			}
			else
			{
				channel = singleChannel;
			}
			
			//Lookup client
			Client other = Server.getServer().getClient(clients[i]);
			
			if(other == null)
			{
				//No such channel
				client.send(client.newNickMessage("401").appendParam(clients[i]).
						appendParam("No such nick / channel"));
				continue;
			}
			
			//Can kick?
			ChannelCheckError error = ChannelChecks.canKick(channel, client, other);
			
			if(error == ChannelCheckError.OK)
			{
				//Kick
				channel.kick(client, other, kickMsg);
			}
			else
			{
				error.sendToClient(channel, client);
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
		return "KICK";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}
}
