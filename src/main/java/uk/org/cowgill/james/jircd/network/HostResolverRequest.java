package uk.org.cowgill.james.jircd.network;

/**
 * A request to resolve the hostname of a client
 * 
 * @author James
 */
class HostResolverRequest implements Runnable
{
	private NetworkClient client;
	
	/**
	 * Creates a new host resolver request
	 * 
	 * @param client client whose host to resolve
	 */
	public HostResolverRequest(NetworkClient client)
	{
		this.client = client;
	}
	
	/**
	 * Performs the hostname resolution (blocking)
	 */
	@Override
	public void run()
	{
		//Resolve hostname
		String host = client.getRemoteAddress().getHostName();
		
		//TODO Update host in ID
		// No events should fire from this thread
	}
}
