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

import java.util.Properties;

import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.Server;

/**
 * The INFO command - displays server info
 *
 * @author James
 */
public class Info implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		Server server = Server.getServer();
		Properties sysInfo = System.getProperties();

		//Send server software information
		client.send(client.newNickMessage("371").appendParam("== " + Server.VERSION_STR + " =="));
		client.send(client.newNickMessage("371").appendParam("| By James Cowgill"));
		client.send(client.newNickMessage("371").appendParam("|"));
		client.send(client.newNickMessage("371").appendParam("| Server Started: " + server.creationTimeStr));
		client.send(client.newNickMessage("371").
				appendParam("| Running Java " + sysInfo.getProperty("java.version")));
		client.send(client.newNickMessage("371").
				appendParam("|  on " + sysInfo.getProperty("os.name") + " " +
						sysInfo.getProperty("os.version")));

		client.send(client.newNickMessage("374").appendParam("End of /INFO list"));
	}

	@Override
	public int getMinParameters()
	{
		return 0;
	}

	@Override
	public String getName()
	{
		return "INFO";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}
}
