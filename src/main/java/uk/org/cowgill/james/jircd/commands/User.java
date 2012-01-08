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
import uk.org.cowgill.james.jircd.RegistrationFlags;
import uk.org.cowgill.james.jircd.ServerISupport;

/**
 * The USER command - registers your username
 * 
 * @author James
 */
public class User implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Musn't have USER set already
		if((client.getRegistrationFlags() & RegistrationFlags.UserSet) != 0)
		{
			//No reregistering
			client.send(Message.newMessageFromServer("462")
					.appendParam("USER").appendParam("You cannot reregister"));
			return;
		}
		
		//Get relevant parameters
		String user = msg.getParam(0);
		String realName = msg.getParam(3);
		
		//Validate
		if(!ServerISupport.validateUser(user))
		{
			//No IRC message for this so return a big fat error
			client.send(Message.newMessageFromServer("ERROR").appendParam("Hostile username"));
			client.close("Hostile username");
			return;
		}
		
		//Set user and realname
		client.id.user = user;
		client.realName = realName;
		client.setRegistrationFlag(RegistrationFlags.UserSet);
	}

	@Override
	public int getMinParameters()
	{
		return 4;
	}

	@Override
	public String getName()
	{
		return "USER";
	}

	@Override
	public int getFlags()
	{
		return FLAG_REGISTRATION;
	}
}
