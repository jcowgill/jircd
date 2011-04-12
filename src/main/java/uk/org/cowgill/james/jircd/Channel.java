package uk.org.cowgill.james.jircd;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.org.cowgill.james.jircd.util.ModeType;

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
		 * @param time time the object was set
		 * @param nick the nickname who set the object (can be server's name)
		 */
		public SetInfo(long time, String nick)
		{
			this.time = time;
			this.nick = nick;
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
	private Set<Client> invited = new HashSet<Client>();
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
	
	//Channel creation
	private Channel(String name, boolean deletable)
	{
		//Setup default channel stuff
		this.name = name;
		this.deletable = deletable;
		this.creationTime = System.currentTimeMillis();
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
	
	/**
	 * Causes a client to join this channel with the specified mode
	 * 
	 * <p>No checks are performed by this method. Do NOT just let anyone use this without checks.
	 * <p>If the mode passed has BANCHECKED set, the banned flag is used
	 * 
	 * @param client Client to add
	 * @param mode Mode to add with
	 * @return true on sucess, false if the client is already on the channel
	 */
	public boolean join(Client client, int mode)
	{
		//Check for member
		if(members.containsKey(client))
		{
			return false;
		}
		 
		//Setup mode
		ChannelMemberMode chanMode = new ChannelMemberMode();
		chanMode.setAllModes(mode);
		chanMode.clearMode(ChannelMemberMode.BANNED);
		
		//TODO notify others about the mode change
		//TODO send topic, names, mode to caller
		
		//Add member
		members.put(client, chanMode);
		
		//Notify others
		Message msg = new Message("JOIN", client);
		msg.appendParam(this.name);
		send(msg);
		
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
	 * @param client client who spoke the message
	 * @param command command message was sent with (PRIVMSG or NOTICE)
	 * @param data data to send
	 */
	public void speak(Client client, String command, String data)
	{
		Message msg;
		String origin;
		
		if(client == null)
		{
			origin = Server.getServer().getConfig().serverName;
		}
		else
		{
			origin = client.id.toString();
		}
		
		msg = new Message(command, origin);
		msg.appendParam(name);
		msg.appendParam(data);
		
		send(msg, client);
	}
	
	/*
	 * TODO list
	 * ------
	 * 
	 * KICK
	 * INVITE
	 * TOPIC
	 * PRIVMSG / NOTICE
	 * SET MODE (onoff, params, lists, member lists)
	 */
}
