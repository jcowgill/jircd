package uk.org.cowgill.james.jircd.commands;

import uk.org.cowgill.james.jircd.Channel;
import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.Server;
import uk.org.cowgill.james.jircd.ServerISupport;
import uk.org.cowgill.james.jircd.util.ChannelCheckError;
import uk.org.cowgill.james.jircd.util.ChannelChecks;

/**
 * The TOPIC command - views or changes a channel topic
 * 
 * @author James
 */
public class Topic implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Lookup channel
		ChannelCheckError checkError;		
		Channel channel = Server.getServer().getChannel(msg.getParam(0));
		
		if(channel == null)
		{
			ChannelCheckError.GeneralNotInChannel.sendToClient(msg.getParam(0), client);
		}
		else
		{
			//View or set topic?
			if(msg.paramCount() == 1)
			{
				//View topic
				if(ChannelChecks.canGetTopic(channel, client))
				{
					//Show topic
					String topic = channel.getTopic();
					
					if(topic == null)
					{
						client.send(client.newNickMessage("331").appendParam(channel.getName()).
								appendParam("No topic is set"));
					}
					else
					{
						Channel.SetInfo topicInfo = channel.getTopicInfo();
						String time = Long.toString(topicInfo.getTime() / 1000);
						
						//Send topic
						client.send(client.newNickMessage("332").appendParam(channel.getName()).
								appendParam(topic));

						//Send topic information
						client.send(client.newNickMessage("333").
								appendParam(channel.getName()).
								appendParam(topicInfo.getNick()).
								appendParam(time));
					}
					
					return;
				}
				else
				{
					checkError = ChannelCheckError.GeneralNotInChannel;
				}
			}
			else
			{
				//Set topic
				// Validate topic
				String topic = msg.getParam(1);
				
				if(topic.length() > ServerISupport.TOPICLEN)
				{
					return;
				}
				
				//Can set topic?				
				checkError = ChannelChecks.canSetTopic(channel, client);
				
				if(checkError == ChannelCheckError.OK)
				{
					//Set the topic
					channel.setTopic(client, msg.getParam(1));
					return;
				}
			}
			
			//If we're here, we have an error
			checkError.sendToClient(channel, client);
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
		return "TOPIC";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}
}
