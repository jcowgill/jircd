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
		finishQueue.add(client);
		eventSelector.wakeup();
	}
}
