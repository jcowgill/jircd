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
