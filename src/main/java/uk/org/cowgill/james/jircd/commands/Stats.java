/*
   Copyright 2011 James Cowgill

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
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
		char statsCmd;
		
		//Perform requested operation
		if(msg.paramCount() > 0 && msg.getParam(0).length() == 1)
		{
			statsCmd = Character.toLowerCase(msg.getParam(0).charAt(0));
			
			//Only very basic stuff here i'm afraid
			switch(statsCmd)
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
				int upTime = (int) ((System.currentTimeMillis() - 
										Server.getServer().creationTime.getTime()) / 1000);
				
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
		else
		{
			statsCmd = '*';
		}
		
		//End of /STATS
		client.send(client.newNickMessage("219").
				appendParam(Character.toString(statsCmd)).appendParam("End of /STATS report"));
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
