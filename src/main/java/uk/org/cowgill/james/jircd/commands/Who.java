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

import java.util.Collection;
import uk.org.cowgill.james.jircd.Channel;
import uk.org.cowgill.james.jircd.ChannelMemberMode;
import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.IRCMask;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.Permissions;
import uk.org.cowgill.james.jircd.Server;
import uk.org.cowgill.james.jircd.util.MemberListDisplayer;

/**
 * The WHO command - displays information about clients
 * 
 * @author James
 */
public class Who implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Get mask
		String mask;
		boolean operOnly = false;
		
		if(msg.paramCount() == 0)
		{
			//Force everyone
			mask = "*";
		}
		else
		{
			//Use given mask
			mask = msg.getParam(0);
			
			if(mask.equals("0"))
			{
				mask = "*";
			}
			
			//Check opers only
			operOnly = (msg.paramCount() >= 2 && msg.getParam(1).equals("o"));
		}
		
		//Check for chanel
		if(mask.charAt(0) == '#')
		{
			//Print channel members
			Channel channel = Server.getServer().getChannel(mask);
			
			if(channel != null)
			{
				//Print channel members
				MemberListDisplayer.listChannel(client, channel, new MemberListDisplayer.Executer()
				{
					@Override
					public void displayMember(Client client, Channel channel, Client other,
							ChannelMemberMode mode)
					{
						//Forward each member display to a new message
						Who.sendWhoMsg(client, other, channel, mode);
					}
				});
			}
		}
		else
		{
			//Search all clients
			Collection<Client> clients;
			
			if(operOnly)
			{
				clients = Server.getServer().getRegisteredClients();
			}
			else
			{
				clients = Server.getServer().getIRCOperators();
			}
			
			//Do windcard test on all clients in the list
			for(Client other : clients)
			{
				//Check visibility
				Channel commonChannel = MemberListDisplayer.findCommonChannel(client, other);
				
				if(commonChannel != null || !other.isModeSet('i') || client == other
						|| client.hasPermission(Permissions.seeInvisible))
				{
					if(IRCMask.wildcardCompare(other.id.nick, mask) ||
						IRCMask.wildcardCompare(other.id.user, mask) ||
						IRCMask.wildcardCompare(other.id.host, mask) ||
						IRCMask.wildcardCompare(other.realName, mask))
					{
						//Send this client
						sendWhoMsg(client, other, commonChannel, null);
					}
				}
			}
		}
		
		//Send end reply
		client.send(client.newNickMessage("315").
				appendParam(mask).appendParam("End of /WHO list"));
	}

	/**
	 * Sends a WHO reply to client
	 * 
	 * @param client client to reply to
	 * @param other client information is read from
	 * @param chanName common channel (or null if no common channel)
	 * @param chanMode other's channel mode (or null to find mode)
	 */
	private static void sendWhoMsg(Client client, Client other, Channel channel, ChannelMemberMode chanMode)
	{
		//Calculate channel name and mode
		String chanName = "*";
		if(channel != null)
		{
			//Get name
			chanName = channel.getName();
			
			//Get mode
			if(chanMode == null)
			{
				chanMode = channel.lookupMember(other);
			}
		}
		
		//Send reply
		StringBuilder info = new StringBuilder();
		
		if(other.isAway())
		{
			info.append('G');
		}
		else
		{
			info.append('H');
		}
		
		//IRC op prefix
		if(other.isModeSet('o') || other.isModeSet('O'))
		{
			info.append('*');
		}

		//Chan op prefix
		if(chanMode != null)
		{
			info.append(chanMode.toPrefixString(true));
		}
		
		//Final send
		client.send(client.newNickMessage("352").
				appendParam(chanName).
				appendParam(other.id.user).
				appendParam(other.id.host).
				appendParam(Server.getServer().getConfig().serverName).
				appendParam(other.id.nick).
				appendParam(info.toString()).
				appendParam("0 " + other.realName));
	}
	
	@Override
	public int getMinParameters()
	{
		return 0;
	}

	@Override
	public String getName()
	{
		return "WHO";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}
}
