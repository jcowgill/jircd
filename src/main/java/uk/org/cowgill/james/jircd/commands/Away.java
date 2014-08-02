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

import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.ServerISupport;

/**
 * The AWAY command - marks a client as away or returned
 *
 * @author James
 */
public class Away implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Setting or unsetting away message?
		if(msg.paramCount() == 0)
		{
			//Unset away message
			if(client.isAway())
			{
				client.awayMsg = null;
				client.send(client.newNickMessage("305").appendParam("You are no longer away"));
			}
		}
		else
		{
			//Set away message
			if(!client.isAway())
			{
				//Validate message
				String away = msg.getParam(0);
				if(away.length() <= ServerISupport.AWAYLEN)
				{
					//Set away message
					client.awayMsg = away;
					client.send(client.newNickMessage("306").appendParam("You have been marked away"));
				}
			}
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
		return "AWAY";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}
}
