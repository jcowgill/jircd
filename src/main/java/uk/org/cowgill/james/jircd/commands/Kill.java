package uk.org.cowgill.james.jircd.commands;

import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.Permissions;
import uk.org.cowgill.james.jircd.Server;

import org.apache.log4j.Logger;

/**
 * The KILL command - ejects a client from the server
 * 
 * @author James
 */
public class Kill implements Command
{
	private static final Logger logger = Logger.getLogger(Kill.class);
	
	@Override
	public void run(Client client, Message msg)
	{
		//Lookup client
		Client other = Server.getServer().getClient(msg.getParam(0));
		
		if(other == null)
		{
			//No such nickname
			client.send(client.newNickMessage("401").appendParam(msg.getParam(0)).
					appendParam("No such nick / channel"));
		}
		else
		{
			//Must have kill rights
			if(client.hasPermission(Permissions.kill))
			{
				//Kill them
				logger.warn(client.id.toString() + " killed " + other.id.toString() +
						" (" + msg.getParam(1) + ")");
				other.close("Killed by " + client.id.nick + " (" + msg.getParam(1) + ")");
			}
			else
			{
				//Permission denied
				logger.warn(client.id.toString() + " attempted to kill " +
							other.id.toString() + " but was denied");
				client.send(client.newNickMessage("481").appendParam("KILL: Permission Denied"));
			}
		}
	}

	@Override
	public int getMinParameters()
	{
		return 2;
	}

	@Override
	public String getName()
	{
		return "KILL";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}
}
