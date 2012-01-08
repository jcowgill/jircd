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

import java.util.Date;
import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.Server;

/**
 * The TIME command - displays the server time
 * 
 * @author James
 */
public class Time implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Send time
		client.send(client.newNickMessage("391").
				appendParam(Server.getServer().getConfig().serverName).
				appendParam(Server.DATE_FORMAT.format(new Date())));
	}

	@Override
	public int getMinParameters()
	{
		return 0;
	}

	@Override
	public String getName()
	{
		return "TIME";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}
}
