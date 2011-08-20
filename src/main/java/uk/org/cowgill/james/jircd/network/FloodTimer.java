package uk.org.cowgill.james.jircd.network;

import java.util.LinkedList;

import uk.org.cowgill.james.jircd.Permissions;

/**
 * Class which handles whether networking clients can process messages in the flood limit
 * 
 * @author james
 */
class FloodTimer
{
	private static LinkedList<NetworkClient> floodQueue = new LinkedList<NetworkClient>();
	
	private NetworkClient client;
	private long timer;
	
	/**
	 * Creates a new flood timer for the given client
	 * 
	 * @param client client to create timer for
	 */
	public FloodTimer(NetworkClient client)
	{
		this.client = client;
	}
	
	/**
	 * Raise the read event on all clients in the flood queue
	 */
	public static void processFloodQueue()
	{
		if(!floodQueue.isEmpty())
		{
			//Extract flood queue
			LinkedList<NetworkClient> queue = floodQueue;
			floodQueue = new LinkedList<NetworkClient>();
			
			//Raise read event on all entries
			for(NetworkClient client : queue)
			{
				if(!client.isClosed())
				{
					client.processReadEvent();
				}
			}
		}
	}
	
	/**
	 * Determines whether messages can be processed and adds you to the flood queue
	 * 
	 * <p>If this returns false, this method will also add the client to the flood queue
	 * 
	 * @return true if messages can be processed
	 */
	public boolean checkTimer()
	{
		//Check opers and timers
		if(client.hasPermission(Permissions.noFloodLimit) ||
				timer < (System.currentTimeMillis() + 10000))
		{
			return true;
		}
		else
		{
			floodQueue.add(client);
			return false;
		}
	}
	
	/**
	 * Updates the flood timer after a message has been processed
	 */
	public void processMessage()
	{
		//Update timer after processing message
		if(timer < System.currentTimeMillis())
		{
			//Fast-forward timer first
			timer = System.currentTimeMillis();
		}
		
		//Add 2 second penalty
		timer += 2000;
	}
}
