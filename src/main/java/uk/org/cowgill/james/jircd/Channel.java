package uk.org.cowgill.james.jircd;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import uk.org.cowgill.james.jircd.util.ModeType;

/*
 * TODO
 * 
 * Note difference between +p and +s
 * ----
 * +p has secret MEMBERSHIP. Channel does not appear on /whois, /who, /names unless your in it
 * +s is a completely secret channel. Channel does not appear on /list or /topic.
 */

/**
 * Represents an IRC channel
 * 
 * @author James
 */
public final class Channel
{
	/**
	 * Avaliable channel modes
	 * 
	 * <p>You are allowed to add ONOFF modes to this (do not add modes requiring parameters)
	 * <p>When adding modes, all existing channels will not have that mode set
	 */
	public static final Map<Character, ModeType> modes;

	static
	{
		//Setup channel modes
		modes = new HashMap<Character, ModeType>();
		modes.put('q', ModeType.MemberList);
		modes.put('a', ModeType.MemberList);
		modes.put('o', ModeType.MemberList);
		modes.put('h', ModeType.MemberList);
		modes.put('v', ModeType.MemberList);
		modes.put('b', ModeType.List);
		modes.put('e', ModeType.List);
		modes.put('I', ModeType.List);
		modes.put('k', ModeType.Param);
		modes.put('k', ModeType.Param);
		modes.put('p', ModeType.OnOff);
		modes.put('s', ModeType.OnOff);
		modes.put('t', ModeType.OnOff);
		modes.put('n', ModeType.OnOff);
		modes.put('m', ModeType.OnOff);
		modes.put('i', ModeType.OnOff);
		modes.put('O', ModeType.OnOff);
	}
	
	/**
	 * Information about when something was last set
	 * 
	 * @author James
	 */
	public static class SetInfo
	{
		private final long time;
		private final String nick;
		
		/**
		 * Creates a new set info object
		 * 
		 * @param nick the client who set the object (null for server's name)
		 */
		public SetInfo(Client client)
		{
			this.time = System.currentTimeMillis();
			
			if(client == null)
			{
				this.nick = Server.getServer().getConfig().serverName;
			}
			else
			{
				this.nick = client.id.nick;
			}
		}
		
		/**
		 * Gets the time the object was set
		 * 
		 * <p>Time is the number of milliseconds since the UNIX Epoch
		 * 
		 * @return time the object was set
		 */
		public long getTime()
		{
			return time;
		}
		
		/**
		 * Gets the nickname who set the object
		 * 
		 * <p>This can be the server's name
		 * 
		 * @return nickname who set the object
		 */
		public String getNick()
		{
			return nick;
		}
	}
	
	//Collection of channel fields
	// These are documented in the relevent getters
	private final String name;
	private final boolean deletable;
	private final long creationTime;
	private String topic;
	private SetInfo topicInfo;
	private long mode;
	private String key;
	private int limit;
	private Map<String, SetInfo> banList = new HashMap<String, SetInfo>();
	private Map<String, SetInfo> banExceptList = new HashMap<String, SetInfo>();
	private Map<String, SetInfo> inviteExceptList = new HashMap<String, SetInfo>();
	private Set<Client> invited = new HashSet<Client>();		//Set of clients invited by ops
	private Map<Client, ChannelMemberMode> members = new HashMap<Client, ChannelMemberMode>();
	
	//Field getters
	
	/**
	 * Gets the name of this channel with the leading #
	 * 
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Gets whether the channel will delete itself when the last client leaves it
	 * 
	 * @return whether the channel is deletable
	 */
	public boolean isDeletable()
	{
		return deletable;
	}

	/**
	 * Gets the time this channel was created
	 * 
	 * <p>Time is the number of milliseconds since the UNIX Epoch
	 * 
	 * @return the creationTime
	 */
	public long getCreationTime()
	{
		return creationTime;
	}

	/**
	 * Gets the topic of the channel
	 * 
	 * <p>If no topic has ever been set, this is null
	 * 
	 * @return the topic
	 */
	public String getTopic()
	{
		return topic;
	}

	/**
	 * Gets the information about the last topic set
	 * 
	 * <p>If no topic has ever been set, this is null
	 * 
	 * @return the topicInfo
	 */
	public SetInfo getTopicInfo()
	{
		return topicInfo;
	}

	/**
	 * Gets the channel key or null if there is no key
	 * 
	 * @return the key
	 */
	public String getKey()
	{
		return key;
	}

	/**
	 * Gets the channel limit or 0 if there is no limit
	 * 
	 * @return the limit
	 */
	public int getLimit()
	{
		return limit;
	}

