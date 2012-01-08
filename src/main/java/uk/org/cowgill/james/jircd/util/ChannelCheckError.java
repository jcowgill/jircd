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

import uk.org.cowgill.james.jircd.Channel;
import uk.org.cowgill.james.jircd.Client;

/**
 * Enumeration containing all the errors used by the ChannelChecks class
 * 
 * @author James
 */
public enum ChannelCheckError
{
	OK(null, null),
	
	GeneralNotAnOp("482", "You're not a channel operator"),
	GeneralNotInChannel("442", "You're not on that channel"),
	
	JoinAlreadyInChannel(null, null),
	JoinBanned("474", "Cannot join channel (+b)"),
	JoinChannelFull("471", "Cannot join channel (+l)"),
	JoinInvalidKey("475", "Cannot join channel (+k)"),
	JoinInviteOnly("473", "Cannot join channel (+i)"),
	JoinOpersOnly("520", "Cannot join channel (+O)"),
	JoinTooManyChannels("405", "You have joined too many channels"),
	
	SpeakBanned("404", "You are banned (+b)"),
	SpeakModerated("404", "You need voice (+v)"),
	SpeakNotInChannel("404", "No external channel messages (+n)"),
	
	//Kick can produce GeneralNotAnOp
	KickOtherNotInChannel("441", "They arn't on that channel"),
	
	//Invite can produce GeneralNotAnOp and GeneralNotInChannel
	InviteAlreadyInChannel("443", "is already on that channel"),
	
	//SetTopic can produce GeneralNotAnOp and GeneralNotInChannel
	
	//SetMode can produce GeneralNotAnOp
	SetModeHalfOpDeny("460", "Half-ops cannot set that mode"),
	SetModeOwnerOnly("499", "You're not channel owner"),
	SetModeNotAnIrcOp("481", "Only IRC Operators can set mode O"),
	SetModeListFull("478", "Channel list is full");
	
	private final String numeric;
	private final String text;

	private ChannelCheckError(String numeric, String text)
	{
		this.numeric = numeric;
		this.text = text;
	}

	/**
	 * Returns the IRC numeric associated with this error
	 * 
	 * <p>This can be null if there is no message associated with this error
	 * 
	 * @return the number as a string
	 */
	public String getNumeric()
	{
		return numeric;
	}
	
	/**
	 * Returns the textual description associated with this error
	 * 
	 * <p>This can be null if there is no message associated with this error
	 * 
	 * @return the description
	 */
	public String getText()
	{
		return text;
	}
	
	/**
	 * Sends this error to a client
	 * 
	 * @param channel channel the error occured on
	 * @param client client to send the error to
	 */
	public void sendToClient(Channel channel, Client client)
	{
		if(numeric != null)
		{
			client.send(client.newNickMessage(numeric).appendParam(channel.getName()).appendParam(text));
		}
	}
	
	/**
	 * Sends this error to a client using a string channel
	 * 
	 * @param channel channel the error occured on
	 * @param client client to send the error to
	 */
	public void sendToClient(String channel, Client client)
	{
		if(numeric != null)
		{
			client.send(client.newNickMessage(numeric).appendParam(channel).appendParam(text));
		}
	}
}
