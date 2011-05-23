package uk.org.cowgill.james.jircd.commands;

import uk.org.cowgill.james.jircd.Channel;
import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.RegistrationFlags;
import uk.org.cowgill.james.jircd.ServerISupport;

/**
 * The NICK command - changes your own nickname
 * 
 * @author James
 */
public class Nick implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Nick exists?
		if(msg.paramCount() < 0)
		{
			client.send(client.newNickMessage("431").appendParam("No nickname given"));
			return;
		}
		
		//Validate nick
		String nick = msg.getParam(0);
		if(!ServerISupport.validateNick(nick))
		{
			client.send(client.newNickMessage("432").appendParam("Erronous nickname"));
			return;
		}
		
		//Cannot change if banned in any channel
		for(Channel chan : client.getChannels())
		{
			if(chan.isBanned(client))
			{
				//Cannot change nick
				client.send(client.newNickMessage("437").
						appendParam("Cannot change nickname while banned on a channel"));
				return;
			}
		}
		
		//Set nick
		client.setNick(nick);
		client.setRegistrationFlag(RegistrationFlags.NickSet);
	}

	@Override
	public int getMinParameters()
	{
		return 0;
	}

	@Override
	public String getName()
	{
		return "NICK";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL | FLAG_REGISTRATION;
	}
}
