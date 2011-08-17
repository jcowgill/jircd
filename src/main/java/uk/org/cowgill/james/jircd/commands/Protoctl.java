package uk.org.cowgill.james.jircd.commands;

import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.ProtocolEnhancements;

/**
 * The PROTOCTL command - enables protocol enhancements
 * 
 * @author James
 */
public class Protoctl implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Process each enhancement and set it in the calling client
		for(String enhancement : msg.getParamList())
		{
			if(enhancement.equalsIgnoreCase("NAMESX"))
			{
				client.setProtocolEnhancement(ProtocolEnhancements.NamesX);
			}
			else if(enhancement.equalsIgnoreCase("UHNAMES"))
			{
				client.setProtocolEnhancement(ProtocolEnhancements.UhNames);
			}
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
		return "PROTOCTL";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}
}
