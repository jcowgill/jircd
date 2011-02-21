package uk.org.cowgill.james.jircd;

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

	/**
	 * Changes the class of this client
	 * @param clazz Class to change to
	 * @param defaultClass True to change default class
	 */
	protected void changeClass(ConnectionClass clazz, boolean defaultClass)
	{
		//Default = no class changes
	}
	
	/**
	 * Restores this client's class to the default class
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
	 * Requests that this client be closed
	 */
	public final void close()
	{
		//TODO Close
	}
}
