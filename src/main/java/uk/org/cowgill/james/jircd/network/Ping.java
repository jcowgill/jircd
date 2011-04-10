package uk.org.cowgill.james.jircd.network;

import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.Server;

/**
 * PING command implementation
 * 
 * @author James
 */
class Ping implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Someone tried to ping us
		// Check for origin
		if(msg.paramCount() == 0)
		{
			client.send(Message.newStringFromServer("409 :No origin specified"));
		}
		
		// Pong back with server name
		Message reply = new Message("PONG")
				.appendParam(Server.getServer().getConfig().serverName);
		client.send(reply);
	}

	@Override
	public int getMinParameters()
	{
		return 0;
	}

	@Override
	public String getName()
	{
		return "PING";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL | FLAG_REGISTRATION;
	}
}
