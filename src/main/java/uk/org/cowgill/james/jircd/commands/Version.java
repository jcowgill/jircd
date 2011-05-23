package uk.org.cowgill.james.jircd.commands;

import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.Server;

/**
 * The VERSION command - sends the server version
 * 
 * @author James
 */
public class Version implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		client.send(client.newNickMessage("351").appendParam(Server.VERSION_STR + " By James Cowgill"));
	}

	@Override
	public int getMinParameters()
	{
		return 0;
	}

	@Override
	public String getName()
	{
		return "VERSION";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL | FLAG_REGISTRATION;
	}
}
