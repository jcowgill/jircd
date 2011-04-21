package uk.org.cowgill.james.jircd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.org.cowgill.james.jircd.util.ModesParser;

/**
 * Represents a client in the server
 * 
 * Clients are users on the server with a nickname, modes and can join channels
 * 
 * This could be a remote client or a local servlet
 * 
 * @author James
 */
public abstract class Client
{
	private static Logger logger = Logger.getLogger(Client.class);
	
	/**
	 * List of clients to be closed when close queue is processed
	 */
	private static ArrayList<Client> queuedClosures = new ArrayList<Client>();
	
	/**
	 * Reason for this client's closure
	 */
	private String queuedCloseReason = null;

	/**
	 * The client's id
	 */
	public IRCMask id;
	
	/**
	 * Set of joined channels
	 */
	Set<Channel> channels = new HashSet<Channel>();
	
	/**
	 * Set of channels you've been invited
	 */
	Set<Channel> invited = new HashSet<Channel>();
	
	/**
	 * Flags used to see what parts of the registration process has been completed
	 * 
	 * @see RegistrationFlags
	 */
	private int registrationFlags;
	
	/**
	 * This client's IRC user mode
	 */
	private long mode;
	
	/**
	 * True if client is closed
	 */
	private boolean closed = false;

	//------------------------------------------------

	/**
	 * Creates a new client with a blank id and adds it to global collections
	 */
	public Client()
	{
		this(new IRCMask());
	}

	/**
	 * Creates a new client and adds it to global collections
	 * 
	 * @param id the IRCMask representing this client's id
	 */
	public Client(IRCMask id)
	{
		//Set id
		this.id = id;
		
		//Add to global collections
		Server.getServer().clients.add(this);
	}
	
	
	/**
	 * Gets weather this client has been fully registered
	 * @return true if this client has been fully registered
	 */
	public boolean isRegistered()
	{
		return (~registrationFlags & RegistrationFlags.AllFlags) == 0;
	}
	
	/**
	 * Sets a set of registration flags
	 * 
	 * If the client is already registered, this doesn't do anything useful
	 * 
	 * @param flags flags to set
	 */
	public void setRegistrationFlag(int flags)
	{
		registrationFlags |= flags;
	}
	
