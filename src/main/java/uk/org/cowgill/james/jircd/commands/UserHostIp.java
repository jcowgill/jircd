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

import java.util.Iterator;

import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.Permissions;
import uk.org.cowgill.james.jircd.Server;

/**
 * The USERHOST and USERIP commands - displays the user and host/ip of a nickname
 * 
 * @author James
 */
public abstract class UserHostIp implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		StringBuilder output = new StringBuilder();

		//Process nick parameters
		Iterator<String> nicks = msg.paramIterator();
		while(nicks.hasNext())
		{
			processNick(client, nicks.next(), output);
		}
		
		//Send information
		client.send(client.newNickMessage(getNumeric()).appendParam(output.toString().trim()));
	}
	
	private void processNick(Client client, String nick, StringBuilder output)
	{
		//Lookup client
		Client other = Server.getServer().getClient(nick);
		
		if(other != null)
		{
			//Get user host
			String host = getUserHost(client, other);
			
			//Send information
			output.append(other.id.nick);
			
			if(other.isModeSet('o') || other.isModeSet('O'))
			{
				output.append('*');
			}
			
			output.append('=');
			output.append(other.isAway() ? '-' : '+');
			output.append(host);
		}
	}
	
	/**
	 * Should return the username@hostname of the given client
	 * 
	 * <p>If null is returned, the client is not processed
	 * 
	 * @param client client who is fetching the information
	 * @param other other client to get information from
	 */
	protected abstract String getUserHost(Client client, Client other);

	/**
	 * Returns the IRC numeric of this command
	 * @return the IRC numeric of this command
	 */
	protected abstract String getNumeric();
	
	@Override
	public int getMinParameters()
	{
		return 1;
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}

	/**
	 * The USERHOST command - displays the user and host of a nickname
	 * 
	 * @author James
	 */
	public static class UserHost extends UserHostIp
	{
		@Override
		public String getName()
		{
			return "USERHOST";
		}

		@Override
		protected String getNumeric()
		{
			return "302";
		}

		@Override
		protected String getUserHost(Client client, Client other)
		{
			return other.id.user + '@' + other.id.host;
		}
	}

	/**
	 * The USERIP command - displays the user and ip of a nickname
	 * 
	 * @author James
	 */
	public static class UserIp extends UserHostIp
	{
		@Override
		public String getName()
		{
			return "USERIP";
		}

		@Override
		protected String getNumeric()
		{
			return "340";
		}

		@Override
		protected String getUserHost(Client client, Client other)
		{
			//Verify permissions
			if(client == other || client.hasPermission(Permissions.userIP))
			{
				return other.id.user + '@' + other.getIpAddress();
			}
			else
			{
				return null;
			}
		}
	}
}