	/**
	 * Gets the channel ban list
	 * 
	 * <p>The list returned is immutable
	 * 
	 * @return the banList
	 */
	public Map<String, SetInfo> getBanList()
	{
		return Collections.unmodifiableMap(banList);
	}

	/**
	 * Gets the channel ban expeption list
	 * 
	 * <p>The list returned is immutable
	 * 
	 * @return the banExceptList
	 */
	public Map<String, SetInfo> getBanExceptList()
	{
		return Collections.unmodifiableMap(banExceptList);
	}

	/**
	 * Gets the channel invite exception list
	 * 
	 * <p>The list returned is immutable
	 * 
	 * @return the inviteExceptList
	 */
	public Map<String, SetInfo> getInviteExceptList()
	{
		return Collections.unmodifiableMap(inviteExceptList);
	}

	/**
	 * Gets a list of channel members
	 * 
	 * <p>The list returned is immutable
	 * 
	 * @return the members
	 */
	public Map<Client, ChannelMemberMode> getMembers()
	{
		return Collections.unmodifiableMap(members);
	}
	
	//Mode testing
	
	/**
	 * Tests whether a channel mode has been set
	 * 
	 * <p>This method does not work with list modes
	 * 
	 * @param mode the mode to test
	 * @return whether the mode is set
	 */
	public boolean isModeSet(char mode)
	{
		switch(mode)
		{
		case 'l':
			return limit != 0;
			
		case 'k':
			return key != null;
			
		default:
			//Check modes bitset
			if(mode >= 'A' && mode <= 'Z')
			{
				return (this.mode & (1 << ('Z' - mode))) != 0;
			}
			else if(mode >= 'a' && mode <= 'a')
			{
				return (this.mode & ((1 << 32) << ('z' - mode))) != 0;
			}
			else
			{
				//Invalid modes are never set
				return false;
			}
		}
	}
	
	/**
	 * Sets a mode in the mode variable
	 * 
	 * <p>This assumes mode is a valid mode
	 * 
	 * @param mode mode to set
	 */
	private void rawSetMode(char mode)
	{
		if(mode >= 'a')
		{
			this.mode |= (1 << 32) << ('z' - mode);
		}
		else
		{
			this.mode |= 1 << ('Z' - mode);			
		}
	}

	/**
	 * Clears a mode in the mode variable
	 * 
	 * <p>This assumes mode is a valid mode
	 * 
	 * @param mode mode to clear
	 */
	private void rawClearMode(char mode)
	{
		if(mode >= 'a')
		{
			this.mode &= ~((1 << 32) << ('z' - mode));
		}
		else
		{
			this.mode &= ~(1 << ('Z' - mode));			
		}
	}
	
	//Channel creation
	private Channel(String name, boolean deletable)
	{
		//Setup default channel stuff
		this.name = name;
		this.deletable = deletable;
		this.creationTime = System.currentTimeMillis();
		
		//Default mode is +nt
		rawSetMode('n');
		rawSetMode('t');
	}
	
	/**
	 * Creates a new blank channel with the specified name
	 * 
	 * <p>If the channel already exists, null is returned
	 * 
	 * @param name the name of the channel
	 * @return the new channel
	 */
	public static Channel createChannel(String name)
	{
		return createChannel(name, true);
	}
	
