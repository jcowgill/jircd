package uk.org.cowgill.james.jircd.commands;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.ModuleManager;
import uk.org.cowgill.james.jircd.Server;

/**
 * The STATS command - displays server statistics
 * 
 * @author James
 */
public class Stats implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Perform requested operation
		if(msg.paramCount() > 0 && msg.getParam(0).length() == 1)
		{
			//Only very basic stuff here i'm afraid
			switch(Character.toLowerCase(msg.getParam(0).charAt(0)))
			{
			case 'm':
				//Get command statistics
				Map<String, ModuleManager.CommandInfo> commands =
						Server.getServer().getModuleManager().getCommands();
				
				//Generate messages in sorted order
				TreeMap<String, Message> messages = new TreeMap<String, Message>();
				for(Entry<String, ModuleManager.CommandInfo> entry : commands.entrySet())
				{
					if(entry.getValue().getTimesRun() > 0)
					{
						//Add to messages
						messages.put(entry.getKey(),
								client.newNickMessage("212").
									appendParam(entry.getKey()).
									appendParam(Integer.toString(entry.getValue().getTimesRun())));
					}
				}
				
				//Send messages
				for(Message outMsg : messages.values())
				{
					client.send(outMsg);
				}
				
				break;
				
			case 'u':
				//Get uptime
				int upTime = (int) ((Server.getServer().creationTime.getTime() -
												System.currentTimeMillis()) / 1000);
				int secs = upTime % 60;
				int mins = (upTime / 60) % 60;
				int hours = (upTime / 3600) % 24;
				int days = upTime / (24 * 3600);
				
				client.send(client.newNickMessage("242").appendParam(
						String.format("Server Up %d day%s %d:%d:%d",
								days,
								days != 1 ? "s" : "",
								hours,
								mins,
								secs)));
				
				break;
			}
		}
		
		//End of /STATS
		client.send(client.newNickMessage("219").
				appendParam(msg.getParam(0)).appendParam("End of /STATS report"));
	}
	
	@Override
	public int getMinParameters()
	{
		return 0;
	}

	@Override
	public String getName()
	{
		return "STATS";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}
}
