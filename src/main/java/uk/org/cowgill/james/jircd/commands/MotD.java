package uk.org.cowgill.james.jircd.commands;

import java.util.List;

import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Config;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.Server;

/**
 * The MOTD command - sends the server's message of the day
 * 
 * @author James
 */
public class MotD implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Send MotD reply
		Config config = Server.getServer().getConfig();
		List<String> motd = config.motd;
		
		if(motd.isEmpty())
		{
			//No MotD
			client.send(client.newNickMessage("422").appendParam("No MotD"));
		}
		else
		{
			//Send MotD
			client.send(client.newNickMessage("375").
					appendParam("- " + config.serverName + " Message of the Day -"));
			
			//Get line prefix
			String prefix = client.newNickMessage("372") + " :- ";
			
			for(String line : motd)
			{
				client.send(prefix + line);
			}
			
			//Send end of MotD
			client.send(client.newNickMessage("376").appendParam("- End of MotD"));
		}
	}

	@Override
	public int getMinParameters()
	{
		return 0;
	}

	@Override
	public String getName()
	{
		return "MOTD";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}
}
