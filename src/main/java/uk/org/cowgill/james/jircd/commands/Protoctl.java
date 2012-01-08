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
import uk.org.cowgill.james.jircd.ProtocolEnhancements;

/**
 * The PROTOCTL command - enables protocol enhancements
 * 
 * @author James
 */
public class Protoctl implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Process each enhancement and set it in the calling client
		for(String enhancement : msg.getParamList())
		{
			if(enhancement.equalsIgnoreCase("NAMESX"))
			{
				client.setProtocolEnhancement(ProtocolEnhancements.NamesX);
			}
			else if(enhancement.equalsIgnoreCase("UHNAMES"))
			{
				client.setProtocolEnhancement(ProtocolEnhancements.UhNames);
			}
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
		return "PROTOCTL";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}
}
