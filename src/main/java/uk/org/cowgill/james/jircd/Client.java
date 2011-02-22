package uk.org.cowgill.james.jircd;

import java.util.ArrayList;

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
	private IRCMask id;
	
	/**
	 * Gets a mask containing the id of this client
	 * @return a mask containing the id of this client
	 */
	public IRCMask getId()
	{
		return id.clone();
	}

	/**
	 * Requests that this client be closed
	 * 
	 * @param quitStatus the string told to other users about why this client is exiting
	 */
	public final void close(String quitStatus)
	{
		//TODO Close
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
	public String ipAddress()
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
