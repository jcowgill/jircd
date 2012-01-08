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
import uk.org.cowgill.james.jircd.Server;

/**
 * The LUSERS command - sends statistics about number of users
 * 
 * @author James
 */
public class LUsers implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		Server server = Server.getServer();
		
		//Get server statistics
		int users = server.getClientCount();
		int opers = server.getIRCOperators().size();
		int unknown = server.getUnregisteredClients();
		int channels = server.getChannels().size();
		int peekUsers = server.getClientCountPeek();
				
		//Send server statistics
		// Entire network user count
		client.send(client.newNickMessage("251").
				appendParam("There are " + users + " users on 1 server"));
		
		// IRC operators
		if(opers > 0)
		{
			client.send(client.newNickMessage("252").
					appendParam(Integer.toString(opers)).appendParam("operator(s) online"));
		}
		
		// Number of unregistered connections
		if(unknown > 0)
		{
			client.send(client.newNickMessage("253").
					appendParam(Integer.toString(unknown)).appendParam("unknown connection(s)"));
		}
		
		// Number of channels
		if(channels > 0)
		{
			client.send(client.newNickMessage("254").
					appendParam(Integer.toString(channels)).appendParam("channel(s) formed"));			
		}
		
		// This server user count
		client.send(client.newNickMessage("255").
				appendParam("I have " + users + " clients and 1 server"));
		
		// Send users with peek
		client.send(client.newNickMessage("265").
				appendParam("Current Local Users: " + users + "  Max: " + peekUsers));
		
		client.send(client.newNickMessage("266").
				appendParam("Current Global Users: " + users + "  Max: " + peekUsers));
	}

	@Override
	public int getMinParameters()
	{
		return 0;
	}

	@Override
	public String getName()
	{
		return "LUSERS";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}
}