	/**
	 * Event which should be fired after all registration information has been set
	 */
	protected void registeredEvent()
	{
		//Validate registration
		if(registrationFlags != (RegistrationFlags.AllFlags - RegistrationFlags.RegComplete))
		{
			return;
		}

		Message msg;
		Server server = Server.getServer();
		Config config = server.getConfig();
		
		// * Check nick is not registered
		if(server.clientsByNick.containsKey(id.nick))
		{
			msg = Message.newMessageFromServer("433");
			msg.appendParam("*");
			msg.appendParam(id.nick);
			msg.appendParam("Nickname already in use");
			send(msg);
			
			id.nick = null;			
			registrationFlags &= ~ RegistrationFlags.NickSet;
			return;
		}
		
		// * Check bans
		for(Config.Ban ban : config.banNick)
		{
			if(IRCMask.wildcardCompare(id.nick, ban.mask))
			{
				//Banned
				msg = Message.newMessageFromServer("432");
				msg.appendParam("*");
				msg.appendParam(id.nick);
				msg.appendParam("Nickname banned: " + ban.reason);
				send(msg);
				
				id.nick = null;			
				registrationFlags &= ~ RegistrationFlags.NickSet;
				return;
			}
		}

		for(Config.Ban ban : config.banUserHost)
		{
			if(IRCMask.wildcardCompare(id.user + "@" + id.host, ban.mask))
			{
				//Banned
				msg = newNickMessage("465");
				msg.appendParam("Banned: " + ban.reason);
				send(msg);
				
				close("Banned");
				return;
			}
		}
		
		// * Check accept lines
		Config.Accept myAcceptLine = null;
		
		for(Config.Accept accept : config.accepts)
		{
			if(IRCMask.wildcardCompare(id.user + "@" + id.host, accept.hostMask) ||
					IRCMask.wildcardCompare(this.getIpAddress(), accept.ipMask))
			{
				//Accept using this line
				myAcceptLine = accept;
				break;
			}
		}
		
		if(myAcceptLine == null)
		{
			msg = newNickMessage("465");
			msg.appendParam("No accept lines for your host");
			send(msg);
			
			close("No accept lines for your host");
			return;
		}
		
		// * Check max ip clones
		if(isRemote() && !server.ipClonesIncrement(getIpAddress(), myAcceptLine.maxClones))
		{
			msg = newNickMessage("465");
			msg.appendParam("Too many connections from your host");
			send(msg);
			
			close("Too many connections from your host");
			return;
		}
		
		// * Change default connection class
		if(!this.changeClass(myAcceptLine.classLine, true))
		{
			msg = newNickMessage("465");
			msg.appendParam("The server is full");
			send(msg);
			
			close("The server is full");
			return;
		}
		
		// * Add to global nick arrays
		server.clientsByNick.put(id.nick, this);
		
		// * Update peek users
		int clientCount = server.getClientCount();
		if(clientCount > server.peekClients)
		{
			server.peekClients = clientCount;
		}
		
		//Display welcome messages
		send(this.newNickMessage("001").appendParam("Welcome to the Internet Relay Network " + id.toString()));
		send(this.newNickMessage("002").appendParam("Your host is " + config.serverName +
				" running version " + Server.VERSION_STR));
		send(this.newNickMessage("003").appendParam("This server was created " + server.creationTime));
		server.getISupport().sendISupportMsgs(this);		//Sends 004 and 005
		
		// * Display LUSERS, MOTD and MODE
		ModuleManager moduleMan = server.getModuleManager();
		moduleMan.executeCommand(this, new Message("LUSERS"));
		moduleMan.executeCommand(this, new Message("MOTD"));
		
		if(this.mode != 0)
		{
			send(new Message("MODE", this).appendParam(id.nick).appendParam(ModesParser.getModeString(mode)));
		}
	}
	
	/**
	 * Requests that this client be closed
	 * 
	 * @param quitMsg the string told to other users about why this client is exiting
	 */
	public final void close(String quitMsg)
	{
		//Send info + close client
		if(!this.closeForShutdown(quitMsg))
		{
			return;
		}		

		//Generate client collection to send to
		HashSet<Client> toSendTo = new HashSet<Client>();
		Set<Client> chanSet;

		for(Channel channel : this.channels)
		{
			//Part channel
			chanSet = channel.partForQuit(this);
			
			//Organise sending
			if(chanSet != null)
			{
				toSendTo.addAll(chanSet);
			}
		}

		//Send notifications
		sendTo(toSendTo, new Message("QUIT", this).appendParam(quitMsg));

		//Remove Any Channel Invites
		for (Channel invite : invited)
		{
			invite.invited.remove(this);
		}
		
		//Remove nick from global nick array
		Server server = Server.getServer();
		
		if (isRegistered())
		{
			//Remove from clients by nick
			server.clientsByNick.remove(id.nick);
			
			//Ip Clone check
			if(isRemote())
			{
				server.ipClonesDecrement(getIpAddress());
			}
		}

		//Remove from global array + operator cache
		server.operators.remove(this);
		server.clients.remove(this);
	}
	
	/**
	 * Closes a client connection but does not bother with freeing resources
	 * 
	 * @param quitMsg quit message
	 * @return whether the close was successful
	 */
	boolean closeForShutdown(String quitMsg)
	{
		//Shield from multiple closures
		if (closed)
		{
			return false;
		}
		closed = true;

		//Send Notification To Client (if it isn't a servlet)
		if (isRemote())
		{
			if(quitMsg == null || quitMsg.length() == 0)
			{
				send(Message.newStringFromServer("ERROR :Closing Link"));
			}
			else
			{
				send(Message.newStringFromServer("ERROR :Closing Link - ") + quitMsg);
			}
		}

		//Close Socket
		closed = this.rawClose();
		return closed;
	}
	
