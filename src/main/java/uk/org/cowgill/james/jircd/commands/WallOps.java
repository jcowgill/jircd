package uk.org.cowgill.james.jircd.commands;

import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.Permissions;
import uk.org.cowgill.james.jircd.Server;

/**
 * The WALLOPS command - sends a message to other operators
 * 
 * @author James
 */
public class WallOps implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Check permissions
		if(client.hasPermission(Permissions.wallops))
		{
			//Send WALLOPS message
			Message wMsg = new Message("WALLOPS", client);
			wMsg.appendParam(msg.getParam(0));
			
			Client.sendTo(Server.getServer().getIRCOperators(), wMsg);
		}
		else
		{
			client.send(client.newNickMessage("481").appendParam("WALLOPS: Permission Denied"));
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
		return "WALLOPS";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}
}
