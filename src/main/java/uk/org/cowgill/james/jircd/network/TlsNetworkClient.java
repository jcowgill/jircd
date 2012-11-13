package uk.org.cowgill.james.jircd.network;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;

import uk.org.cowgill.james.jircd.util.ModeUtils;

/**
 * A NetworkClient operating over a secure TLS connection
 * 
 * @author James
 */
class TlsNetworkClient extends NetworkClient
{
	private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);
	
	//Current TLS Engine
	private SSLEngine engine;
	
	//TLS Buffers
	private ByteBuffer encInbound;
	private ByteBuffer encOutbound;
	
	//Queued initial buffers
	private LinkedList<ByteBuffer> bufferQueue = new LinkedList<ByteBuffer>();
	
	/**
	 * Creates a new TLS client wrapping the given channel
	 * 
	 * @param channel channel to wrap
	 * @param context the ssl context to use - this must be loaded with 1 server key
	 */
	protected TlsNetworkClient(SocketChannel channel, SSLContext context) throws IOException
	{
		super(channel, ModeUtils.setMode(0, 'Z'));
		
		//Create new engine
		engine = context.createSSLEngine();
		engine.setUseClientMode(false);
		
		//Allocate TLS buffers
		int encBufferSize = engine.getSession().getPacketBufferSize();
		encInbound = ByteBuffer.allocate(encBufferSize);
		encOutbound = ByteBuffer.allocate(encBufferSize);
		
		//Begin handshake (client sends hello first)
		engine.beginHandshake();
	}
	
	/**
	 * Sends all the data in the buffer queue
	 */
	private boolean sendBufferQueue() throws IOException
	{
		while(!bufferQueue.isEmpty())
		{
			if(!wrapAndSend(bufferQueue.poll()))
				return false;
		}
		
		return true;
	}
	
	/**
	 * Wraps the given data and sends it, returning true if all the data was sent
	 * 
	 * Ensure handshake tasks are processed after using this
	 * 
	 * @param userData user supplied data
	 * @throws IOException 
	 */
	private boolean wrapAndSend(ByteBuffer userData) throws IOException
	{
		//Wrap data
		encOutbound.clear();
		SSLEngineResult result = engine.wrap(userData, encOutbound);
		encOutbound.flip();
		
		//Check result
		if(result.getStatus() != SSLEngineResult.Status.OK)
		{
			//SSL Failed
			return false;
		}
		
		//Send the data
		if(super.writeWrapper(encOutbound))
		{
			//Send initial buffer if the handshake just finished
			if(result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED)
			{
				return sendBufferQueue();
			}
			
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Processes handshake tasks after a wrap / unwrap operation
	 * 
	 * @param hStatus handshake status code
	 * @return false if a wrap and send failed (sendq exceeded) 
	 */
	private boolean processHandshakeTasks() throws IOException
	{
		for(;;)
		{
			switch(engine.getHandshakeStatus())
			{
				case NEED_TASK:
					//Do task
					engine.getDelegatedTask().run();
					break;
					
				case NEED_WRAP:
					//So a send on any data in the buffers
					if(!wrapAndSend(EMPTY_BUFFER))
						return false;
					
					break;
					
				case FINISHED:
					//Done - send final buffer
					sendBufferQueue();
					return true;
					
				default:
					//Ignore any other commands
					return true;
			}
		}
	}
	
	@Override
	protected int readWrapper(ByteBuffer buffer) throws IOException
	{
		int totalBytes = 0;
		
		//Data has arrived, read it and process it
		switch(super.readWrapper(encInbound))
		{
			case -1:
				//End of stream
				engine.closeInbound();
				return -1;
				
			case 0:
				//No data
				return 0;
		}
		
		//Prepare buffer
		encInbound.flip();
		
		for(;;)
		{
			//Try to unwrap the data
			SSLEngineResult result = engine.unwrap(encInbound, buffer);
			
			switch(result.getStatus())
			{
				case BUFFER_OVERFLOW:
					//Not enough space = ReadQ exceeded
					// Simulate this so it can be handled by the caller
					int bytes = buffer.remaining();
					buffer.position(buffer.limit());
					return totalBytes + bytes;
					
				case BUFFER_UNDERFLOW:
					//Not enough bytes to complete packet - wait for more
					encInbound.compact();
					return totalBytes;
	
				case CLOSED:
					//Connection closed
					return -1;
	
				case OK:
					//Process extra tasks
					if(!processHandshakeTasks())
					{
						//Send Q exceeded
						close("SendQ Limit Exceeded");
						return 0;
					}
					
					break;
			}
			
			//Try to get more data
			totalBytes += result.bytesProduced();
		}
	}
	
	@Override
	protected boolean writeWrapper(ByteBuffer buffer) throws IOException
	{
		//Check if the connection is valid yet
		if(engine.getHandshakeStatus() != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING)
		{
			//Add to queue and process any tasks
			bufferQueue.offer(buffer);
			processHandshakeTasks();
			return true;
		}
		else
		{
			//Check for anything on the buffer queue first
			sendBufferQueue();
			
			//Send data
			// As we know there is no handshake, there shouldn't be any tasks
			return wrapAndSend(buffer);
		}
	}
	
	protected boolean rawClose()
	{
		//Close SSL Engine
		engine.closeOutbound();
		
		//Dump any handshake items still left
		try
		{
			wrapAndSend(EMPTY_BUFFER);
		}
		catch(IOException e)
		{
			//Ignore
		}
		
		//Close the socket
		return super.rawClose();
	}
}
