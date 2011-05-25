package uk.org.cowgill.james.jircd.commands;

import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.Server;

/**
 * The LINKS command - displays links with other servers
 * 
 * @author James
 */
public class Links implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		String name = Server.getServer().getConfig().serverName;
		String info = Server.getServer().getConfig().serverDescription;
		
		//Send info about ourselves
		client.send(client.newNickMessage("364").
				appendParam(name).
				appendParam(name).
				appendParam("0 " + info));
		
		//Send end of list
		client.send(client.newNickMessage("365").appendParam("*").appendParam("End of /LINKS list"));
	}

	@Override
	public int getMinParameters()
	{
		return 0;
	}

	@Override
	public String getName()
	{
		return "LIST";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}
}
