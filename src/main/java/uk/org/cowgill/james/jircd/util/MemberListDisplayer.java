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

import java.util.Set;
import java.util.Map.Entry;

import uk.org.cowgill.james.jircd.Channel;
import uk.org.cowgill.james.jircd.ChannelMemberMode;
import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Permissions;

/**
 * Class which enumerates all the members in channel which another client can see
 * 
 * @author james
 */
public final class MemberListDisplayer
{
	/**
	 * Interface which member list results are posted to
	 * 
	 * @author james
	 */
	public interface Executer
	{
		public void displayMember(Client client, Channel channel, Client other, ChannelMemberMode mode);
	}
	
	/**
	 * Lists the members of a channel which the client can see
	 * 
	 * @param client client who is seeing the channel
	 * @param channel the channel being listed
	 * @param executer the executer the list is posted to
	 */
	public static void listChannel(Client client, Channel channel, Executer executer)
	{
		//Can see channel?
		boolean allSeeing = (channel.lookupMember(client) != null) ||
								client.hasPermission(Permissions.seeInvisible);
		
		//Send replies
		for(Entry<Client, ChannelMemberMode> other : channel.getMembers().entrySet())
		{
			//Can see client?
			if(allSeeing || findCommonChannel(client, other.getKey()) != null)
			{
				//Send message
				executer.displayMember(client, channel, other.getKey(), other.getValue());
			}
		}
	}

	/**
	 * Attempts to find a common channel between 2 clients
	 * 
	 * @param clientA first client
	 * @param clientB second client
	 * @return the common channel or null if there is no common channel
	 */
	public static Channel findCommonChannel(Client clientA, Client clientB)
	{
		Set<Channel> chansA = clientA.getChannels();
		Set<Channel> chansB = clientB.getChannels();
		
		//Swap so clientB has most channels
		if(chansA.size() > chansB.size())
		{
			Set<Channel> tmp = chansA;
			chansB = chansA;
			chansA = tmp;
		}
		
		//Search for common channels
		for(Channel channel : chansA)
		{
			if(chansB.contains(channel))
			{
				return channel;
			}
		}
		
		return null;
	}
	
	private MemberListDisplayer()
	{
	}
}
