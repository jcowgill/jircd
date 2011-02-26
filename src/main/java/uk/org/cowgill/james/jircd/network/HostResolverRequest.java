package uk.org.cowgill.james.jircd.network;

import uk.org.cowgill.james.jircd.RegistrationFlags;

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
		final String host = client.getRemoteAddress().getHostName();
		
		//Update host in client
		client.id.host = host;
		client.setRegistrationFlag(RegistrationFlags.HostSet);
	}
}
