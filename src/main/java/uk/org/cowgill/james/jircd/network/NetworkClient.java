package uk.org.cowgill.james.jircd.network;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import org.apache.log4j.Logger;

import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.ConnectionClass;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.Server;

/**
 * A networking client implementation
 * 
 * @author James
 */
final class NetworkClient extends Client
{
	/**
	 * Timeout after client has been immediately created
	 */
	public static final int START_TIMEOUT = 5;
	
	/**
	 * Timeout after a ping has been sent to the client
	 */
	public static final int AFTER_PING_TIMEOUT = 5;
	
	/**
	 * ReadQ for client after immediately created
	 */
	public static final int START_READQ = 1024;
	
	/**
	 * SendQ for client after immediately created
	 */
	public static final int START_SENDQ = 1024;
	
	//----------------------------------

	private static final Logger logger = Logger.getLogger(NetworkClient.class);
	
	/**
	 * UTF-8 character set encoder
	 */
	private static final CharsetEncoder cEncoder = Charset.forName("UTF-8").newEncoder();
	
	/**
	 * UTF-8 character set decoder
	 */
	private static final CharsetDecoder cDecoder = Charset.forName("UTF-8").newDecoder();
	
	/**
	 * ByteBuffer containing a carriage return then a line feed
	 */
	private static final ByteBuffer CRLF = ByteBuffer.wrap(new byte[] { '\r', '\n' });

	//-----------------------------------
	
	/**
	 * Channel this client is connected to
	 */
	private SocketChannel channel;

	/**
	 * Data for byte buffer
	 */
	private byte[] localBufferData = new byte[512];
	
	/**
	 * Byte buffer to recent messages
	 */
	private ByteBuffer localBuffer = ByteBuffer.wrap(localBufferData);
	
	/**
	 * Default connection class
	 */
	private ConnectionClass defaultConnClass;
	
	/**
	 * Current connection class
	 */
	private ConnectionClass connClass;
	
	/**
	 * Sets up a new NetworkClient from a SocketChannel
	 * 
	 * @param channel channel to setup from
	 * @throws IOException thrown when an error occurs in setting socket options
	 */
	NetworkClient(SocketChannel channel) throws IOException
	{
		//Setup channel options
		channel.configureBlocking(false);
		channel.socket().setSendBufferSize(START_SENDQ);
		channel.socket().setReceiveBufferSize(START_READQ);
		
		//Save channel
		this.channel = channel;
	}
	
	/**
	 * Called when a read event occurs
	 */
	void processReadEvent() throws IOException
	{
		//Read message into buffer
		int endByte = channel.read(localBuffer) + localBuffer.position();
		localBuffer.position(0);
		
		//Find messages in buffer
		for(int i = 1; i < endByte; i++)
		{
			//Check for end of message
			if(localBufferData[i] == '\n' && localBufferData[i - 1] == '\r')
			{
				//Found new line
				localBuffer.limit(i - 1);
				
				if(localBuffer.remaining() == 0)
				{
					//Skip this blank message
					if(i != 511)
					{
						localBuffer.position(i + 1);
					}
					
					continue;
				}
				
				//Decode message
				Message msg = Message.parse(cDecoder.decode(localBuffer).toString());
				
				//Dispatch message
				Server.getServer().getModuleManager().executeCommand(this, msg);
				
				//Reset position and limit
				localBuffer.position(i + 1);
				localBuffer.limit(512);
			}
		}
		
		//Copy data after position back to start
		int bytesLeft = endByte - localBuffer.position();
		System.arraycopy(localBufferData, localBuffer.position(),
						 localBufferData, 0, bytesLeft);
		
		localBuffer.position(bytesLeft);
		
		//Process closure queue
		processCloseQueue();
	}
	
	@Override
	public void send(Object data)
	{
		//Get string
		CharSequence strData;
		if(data instanceof CharSequence)
		{
			strData = (CharSequence) data;
		}
		else
		{
			strData = data.toString();
		}
		
		try
		{
			//Encode object as string
			ByteBuffer buffer = cEncoder.encode(CharBuffer.wrap(strData));
			buffer.flip();
			
			//Write to buffer
			if(channel.write(new ByteBuffer[] { buffer, CRLF }) != (buffer.limit() + 2))
			{
				//SendQ limit exceeded
				queueClose("SendQ Limit Exceeded");
			}
		}
		catch(CharacterCodingException e)
		{
			logger.warn("Error encoding message", e);
		}
		catch(IOException e)
		{
			//Error writing to message
			queueClose("IO Error");
		}
	}

	@Override
	protected boolean rawClose()
	{
		try
		{
			//Close channel
			channel.close();
		}
		catch(IOException e)
		{
		}
		
		return true;
	}
	
	@Override
	public boolean isRemote()
	{
		return true;
	}
	
	/**
	 * Gets the remote address of this client
	 * 
	 * @return the remote address of this client
	 */
	InetAddress getRemoteAddress()
	{
		return channel.socket().getInetAddress();
	}
	
	@Override
	public String getIpAddress()
	{
		return channel.socket().getInetAddress().getHostAddress();
	}

	private void forceChangeClass(ConnectionClass clazz)
	{
		//Update link count
		clazz.currentLinks++;
		connClass.currentLinks--;
		
		//Update buffer sizes
		try
		{
			channel.socket().setReceiveBufferSize(clazz.readQueue);
			channel.socket().setSendBufferSize(clazz.readQueue);
		}
		catch(IOException e)
		{
			//TODO print id in error message
			logger.error("Error setting buffer sizes for client " + clazz);
		}
		
		
		//Update restricted flag
		// TODO restricted
		
		//Update ping frequency
		// TODO ping and idle timeouts
		
		//Set class
		connClass = clazz;
	}
	
	@Override
	protected boolean changeClass(ConnectionClass clazz, boolean defaultClass)
	{
		//Check link count
		if(clazz.currentLinks >= clazz.maxLinks)
		{
			return false;
		}
		
		//Force class change
		forceChangeClass(clazz);
		
		//Copy default class
		if(defaultClass)
		{
			this.defaultConnClass = clazz;
		}
		
		return true;
	}
	
	@Override
	public void restoreClass()
	{
		forceChangeClass(defaultConnClass);
	}
}
