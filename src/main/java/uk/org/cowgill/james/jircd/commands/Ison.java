package uk.org.cowgill.james.jircd.commands;

import java.util.Iterator;

import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.Server;

/**
 * The ISON command - determines whether given clients are online
 * 
 * @author James
 */
public class Ison implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Lookup each client
		StringBuilder clients = new StringBuilder();
		
		Iterator<String> paramIter = msg.paramIterator();
		while(paramIter.hasNext())
		{
			//Lookup client
			Client otherClient = Server.getServer().getClient(paramIter.next());
			
			if(otherClient != null)
			{
				clients.append(otherClient.id.nick);
				clients.append(' ');
			}
		}
		
		//Send reply
		client.send(client.newNickMessage("303").appendParam(clients.toString().trim()));
	}

	@Override
	public int getMinParameters()
	{
		return 1;
	}

	@Override
	public String getName()
	{
		return "ISON";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}
}
