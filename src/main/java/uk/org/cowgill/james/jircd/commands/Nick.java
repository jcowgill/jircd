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
import uk.org.cowgill.james.jircd.RegistrationFlags;
import uk.org.cowgill.james.jircd.ServerISupport;

/**
 * The NICK command - changes your own nickname
 * 
 * @author James
 */
public class Nick implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Nick exists?
		if(msg.paramCount() < 0)
		{
			client.send(client.newNickMessage("431").appendParam("No nickname given"));
			return;
		}
		
		//Validate nick
		String nick = msg.getParam(0);
		if(!ServerISupport.validateNick(nick))
		{
			client.send(client.newNickMessage("432").appendParam("Erronous nickname"));
			return;
		}
		
		//Cannot change if banned in any channel
		for(Channel chan : client.getChannels())
		{
			if(chan.isBanned(client))
			{
				//Cannot change nick
				client.send(client.newNickMessage("437").
						appendParam("Cannot change nickname while banned on a channel"));
				return;
			}
		}
		
		//Set nick
		if(client.setNick(nick))
		{
			client.setRegistrationFlag(RegistrationFlags.NickSet);
		}
		else
		{
			client.send(client.newNickMessage("433")
					.appendParam(nick)
					.appendParam("Nickname already in use"));
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
		return "NICK";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL | FLAG_REGISTRATION;
	}
}
