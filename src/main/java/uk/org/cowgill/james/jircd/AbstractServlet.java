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
package uk.org.cowgill.james.jircd;

import uk.org.cowgill.james.jircd.util.ModeUtils;

/**
 * An abstract servelt helper class
 *
 * @author James
 */
public abstract class AbstractServlet extends Client
{
	/**
	 * Event occurs when a message is received by this servlet
	 *
	 * @param msg message received
	 */
	public abstract void messageReceived(Message msg);

	/**
	 * Creates a new servlet with the given ID
	 *
	 * To avoid confusion - servlets should use either 127.0.0.1 or localhost as the hostname
	 *
	 * @param id ID of this servlet
	 * @throws ModuleLoadException thrown if the nickname already exists
	 */
	protected AbstractServlet(IRCMask id) throws ModuleLoadException
	{
		//Set ID
		super(id, ModeUtils.setMode(0, 'B'));

		//Validate nickname
		Server server = Server.getServer();
		if(server.clientsByNick.containsKey(id.nick))
		{
			//Error
			server.clients.remove(this);
			throw new ModuleLoadException("Nickname for servlet already registered");
		}

		//Register
		this.setRegistrationFlag(RegistrationFlags.AllFlags);
		server.clientsByNick.put(id.nick, this);
	}

	@Override
	public final void send(Object data)
	{
		Message msg;

		//Redirect to received message
		if(data instanceof Message)
		{
			msg = (Message) data;
		}
		else
		{
			msg = Message.parse(data.toString());
		}

		messageReceived(msg);
	}

	@Override
	protected boolean changeClass(ConnectionClass clazz, boolean defaultClass)
	{
		//Fake good class change
		return true;
	}

	@Override
	public void restoreClass()
	{
	}

	@Override
	public String getIpAddress()
	{
		return "127.0.0.1";
	}

	@Override
	public boolean isRemote()
	{
		return false;
	}

	@Override
	public long getIdleTime()
	{
		return 0;
	}
}