	/**
	 * Sets the nickname of this client
	 * 
	 * <p>This does not perform any checks whether the user is allowed to change nickname
	 * 
	 * @param nick new nickname
	 * @return false if the nick is in use
	 */
	public boolean setNick(String nick)
	{
		//Check for same nick
		if(nick.equalsIgnoreCase(id.nick))
		{
			return true;
		}
		
		//Check whether nick is in use
		Server server = Server.getServer();
		if(server.clientsByNick.containsKey(nick))
		{
			return false;
		}
		
		//Generate nick change message
		Message msg = new Message("NICK", this);
		msg.appendParam(nick);
		
		//Find all members of all joined channels to send to
		Set<Client> toSendTo = new HashSet<Client>();
		for(Channel channel : channels)
		{
			toSendTo.addAll(channel.getMembers().keySet());
		}
		
		sendTo(toSendTo, msg);
		
		//Change nick
		id.nick = nick;
		return true;
	}
	
	/**
	 * Returns true if this client has been closed
	 * @return true if this client has been closed
	 */
	public boolean isClosed()
	{
		return closed;
	}
	
	/**
	 * Returns the permissions granted to this client
	 * @return permission mask of this client
	 */
	public int getPermissionMask()
	{
		//Check oper modes
		if(isModeSet('o'))
		{
			return Server.getServer().getConfig().permissionsOp;
		}
		else if(isModeSet('O'))
		{
			return Server.getServer().getConfig().permissionsSuperOp;
		}
		else
		{
			return 0;
		}	
	}
	
	/**
	 * Determines whether a client has an extra permission
	 * @param permission permission to check
	 * @return true if the client has that permission
	 */
	public boolean hasPermission(int permission)
	{
		return (getPermissionMask() & permission) != 0;
	}
	
	/**
	 * Gets the client's mode
	 * @return mode of the client
	 */
	public long getMode()
	{
		return mode;
	}
	
