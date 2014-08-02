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
package uk.org.cowgill.james.jircd.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.org.cowgill.james.jircd.Channel;
import uk.org.cowgill.james.jircd.Client;

/**
 * Class which can parse a string passed to the MODE command
 *
 * @author James
 */
public class ModesParser
{
	private Map<Character, ModeType> modes;

	/**
	 * Information about a mode change
	 *
	 * @author James
	 */
	public static class ChangeInfo
	{
		public boolean add;
		public char flag;
		public String param;
	}

	/**
	 * The collection of modes to change
	 */
	public Collection<ChangeInfo> toChange;

	/**
	 * The mode which should be listed (or null if no mode should be listed)
	 */
	public Character toList;

	/**
	 * True if the user / channel mode should just be printed
	 */
	public boolean printMode;

	/**
	 * Creates a new modes parser with the specified available modes
	 *
	 * @param modes the available modes
	 * 	<p>User and Channel modes can be found in the ServerISupport class
	 */
	public ModesParser(Map<Character, ModeType> modes)
	{
		this.modes = modes;
	}

	/**
	 * Creates a new modes parser with modes from another parser
	 *
	 * @param parser the parser to get available modes from
	 */
	public ModesParser(ModesParser parser)
	{
		this.modes = parser.modes;
	}

	/**
	 * Resets the parser
	 */
	public void reset()
	{
		printMode = false;
		toList = null;
		toChange = null;
	}

	/**
	 * Sets all the modes in this mode parser
	 *
	 * @param channel channel to set modes on
	 * @param setter client who set the modes
	 */
	public void setModes(Channel channel, Client setter)
	{
		for(ChangeInfo entry : toChange)
		{
			channel.setMode(setter, entry.add, entry.flag, entry.param);
		}
	}

	/**
	 * Parses the given collection of parameters into the mode parser
	 *
	 * <p>The result of the parsing is stored within the ModeParser and the results are found using the get methods
	 * <p>This method will never fail. If any invalid modes occur, they are ignored like the irc MODE command
	 *
	 * @param params List of parameters to parse
	 */
	public void parse(List<String> params)
	{
		//Manually reset parser
		printMode = false;
		toList = null;
		toChange = new ArrayList<ChangeInfo>();

		//Handle special mode strings
		switch(params.size())
		{
		case 0:
			//No params
			printMode = true;
			return;

		case 1:
			//If 1 param is a list, print the list
			String firstParam = params.get(0);

			switch(firstParam.length())
			{
			case 2:
				//If first is + remove it
				if(firstParam.charAt(0) == '+')
				{
					firstParam = firstParam.substring(1);
				}

			case 1:
				//If character is a list, print list
				if(modes.get(firstParam.charAt(0)) == ModeType.List)
				{
					toList = firstParam.charAt(0);
					return;
				}
			}

			//Otherwise fall though to mode changing
		}

		//Mode changing
		boolean isAdding = true;
		LinkedList<ChangeInfo> paramQueue = new LinkedList<ChangeInfo>();

		for(String item : params)
		{
			//If there are no characters on the queue, process new modes
			if(paramQueue.isEmpty())
			{
				for(int i = 0; i < item.length(); i++)
				{
					char c = item.charAt(i);

					//Check character
					if(c == '+')
					{
						isAdding = true;
					}
					else if(c == '-')
					{
						isAdding = false;
					}
					else
					{
						//Get mode type
						ModeType type = modes.get(c);
						if(type != null)
						{
							//Set basic information
							ChangeInfo info = new ChangeInfo();
							info.add = isAdding;
							info.flag = c;

							//Check mode type
							switch(type)
							{
							case OnOff:
								//Add to changes
								toChange.add(info);
								break;

							case Param:
								//Add to list if adding
								if(isAdding || c == 'k')		//This k is a hack
								{
									paramQueue.offer(info);
								}
								else
								{
									toChange.add(info);
								}
								break;

							case List:
							case MemberList:
								//Add to param queue
								paramQueue.offer(info);
								break;
							}
						}
					}
				}
			}
			else
			{
				//Use this as a param
				ChangeInfo info = paramQueue.poll();
				info.param = item;
				toChange.add(info);
			}
		}

		//Hack to allow -k with params
		if(!paramQueue.isEmpty())
		{
			ChangeInfo info = paramQueue.poll();
			if(info.flag == 'k' && !info.add)
			{
				toChange.add(info);
			}
		}
	}
}
