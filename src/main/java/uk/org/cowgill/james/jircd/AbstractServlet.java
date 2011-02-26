package uk.org.cowgill.james.jircd;

/**
 * An abstract servelt helper class
 * 
 * @author James
 */
public abstract class AbstractServlet extends Client
{
	/**
	 * Event occurs when a message is received by this servlet
	 * 
	 * @param msg message received
	 */
	public abstract void messageReceived(Message msg);
	
	/**
	 * Creates a new servlet with the given ID
	 * 
	 * To avoid confusion - servlets should use either 127.0.0.1 or localhost as the hostname
	 * 
	 * @param id ID of this servlet
	 */
	public AbstractServlet(IRCMask id)
	{
		//Set ID
		super(id);
		
		//Register
		this.setRegistrationFlag(RegistrationFlags.AllFlags);
	}
	
	@Override
	public final void send(Object data)
	{
		Message msg;
		
		//Redirect to received message
		if(data instanceof Message)
		{
			msg = (Message) data;
		}
		else
		{
			msg = Message.parse(data.toString());
		}
		
		messageReceived(msg);
	}
}
