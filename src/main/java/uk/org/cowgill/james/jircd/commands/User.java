package uk.org.cowgill.james.jircd.commands;

import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.RegistrationFlags;
import uk.org.cowgill.james.jircd.ServerISupport;

/**
 * The USER command - registers your username
 * 
 * @author James
 */
public class User implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Get relevant parameters
		String user = msg.getParam(0);
		String realName = msg.getParam(3);
		
		//Validate
		if(!ServerISupport.validateUser(user))
		{
			//No IRC message for this so return a big fat error
			client.send(Message.newMessageFromServer("ERROR").appendParam("Hostile username"));
			client.close("Hostile username");
			return;
		}
		
		//Set user and realname
		client.id.user = user;
		client.realName = realName;
		client.setRegistrationFlag(RegistrationFlags.UserSet);
	}

	@Override
	public int getMinParameters()
	{
		return 4;
	}

	@Override
	public String getName()
	{
		return "USER";
	}

	@Override
	public int getFlags()
	{
		return FLAG_REGISTRATION;
	}
}
