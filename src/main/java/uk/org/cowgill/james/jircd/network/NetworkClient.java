package uk.org.cowgill.james.jircd.network;

import java.nio.channels.SocketChannel;

import uk.org.cowgill.james.jircd.Client;

/**
 * A networking client implementation
 * 
 * @author James
 */
class NetworkClient extends Client
{
	/**
	 * Timeout after client has been immediately created
	 */
	public static final int START_TIMEOUT = 5;
	
	/**
	 * Timeout after a ping has been sent to the client
	 */
	public static final int AFTER_PING_TIMEOUT = 5;
	
	/**
	 * Size of raw receive buffer
	 */
	public static final int RECEIVE_SIZE = 2048;
	
	/**
	 * ReadQ for client after immediately created (max size for buffer)
	 */
	public static final int START_READQ = 1024;
	
	/**
	 * SendQ for client after immediately created
	 */
	public static final int START_SENDQ = 1024;

	public NetworkClient(SocketChannel channel)
	{
		//
	}
	
	/**
	 * Called when a read event occurs
	 */
	public void processReadEvent()
	{
		//
	}
	
	@Override
	public void send(Object data)
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean rawClose()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
