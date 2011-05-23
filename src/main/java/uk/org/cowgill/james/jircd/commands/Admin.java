package uk.org.cowgill.james.jircd.commands;

import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.Server;

/**
 * The ADMIN command - displays server administrative info
 * 
 * @author James
 */
public class Admin implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Send admin information
		String serverName = Server.getServer().getConfig().serverName;
		String[] admin = Server.getServer().getConfig().admin;
		
		//Send admin start
		client.send(client.newNickMessage("256").
				appendParam(serverName).
				appendParam("Administrative info for server " + serverName));
		
		//Send messages
		for(int i = 0; i < admin.length; ++i)
		{
			//What line?
			String lineCode;
			
			if(i == 0)
			{
				lineCode = "257";
			}
			else if(i == admin.length)
			{
				lineCode = "259";
			}
			else
			{
				lineCode = "258";
			}
			
			//Send message
			client.send(client.newNickMessage(lineCode).appendParam(admin[i]));
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
		return "ADMIN";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}
}
