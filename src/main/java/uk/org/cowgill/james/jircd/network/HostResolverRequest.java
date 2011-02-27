package uk.org.cowgill.james.jircd.network;

import java.nio.channels.Selector;
import java.util.concurrent.BlockingQueue;

/**
 * A request to resolve the hostname of a client
 * 
 * @author James
 */
class HostResolverRequest implements Runnable
{
	private NetworkClient client;
	private BlockingQueue<NetworkClient> finishQueue;
	private Selector eventSelector;
	
	/**
	 * Creates a new host resolver request
	 * 
	 * @param client client whose host to resolve
	 * @param finishQueue queue to add client to when the operation has been complete
	 * @param eventSelector selector to wake up when done
	 */
	public HostResolverRequest(NetworkClient client, BlockingQueue<NetworkClient> finishQueue, Selector eventSelector)
	{
		this.client = client;
		this.finishQueue = finishQueue;
		this.eventSelector = eventSelector;
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
		
		//Notify caller
		finishQueue.offer(client);
		eventSelector.wakeup();
	}
}