	/**
	 * Creates a new blank channel with the specified name
	 * 
	 * <p>If the channel already exists, null is returned
	 * 
	 * @param name the name of the channel
	 * @param deletable if false, when the last client leaves, the channel is not deleted
	 * @return the new channel
	 */
	public static Channel createChannel(String name, boolean deletable)
	{
		//Check whether channel exists
		if(!Server.getServer().channels.containsKey(name))
		{
			//Create channel
			return new Channel(name, deletable);
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Sends this channel a message
	 * 
	 * @param data message to send
	 */
	public void send(Object data)
	{
		send(data, null);
	}
	
	/**
	 * Sends this channel a message
	 * 
	 * @param data message to send
	 * @param except do not send data to this client
	 */
	public void send(Object data, Client except)
	{
		Client.sendTo(members.keySet(), data, except);
	}
	
	//Information Senders
	
	/**
	 * Sends a client the response of a topic request to this channel
	 * @param client client to send topic to
	 */
	public void sendTopic(Client client)
	{
		if(topic == null)
		{
			//No topic set
			Message msg = client.newNickMessage("331");
			msg.appendParam(name);
			msg.appendParam("No topic set");
			client.send(msg);
		}
		else
		{
			//Send channel topic
			Message msg = client.newNickMessage("332");
			msg.appendParam(name);
			msg.appendParam(topic);
			client.send(msg);
	
			//Send channel topic info
			msg = client.newNickMessage("333");
			msg.appendParam(name);
			msg.appendParam(topicInfo.getNick());
			msg.appendParam(String.valueOf(topicInfo.getTime() << 1000));
			client.send(msg);
		}
	}

	/**
	 * Sends a client the response of a names request to this channel
	 * @param client client to send names to
	 */
	public void sendNames(Client client)
	{
		//TODO NAMESX and UHNAMES support
		
		//Construct prefix
		Message namesPrefix = client.newNickMessage("353");
		
		if(isModeSet('s'))
		{
			namesPrefix.appendParam("@");
		}
		else if(isModeSet('p'))
		{
			namesPrefix.appendParam("*");
		}
		else
		{
			namesPrefix.appendParam("=");
		}
		
		namesPrefix.appendParam(name);
		
		//Send up to 8 names per line
		StringBuilder builder = new StringBuilder();
		int namesThisLine = 0;
		Message msg = null;
		
		for(Entry<Client, ChannelMemberMode> entry : members.entrySet())
		{
			//Setup new message
			if(msg == null)
			{
				msg = new Message(namesPrefix);
			}
			else
			{
				builder.append(' ');
			}
			
			//Add name
			builder.append(entry.getValue().toPrefixString(true));
			builder.append(entry.getKey().id.nick);
			namesThisLine++;
			
			//If 8 names, send message
			if(namesThisLine >= 8)
			{
				msg.appendParam(builder.toString());
				client.send(msg);
				
				msg = null;
				namesThisLine = 0;
			}
		}
		
		//Send ending
		if(msg != null)
		{
			client.send(msg);
		}
		
		msg = client.newNickMessage("366");
		msg.appendParam(name);
		msg.appendParam("End of /NAMES list");
		client.send(msg);
	}

	//Channel Actions
	
	/**
	 * Causes a client to join this channel
	 * 
	 * <p>No checks are performed by this method. Do NOT just let anyone use this without checks.
	 * <p>If no-one is on the channel, the client joins with OPS. Otherwise, the user has no extra modes.
	 * 
	 * @param client Client to add
	 * @param banChecked set to true if the ban lists have been checked and this client is not banned
	 * @return true on sucess, false if the client is already on the channel
	 */
	public boolean join(Client client, boolean banChecked)
	{
		//Check for member
		if(members.containsKey(client))
		{
			return false;
		}
		 
		//Setup mode
		ChannelMemberMode chanMode = new ChannelMemberMode();
		if(banChecked)
		{
			chanMode.setMode(ChannelMemberMode.BANCHECKED);
		}
		
		if(members.size() == 0)
		{
			chanMode.setMode(ChannelMemberMode.OP);
		}
		
		//Add member
		members.put(client, chanMode);
		if(invited.remove(client))
		{
			client.invited.remove(this);
		}
		
		//Notify others
		Message msg = new Message("JOIN", client);
		msg.appendParam(this.name);
		send(msg);
		
		//Send topic
		if(topic != null)
		{
			sendTopic(client);
		}
		
		//Send channel names
		sendNames(client);
		
		return true;
	}
	
	/**
	 * Causes a client to leave this channel
	 * 
	 * @param client the client who's leaving
	 * @param partMsg the part / quit message of the client
	 * @param sendToSelf whether to send the quit message to the client
	 * @return false if the client is not on the channel
	 */
	boolean part(Client client, Object partMsg, boolean sendToSelf)
	{
		//Check for member
		if(members.containsKey(client))
		{
			//Send message
			send(partMsg, (sendToSelf ? null : client));
			
			//Update lists
			members.remove(client);
			client.channels.remove(this);
			
			//If channel is empty, delete
			if(members.isEmpty() && deletable)
			{
				Server.getServer().channels.remove(name);
			}
			
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Causes a client to part this channel
	 * 
	 * @param client client parting channel
	 * @param partMsg part message
	 * @return false if the client is not on the channel
	 */
	public boolean part(Client client, String partMsg)
	{
		//Construct message
		Message msg = new Message("PART", client);
		msg.appendParam(name);
		msg.appendParam(partMsg);
		
		//Forward
		return part(client, msg, true);
	}
	
	/**
	 * Speaks a message into the channel
	 * 
	 * @param client client who spoke the message (or null for server)
	 * @param command command message was sent with (PRIVMSG or NOTICE)
	 * @param data data to send
	 */
	public void speak(Client client, String command, String data)
	{
		Message msg = new Message(command, client);
		msg.appendParam(name);
		msg.appendParam(data);
		
		send(msg, client);
	}
	
	/**
	 * Kicks a client from this channel
	 * 
	 * @param kicker client who is kicking
	 * @param kicked client to be kicked
	 * @param kickMsg kick message
	 * @return true is the client was kicked, false if kicked is not in the channel
	 */
	public boolean kick(Client kicker, Client kicked, String kickMsg)
	{
		//Construct message
		Message msg = new Message("KICK", kicker);
		msg.appendParam(name);
		msg.appendParam(kicked.id.toString());
		msg.appendParam(kickMsg);
		
		//Forward
		return part(kicked, msg, true);
	}
	
	/**
	 * Invites a client into a channel
	 * 
	 * @param inviter client giving the invitation
	 * @param invitedClient client to be invited
	 */
	public void invite(Client inviter, Client invitedClient)
	{
		//Add to invited list if inviter is an op
		ChannelMemberMode inviterMode = members.get(inviter);
		Message msg;
		
		if(inviterMode != null && inviterMode.getMode() >= ChannelMemberMode.OP)
		{
			//Add to invited
			if(this.invited.add(invitedClient))
			{
				invitedClient.invited.add(this);
			}
			
			//Notify other opers
			msg = Message.newMessageFromServer("NOTICE");
			msg.appendParam("@" + name);
			msg.appendParam(inviter.id.nick + " invited " + invitedClient.id.nick + " into the channel");
			
			for(Entry<Client, ChannelMemberMode> entry : members.entrySet())
			{
				//Is op?
				if(entry.getValue().getMode() >= ChannelMemberMode.OP)
				{
					entry.getKey().send(msg);
				}
			}
		}
		
		//Notify relevant people		
		if(inviter != null)
		{
			msg = inviter.newNickMessage("341");
			msg.appendParam(invitedClient.id.nick);
			msg.appendParam(name);
			inviter.send(msg);
		}
		
		msg = new Message("INVITE", inviter);
		msg.appendParam(invitedClient.id.nick);
		msg.appendParam(name);
		invitedClient.send(msg);
	}
	
	/**
	 * Sets the channel topic
	 * 
	 * @param setter client who set the topic
	 * @param topic the new topic
	 */
	public void setTopic(Client setter, String topic)
	{
		//Update topic and info
		this.topic = topic;
		this.topicInfo = new SetInfo(setter);
		
		//Tell everyone
		Message msg = new Message("TOPIC", setter);
		msg.appendParam(name);
		msg.appendParam(topic);
		send(msg);
	}
	
	//Set mode
	
	/**
	 * The reason why a setMode request failed
	 * 
	 * @author James
	 */
	public enum SetModeFailReason
	{
		/**
		 * The mode has successfully been set
		 */
		OK,
		
		/**
		 * The parameter in a +l request is not a number
		 */
		InvalidNumber,
		
		/**
		 * The mode has already been set / unset
		 */
		AlreadySet,
		
		/**
		 * In a client mode change, the client does not exist
		 */
		InvalidClient,

		/**
		 * In a client mode change, the client is not a member
		 */
		ClientNotMember,
	}
	
	/**
	 * Processes a set mode for list request
	 * 
	 * @param setter mode setting client
	 * @param add whether the mode shuold be added
	 * @param list the list to modify
	 * @param param mode parameter
	 * @param msg output message
	 * @return the fail reason
	 */
	private SetModeFailReason processList(Client setter, boolean add, Map<String, SetInfo> list,
			Object param, Message msg)
	{
		//Sanitize param
		String entry = IRCMask.sanitize(param.toString());
		
		//Process list
		if(add)
		{
			if(list.containsKey(entry))
			{
				return SetModeFailReason.AlreadySet;
			}
			
			list.put(entry, new SetInfo(setter));
		}
		else
		{
			if(list.remove(entry) == null)
			{
				return SetModeFailReason.AlreadySet;
			}
		}
		
		msg.appendParam(entry);
		return SetModeFailReason.OK;
	}
	
	/**
	 * Processes a member mode change
	 * 
	 * @param add whether to add the mode
	 * @param modeVal mode integer value
	 * @param param client to change
	 * @param msg message to modify
	 * @return the fail reason
	 */
	private SetModeFailReason processMember(boolean add, int modeVal, Object param, Message msg)
	{
		//Lookup client
		Client client;
		
		if(param instanceof Client)
		{
			client = (Client) param;
		}
		else
		{
			//Lookup client
			client = Server.getServer().clientsByNick.get(param.toString());
			
			if(client == null)
			{
				return SetModeFailReason.InvalidClient;
			}
		}
		
		//Find client in members list
		ChannelMemberMode mode = members.get(client);
		
		//Check membership
		if(mode == null)
		{
			return SetModeFailReason.ClientNotMember;
		}
		
		//Change mode
		if(add)
		{
			if((mode.getMode() & modeVal) == 0)
			{
				mode.setMode(modeVal);
			}
			else
			{
				return SetModeFailReason.AlreadySet;
			}
		}
		else
		{
			if((mode.getMode() & modeVal) != 0)
			{
				mode.clearMode(modeVal);
			}
			else
			{
				return SetModeFailReason.AlreadySet;
			}
		}
		
		msg.appendParam(client.id.nick);
		return SetModeFailReason.OK;
	}
	
	/**
	 * Sets a channel's mode and tells the channel about it
	 * 
	 * @param setter client who set the mode (or null if server set it)
	 * @param add whether the mode is being added or deleted
	 * @param mode mode to set
	 * @param param mode parameter (can be integer for +l or client for +vhoaq)
	 */
	public SetModeFailReason setMode(Client setter, boolean add, char mode, Object param)
	{
		SetModeFailReason error = SetModeFailReason.OK;
		
		//Setup mode message
		Message msg = new Message("MODE", setter);
		msg.appendParam(name);

		if(mode != 'p' && mode != 's')
		{
			if(add)
			{
				msg.appendParam("+" + mode);
			}
			else
			{
				msg.appendParam("-" + mode);
			}
		}
		
		//Check for special modes
		switch(mode)
		{
		case 'l':
			//Channel limit
			if(add)
			{
				//Set limit from param
				int newLimit;
				
				if(param instanceof Integer)
				{
					newLimit = ((Integer) param).intValue();
				}
				else
				{
					try
					{
						newLimit = Integer.parseInt(param.toString());
					}
					catch(NumberFormatException e)
					{
						//Number parse error
						return SetModeFailReason.InvalidNumber;
					}
				}
				
				//Limit must be >= 0
				if(newLimit < 0)
				{
					return SetModeFailReason.InvalidNumber;
				}
				
				//Set limit
				this.limit = newLimit;
				msg.appendParam(Integer.toString(newLimit));
			}
			else
			{
				//Limit is unset to 0
				this.limit = 0;
			}
			
			break;
		
		case 'k':
			//Set key
			if(add)
			{
				this.key = param.toString();
				msg.appendParam(this.key);
			}
			else
			{
				this.key = null;
			}
			
			break;
			
		case 'b':
			//Set lists
			error = processList(setter, add, this.banList, param, msg);
			break;
			
		case 'e':
			error = processList(setter, add, this.banExceptList, param, msg);
			break;
			
		case 'I':
			error = processList(setter, add, this.inviteExceptList, param, msg);
			break;
			
		case 'v':
			error = processMember(add, ChannelMemberMode.VOICE, param, msg);
			break;
			
		case 'h':
			error = processMember(add, ChannelMemberMode.HALFOP, param, msg);
			break;
			
		case 'o':
			error = processMember(add, ChannelMemberMode.OP, param, msg);
			break;
			
		case 'a':
			error = processMember(add, ChannelMemberMode.ADMIN, param, msg);
			break;
			
		case 'q':
			error = processMember(add, ChannelMemberMode.OWNER, param, msg);
			break;
			
		case 'p':
		case 's':
			//Remove the other when adding
			if(add)
			{
				String modeStr = "+" + mode;
				
				if(mode == 'p')
				{
					//Remove secret mode
					rawSetMode('p');
					
					if(isModeSet('s'))
					{
						rawClearMode('s');
						modeStr += "-s";
					}
				}
				else
				{
					//Remove private mode
					rawSetMode('s');
					
					if(isModeSet('p'))
					{
						rawClearMode('p');
						modeStr += "-p";
					}
				}
				
				msg.appendParam(modeStr);
			}
			else
			{
				//Clear mode
				rawClearMode(mode);
				msg.appendParam("-" + mode);
			}
			break;
			
		default:
			//Standard mode
			if(add)
			{
				rawSetMode(mode);
			}
			else
			{
				rawClearMode(mode);
			}
			break;
		}
		
		//Send mode message
		if(error == SetModeFailReason.OK)
		{
			send(msg);
		}
		
		return error;
	}
}