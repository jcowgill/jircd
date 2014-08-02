/*
   Copyright 2011 James Cowgill

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
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
	public void submitRequest(NetworkClient client)
	{
		//Create new request and submit
		eService.execute(new HostResolverRequest(client, hostResolveOutput, eventSelector));
	}

	/**
	 * Submits a request for the resolver to process
	 *
	 * @param request resolution request
	 */
	public void submitRequest(HostResolverRequest request)
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
	protected void finalize() throws Throwable
	{
		//Ensure service is shutdown
		super.finalize();
		shutdown();
	}
}
