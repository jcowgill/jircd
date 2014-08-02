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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import uk.org.cowgill.james.jircd.Channel;
import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.Permissions;
import uk.org.cowgill.james.jircd.Server;
import uk.org.cowgill.james.jircd.util.ChannelCheckError;
import uk.org.cowgill.james.jircd.util.ChannelChecks;

/**
 * The JOIN command - joins a channel
 *
 * @author James
 */
public class Join implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Parse join parameters
		if(msg.getParam(0).equals("0"))
		{
			//Part all channels
			List<Channel> channelsCopy = new ArrayList<Channel>(client.getChannels());

			for(Channel channel : channelsCopy)
			{
				channel.part(client, client.id.nick);
			}
		}
		else
		{
			String[] chanStrings = msg.getParam(0).split(",");
			String[] keyStrings;

			if(msg.paramCount() >= 2)
			{
				//Split keys
				keyStrings = msg.getParam(1).split(",");
			}
			else
			{
				keyStrings = new String[0];
			}

			//Process joins
			for(int i = 0; i < chanStrings.length; ++i)
			{
				processJoin(client, chanStrings[i], keyStrings, i);
			}
		}
	}

	/**
	 * Processes a channel join request
	 *
	 * @param client client who's joining
	 * @param chanString channel being joined
	 * @param keyStrings all the keystrings that are searched
	 * @param i join index into keystrings array
	 */
	private void processJoin(Client client, String chanString, String[] keyStrings, int i)
	{
		//Ensure leading #
		if(chanString.charAt(0) != '#')
		{
			chanString = "#" + chanString;
		}

		//Find channel
		Channel channel = Server.getServer().getChannel(chanString);

		if(channel == null)
		{
			//Create channel
			channel = Channel.createChannel(chanString);
		}
		else
		{
			ChannelCheckError error;

			//Check join
			if(keyStrings.length > i)
			{
				error = ChannelChecks.canJoin(channel, client, keyStrings[i], allowJoinAny());
			}
			else
			{
				error = ChannelChecks.canJoin(channel, client, null, allowJoinAny());
			}

			//Display error
			if(error == ChannelCheckError.JoinAlreadyInChannel)
			{
				//Ignore this channel join
				return;
			}
			else if(error != ChannelCheckError.OK)
			{
				//Print error
				error.sendToClient(channel, client);
				return;
			}
		}

		//Join channel
		channel.join(client, true);
		postJoin(channel, client);
	}

	/**
	 * Called after the the client has joined the channel specified
	 *
	 * @param channel channel to join
	 * @param client client who's joining
	 */
	protected void postJoin(Channel channel, Client client)
	{
	}

	/**
	 * Called to delect whether operators are allowed to use joinAnyChannel permission
	 *
	 * @return if joinAnyChannel is allowed
	 */
	protected boolean allowJoinAny()
	{
		return false;
	}

	@Override
	public int getMinParameters()
	{
		return 1;
	}

	@Override
	public String getName()
	{
		return "JOIN";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}

	public static class JoinA extends Join
	{
		private static final Logger logger = Logger.getLogger(JoinA.class);

		@Override
		public void run(Client client, Message msg)
		{
			//Validate permissions
			if(client.hasPermission(Permissions.joinAdmin))
			{
				//Allowed
				super.run(client, msg);
			}
			else
			{
				//Permission denied
				logger.warn(client.id.toString() + " attempted to use JOINA but was denied");
				client.send(client.newNickMessage("481").appendParam("JOINA: Permission Denied"));
			}
		}

		@Override
		protected void postJoin(Channel channel, Client client)
		{
			//Set admin
			channel.setMode(null, true, 'a', client);
		}

		@Override
		protected boolean allowJoinAny()
		{
			return true;
		}

		@Override
		public String getName()
		{
			return "JOINA";
		}
	}
}
