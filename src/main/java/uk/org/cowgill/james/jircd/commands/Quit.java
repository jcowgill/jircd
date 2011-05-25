package uk.org.cowgill.james.jircd.commands;

import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Message;

/**
 * The QUIT command - quits the IRC server
 * 
 * @author James
 */
public class Quit implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Construct quit message
		String quitMsg;
		if(msg.paramCount() == 0)
		{
			quitMsg = client.id.nick;
		}
		else
		{
			quitMsg = msg.getParam(0);
		}
		
		client.close(quitMsg);
	}

	@Override
	public int getMinParameters()
	{
		return 0;
	}

	@Override
	public String getName()
	{
		return "QUIT";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL | FLAG_REGISTRATION;
	}
}
