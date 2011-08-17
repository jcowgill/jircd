package uk.org.cowgill.james.jircd.commands;

import java.util.ArrayList;
import java.util.List;

import uk.org.cowgill.james.jircd.Channel;
import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.Server;
import uk.org.cowgill.james.jircd.util.ChannelCheckError;
import uk.org.cowgill.james.jircd.util.ChannelChecks;

/**
 * The JOIN command - joins a channel
 * 
 * @author James
 */
public class Join implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Parse join parameters
		if(msg.getParam(0).equals("0"))
		{
			//Part all channels
			List<Channel> channelsCopy = new ArrayList<Channel>(client.getChannels());
			
			for(Channel channel : channelsCopy)
			{
				channel.part(client, client.id.nick);
			}
		}
		else
		{
			String[] chanStrings = msg.getParam(0).split(",");
			String[] keyStrings;
			
			if(msg.paramCount() >= 2)
			{
				//Split keys
				keyStrings = msg.getParam(1).split(",");
			}
			else
			{
				keyStrings = new String[0];
			}
			
			//Process joins
			for(int i = 0; i < chanStrings.length; ++i)
			{
				processJoin(client, chanStrings[i], keyStrings, i);
			}
		}
	}

	/**
	 * Processes a channel join request 
	 * 
	 * @param client client who's joining
	 * @param chanString channel being joined
	 * @param keyStrings all the keystrings that are searched
	 * @param i join index into keystrings array
	 */
	private static void processJoin(Client client, String chanString, String[] keyStrings, int i)
	{
		//Ensure leading #
		if(chanString.charAt(0) != '#')
		{
			chanString = "#" + chanString;
		}
		
		//Find channel
		Channel channel = Server.getServer().getChannel(chanString);
		
		if(channel == null)
		{
			//Create channel
			channel = Channel.createChannel(chanString);
		}
		else
		{
			ChannelCheckError error;
			
			//Check join
			if(keyStrings.length > i)
			{
				error = ChannelChecks.canJoin(channel, client, keyStrings[i]);
			}
			else
			{
				error = ChannelChecks.canJoin(channel, client, null);	
			}
			
			//Display error
			if(error == ChannelCheckError.JoinAlreadyInChannel)
			{
				//Ignore this channel join
				return;
			}
			else if(error != ChannelCheckError.OK)
			{
				//Print error
				error.sendToClient(channel, client);
				return;
			}
		}
		
		//Join channel
		channel.join(client, true);
	}

	@Override
	public int getMinParameters()
	{
		return 1;
	}

	@Override
	public String getName()
	{
		return "JOIN";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}
}
