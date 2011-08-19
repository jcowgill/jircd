package uk.org.cowgill.james.jircd.commands;

import java.util.Set;

import uk.org.cowgill.james.jircd.Channel;
import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.Permissions;
import uk.org.cowgill.james.jircd.Server;

//TODO LIST extensions for lots of channels
// and list windcards

/**
 * The LIST command - lists all the channels on the server
 * 
 * @author James
 */
public class List implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		int privateMembers = 0;
		Set<Channel> clientChannels = client.getChannels();
		
		//All seeing?
		boolean allSeeing = client.hasPermission(Permissions.seeAllChannels);
		
		//Prepare reply
		String reply = Message.newStringFromServer("322") + " " + client.id.nick + " ";
		
		//Process channel list
		for(Channel channel : Server.getServer().getChannels())
		{
			//Can we see this channel?
			if(!allSeeing && (channel.isModeSet('p') || channel.isModeSet('s')) &&
					!clientChannels.contains(channel))
			{
				//Do not show
				if(channel.isModeSet('p'))
				{
					//Add members to private category
					privateMembers += channel.getMembers().size();
				}
				
				continue;
			}
			
			//Send channel
			client.send(reply + channel.getName() + " " + channel.getMembers().size() +
					" :" + channel.getTopic());
		}
		
		//Send private members
		if(privateMembers > 0)
		{
			client.send(reply + "* " + privateMembers + " :");
		}
		
		//Send end of list
		client.send(client.newNickMessage("323").appendParam("End of /LIST"));
	}

	@Override
	public int getMinParameters()
	{
		return 0;
	}

	@Override
	public String getName()
	{
		return "LIST";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}
}
