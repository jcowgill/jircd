package uk.org.cowgill.james.jircd.network;

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
	static final int START_TIMEOUT = 5;
	
	/**
	 * Timeout after a ping has been sent to the client
	 */
	static final int AFTER_PING_TIMEOUT = 5;
	
	/**
	 * Size of raw receive buffer
	 */
	static final int RECEIVE_SIZE = 2048;
	
	/**
	 * ReadQ for client after immediately created (max size for buffer)
	 */
	static final int START_READQ = 1024;
	
	/**
	 * SendQ for client after immediately created
	 */
	static final int START_SENDQ = 1024;

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
