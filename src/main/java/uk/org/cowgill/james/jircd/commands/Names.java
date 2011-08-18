package uk.org.cowgill.james.jircd.commands;

import uk.org.cowgill.james.jircd.Channel;
import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.Server;
import uk.org.cowgill.james.jircd.util.ChannelCheckError;
import uk.org.cowgill.james.jircd.util.ChannelChecks;

/**
 * The NAMES command - views the memberlist of a channel
 * 
 * @author James
 */
public class Names implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Lookup channel
		Channel channel = Server.getServer().getChannel(msg.getParam(0));
		
		if(channel == null)
		{
			ChannelCheckError.GeneralNotInChannel.sendToClient(msg.getParam(0), client);
		}
		else
		{
			//Can view names?
			if(ChannelChecks.canGetNames(channel, client))
			{
				channel.sendNames(client);
			}
			else
			{
				ChannelCheckError.GeneralNotInChannel.sendToClient(msg.getParam(0), client);
			}
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
		return "NAMES";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}
}
