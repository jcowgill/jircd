package uk.org.cowgill.james.jircd.commands;

import uk.org.cowgill.james.jircd.Channel;
import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.Server;
import uk.org.cowgill.james.jircd.util.ChannelCheckError;
import uk.org.cowgill.james.jircd.util.ChannelChecks;

/**
 * The INVITE command - invites a client into a channel
 * 
 * @author James
 */
public class Invite implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Lookup client and channel
		Client other = Server.getServer().getClient(msg.getParam(0));
		Channel channel = Server.getServer().getChannel(msg.getParam(1));
		
		if(other == null)
		{
			//No such nickname
			client.send(client.newNickMessage("401").appendParam(msg.getParam(0)).
					appendParam("No such nick / channel"));
		}
		else
		{
			//If channel exists, check we can invite
			if(channel != null)
			{
				ChannelCheckError error = ChannelChecks.canInvite(channel, client, other);
				
				if(error == ChannelCheckError.OK)
				{
					//Do the invite
					channel.invite(client, other);
				}
				else
				{
					//Display error
					error.sendToClient(channel, client);
				}
			}
			else
			{
				//Do a fake invite
				Channel.inviteFake(msg.getParam(1), client, other);
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
		return "INVITE";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}
}
