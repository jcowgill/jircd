package uk.org.cowgill.james.jircd.network;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class which manages host resolution of client's ips
 * 
 * @author James
 */
final class HostResolver
{
	/**
	 * Current executor service
	 */
	private ExecutorService eService = Executors.newCachedThreadPool();

	/**
	 * Submits a request for the resolver to process
	 * @param client client whose hostname to resolve
	 */
	public void sumbitRequest(NetworkClient client)
	{
		//Create new request and submit
		eService.execute(new HostResolverRequest(client));
	}
	
	/**
	 * Submits a request for the resolver to process
	 * @param client resolution request
	 */
	public void sumbitRequest(HostResolverRequest request)
	{
		//Create new request and submit
		eService.execute(request);
	}
	
	/**
	 * Immediately shuts down the hostname resolver and aborts all running requests
	 */
	public void shutdown()
	{
		eService.shutdownNow();
	}
	
	@Override
	protected void finalize()
	{
		//Ensure service is shutdown
		eService.shutdownNow();
	}
}
