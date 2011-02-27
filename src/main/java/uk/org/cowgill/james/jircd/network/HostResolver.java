package uk.org.cowgill.james.jircd.network;

import java.nio.channels.Selector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

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
	 * List of clients whose hosts have been resolved and need attention
	 */
	private LinkedBlockingQueue<NetworkClient> hostResolveOutput = new LinkedBlockingQueue<NetworkClient>();
	
	/**
	 * Resolver's server
	 */
	private Selector eventSelector;
	
	/**
	 * Creates a new host resolver with a wake up selector
	 * @param eventSelector selector to wake up when each request has completed
	 */
	public HostResolver(Selector eventSelector)
	{
		this.eventSelector = eventSelector;
	}
	
	/**
	 * Submits a request for the resolver to process
	 * @param client client whose hostname to resolve
	 */
	public void sumbitRequest(NetworkClient client)
	{
		//Create new request and submit
		eService.execute(new HostResolverRequest(client, hostResolveOutput, eventSelector));
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
	 * Checks if any requests have finished, if they have returns one.
	 * 
	 * If no more requests have finished since drainOneFinished was last called,
	 *  this returns null
	 * 
	 * @return null or the finished client
	 */
	public NetworkClient drainOneFinished()
	{
		return hostResolveOutput.poll();
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
