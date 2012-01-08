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
 * The PRIVMSG or NOTICE command - relays a message to another client or channel
 * 
 * @author James
 */
public abstract class Msg implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Lookup target
		if(msg.getParam(0).charAt(0) == '#')
		{
			//Channel lookup
			Channel channel = Server.getServer().getChannel(msg.getParam(0));
			
			if(channel != null)
			{
				//Can speak?
				ChannelCheckError error = ChannelChecks.canSpeak(channel, client);
				
				if(error == ChannelCheckError.OK)
				{
					channel.speak(client, getName(), msg.getParam(1));
				}
				else
				{
					//Send error
					error.sendToClient(channel, client);
				}
				
				return;
			}
		}
		else
		{
			//Client lookup
			Client other = Server.getServer().getClient(msg.getParam(0));
			
			if(other != null)
			{
				//Away message
				other.sendAwayMsgTo(client);
				
				//Send message
				Message relayMsg = new Message(getName(), client);
				relayMsg.appendParam(client.id.nick);
				relayMsg.appendParam(msg.getParam(1));
				
				other.send(relayMsg);
				return;
			}
		}
		
		//No such client / channel
		client.send(client.newNickMessage("401").appendParam(msg.getParam(0)).
				appendParam("No such nick / channel"));
	}

	@Override
	public int getMinParameters()
	{
		return 2;
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}

	/**
	 * The PRIVMSG message - sends a private message to a client or channel
	 * 
	 * @author James
	 */
	public static class PrivMsg extends Msg
	{
		@Override
		public String getName()
		{
			return "PRIVMSG";
		}
	}
	
	/**
	 * The NOTICE message - sends a notice to a client or channel
	 * 
	 * @author James
	 */
	public static class Notice extends Msg
	{
		@Override
		public String getName()
		{
			return "NOTICE";
		}
	}
}
