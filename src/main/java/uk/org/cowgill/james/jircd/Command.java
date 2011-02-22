package uk.org.cowgill.james.jircd;

/**
 * Implementation for a command event
 * 
 * @author James
 */
public interface Command
{
	/**
	 * Event occurs when a command is received by any client
	 * 
	 * @param client client the message was received from
	 * @param msg message object representing the message received
	 */
	public void run(Client client, Message msg);
}
