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
import uk.org.cowgill.james.jircd.Config;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.Permissions;
import uk.org.cowgill.james.jircd.Server;

/**
 * The WHOIS command - displays information about a client
 * 
 * @author James
 */
public class Whois implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Lookup client
		Config config = Server.getServer().getConfig();
		Client other = Server.getServer().getClient(msg.getParam(0));
		
		if(other == null)
		{
			//No such nickname
			client.send(client.newNickMessage("401").appendParam(msg.getParam(0)).
					appendParam("No such nick / channel"));
		}
		else
		{
			//Notify other if they are an irc operator
			if(other.hasPermission(Permissions.seeWhois))
			{
				other.send(Message.newMessageFromServer("NOTICE").
						appendParam(other.id.nick).
						appendParam(client.id.nick + " whoised you"));
			}
			
			//Send information
			client.send(client.newNickMessage("311").
					appendParam(other.id.nick).
					appendParam(other.id.user).
					appendParam(other.id.host).
					appendParam("*").
					appendParam(other.realName));
			
			client.send(client.newNickMessage("312").
					appendParam(other.id.nick).
					appendParam(config.serverName).
					appendParam(config.serverDescription));
			
			//IRC operator?
			if(other.isModeSet('o') || other.isModeSet('O'))
			{
				String superStr = other.isModeSet('O') ? "super " : " ";
				
				client.send(client.newNickMessage("313").
						appendParam(other.id.nick).
						appendParam("is an IRC " + superStr + "operator"));
			}
			
			//Secure?
			if(other.isModeSet('z'))
			{
				client.send(client.newNickMessage("617").
						appendParam(other.id.nick).
						appendParam("is using a Secure Connection"));
			}
			
			//Display channels
			// Send up to 8 channels per line
			StringBuilder builder = new StringBuilder();
			int namesThisLine = 0;
			Message chanMsg = null;
			
			for(Channel channel : other.getChannels())
			{
				String chanName;

				//Check for secret or private channels
				if(channel.isModeSet('s'))
				{
					continue;
				}
				else if(channel.isModeSet('p'))
				{
					//Channel name is displayed as [private]
					chanName = "[private]";
				}
				else
				{
					//Channel name contains prefix as well
					chanName = channel.lookupMember(other).toPrefixString(true) + channel.getName();
				}

				//Setup new message
				if(chanMsg == null)
				{
					chanMsg = client.newNickMessage("319").appendParam(other.id.nick);
				}
				else
				{
					builder.append(' ');
				}
				
				//Add channel
				builder.append(chanName);
				namesThisLine++;
				
				//If 8 names, send message
				if(namesThisLine >= 8)
				{
					chanMsg.appendParam(builder.toString());
					client.send(msg);
					
					chanMsg = null;
					namesThisLine = 0;
				}
			}
			
			//Send ending
			if(chanMsg != null)
			{
				chanMsg.appendParam(builder.toString());
				client.send(chanMsg);
			}
			
			//Away message
			other.sendAwayMsgTo(client);

			client.send(client.newNickMessage("317").
					appendParam(other.id.nick).
					appendParam(Long.toString(other.getIdleTime() / 1000)).
					appendParam(Long.toString(other.getSignonTime() / 1000)).
					appendParam("seconds idle, signon time"));
			
			client.send(client.newNickMessage("318").
					appendParam(other.id.nick).
					appendParam("End of /WHOIS list"));
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
		return "WHOIS";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}
}