	/**
	 * Gets whether a user mode is set
	 * 
	 * @param mode the mode to test
	 * @return true if the mode is set
	 */
	public boolean isModeSet(char mode)
	{
		//TODO Refactor all the modes stuff out into a separate class
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
	
	/**
	 * Sets a client mode without checking or notification
	 * 
	 * @param mode mode to set
	 * @param adding true to add the mode
	 */
	private void setModeRaw(char mode, boolean adding)
	{
		long modeMask;
		
		//Get mask representing correct mode bit
		if(mode >= 'A' && mode <= 'Z')
		{
			modeMask = 1 << ('Z' - mode);
		}
		else if(mode >= 'a' && mode <= 'a')
		{
			modeMask = (1 << 32) << ('z' - mode);
		}
		else
		{
			throw new IllegalArgumentException("mode");
		}
		
		//Change mode
		if(adding)
		{
			this.mode |= modeMask;
		}
		else
		{
			this.mode &= ~modeMask;
		}
	}
	
	/**
	 * Sets a usermode and tells the client
	 * 
	 * @param mode mode to set
	 * @param adding whether to add the mode (false to delete it)
	 */
	public void setMode(char mode, boolean adding)
	{
		//Check mode setting
		if(isModeSet(mode) != adding)
		{
			//Change mode
			String str = "+" + mode;
			
			//Check for special modes
			if(mode == 'o' || mode == 'O')
			{
				if(adding)
				{
					//Check existing modes
					if(isModeSet('o') || isModeSet('O'))
					{
						//Unset other mode
						char c;
						if(mode == 'o')
						{
							c = 'O';
						}
						else
						{
							c = 'o';
						}
						
						setModeRaw(c, false);
						str += "-" + c;
					}
					else
					{
						//Add to oper cache
						Server.getServer().operators.add(this);
					}
	
					//Log change
					logger.info(id.toString() + " has set mode " + str);
				}
				else
				{
					//Remove from oper cache
					Server.getServer().operators.remove(this);
				}
			}
			
			//Change mode
			setModeRaw(mode, adding);
			send(new Message("MODE", this).appendParam(id.nick).appendParam(str));
		}
	}
	
	/**
	 * Returns the channels which this client has joined
	 * 
	 * @return channels this client has joined
	 */
	public Set<Channel> getChannels()
	{
		return Collections.unmodifiableSet(channels);
	}
	
	/**
	 * Changes the class of this client
	 * @param clazz Class to change to
	 * @param defaultClass True to change default class
	 * @return false if there are not enough links in a class to change
	 */
	protected boolean changeClass(ConnectionClass clazz, boolean defaultClass)
	{
		//Default = no class changes
		return true;
	}
	
	/**
	 * Restores this client's class to the default class
	 * 
	 * (default class restores override the max links)
	 */
	public void restoreClass()
	{
		//Default = no class changes
	}
	
	/**
	 * Changes the class of this client
	 * @param clazz Class to change to
	 */
	public final void changeClass(ConnectionClass clazz)
	{
		changeClass(clazz, false);
	}
	
	/**
	 * Returns the ip address for this client
	 * 
	 * Servlets always return 127.0.0.1
	 * 
	 * @return The ip address of the client
	 */
	public String getIpAddress()
	{
		//Default return
		return "127.0.0.1";
	}
	
	/**
	 * Returns true if this client is a remote user
	 * 
	 * @return true if this client is a remote user
	 */
	public boolean isRemote()
	{
		return false;
	}
	
	/**
	 * Marks this client for closure after the current client has finished processing
	 * 
	 * @param quitStatus the string told to other users about why this client is exiting
	 */
	public final void queueClose(String quitStatus)
	{
		if(queuedCloseReason != null)
		{
			queuedCloseReason = quitStatus;
			queuedClosures.add(this);
		}
	}
	
	/**
	 * Gets weather this client is queued for closure
	 * @return weather this client is queued for closure
	 */
	public boolean isQueuedForClose()
	{
		return queuedCloseReason != null;
	}
	
	/**
	 * Processes the close queue - closes all queued clients
	 */
	public static void processCloseQueue()
	{
		for(Client client : queuedClosures)
		{
			client.close(client.queuedCloseReason);
		}
		
		queuedClosures.clear();
	}
	
	/**
	 * Sends a message to a collection of clients
	 * 
	 * @param clients clients to send data to
	 * @param data data to send
	 */
	public static void sendTo(Iterable<? extends Client> clients, Object data)
	{
		sendTo(clients, data, null);
	}
	
	/**
	 * Sends a message to a collection of clients
	 * 
	 * @param clients clients to send data to
	 * @param data data to send
	 * @param except do not send message to this client
	 */
	public static void sendTo(Iterable<? extends Client> clients, Object data, Client except)
	{
		//Send strings to remote clients
		String remoteSend = data.toString();
		
		for(Client client : clients)
		{
			if(client != except)
			{
				if(client.isRemote())
				{
					client.send(remoteSend);
				}
				else
				{
					client.send(data);
				}
			}
		}
	}
	
	/**
	 * Creates a new message from this server with this client's nickname as the first parameter
	 * 
	 * @param command command of the message
	 */
	public Message newNickMessage(String command)
	{
		return Message.newMessageFromServer(command).appendParam(id.nick);
	}
	
	/**
	 * Sends IRC data to a client (data is converted to string with toString)
	 * 
	 * @param data Data to send
	 */
	public abstract void send(Object data);
	
	/**
	 * Performs client sepific close routines
	 * 
	 * @return Returns true if the close was a sucess. Returns false to abort the close.
	 */
	protected abstract boolean rawClose();
}
