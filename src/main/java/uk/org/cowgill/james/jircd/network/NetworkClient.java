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
import java.security.SecureRandom;
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
	private static final ConnectionClass DEFAULT_CONN_CLASS = new ConnectionClass();
	
	private static final SecureRandom randomGen = new SecureRandom();
	
	/**
	 * Timeout after a ping has been sent to the client
	 */
	public static final int AFTER_PING_TIMEOUT = 5;
	
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
	 * Time of the last message to be received by the server
	 */
	private long lastMessageTime;
	
	/**
	 * Spoof check string
	 */
	String spoofCheckChars;
	
	/**
	 * Default connection class
	 */
	private ConnectionClass defaultConnClass;
	
	/**
	 * Current connection class
	 */
	private ConnectionClass connClass;
	
	//Static constructor
	static
	{
		//Setup default connection class
		DEFAULT_CONN_CLASS.maxLinks = Integer.MAX_VALUE;
		DEFAULT_CONN_CLASS.pingFreq = 0;	//No "extra" ping frequency
		DEFAULT_CONN_CLASS.readQueue = 1024;
		DEFAULT_CONN_CLASS.sendQueue = 1024;
	}
	
	/**
	 * Sets up a new NetworkClient from a SocketChannel
	 * 
	 * @param channel channel to setup from
	 * @throws IOException thrown when an error occurs in setting socket options
	 */
	NetworkClient(SocketChannel channel) throws IOException
	{
		//Save channel
		this.channel = channel;
		
		//Setup channel options
		channel.configureBlocking(false);
		changeClass(DEFAULT_CONN_CLASS, true);
		
		//Begin spoof check
		final StringBuffer buffer = new StringBuffer(10);
		for(int i = 0; i < 10; ++i)
		{
			buffer.append((char) randomGen.nextInt('z' - 'A'));
		}
		
		spoofCheckChars = buffer.toString();
		
		send(Message.newStringFromServer("PING :" + spoofCheckChars));
	}
	
	/**
	 * Called when a read event occurs
	 */
	void processReadEvent() throws IOException
	{
		//Read message into buffer
		int endByte = channel.read(localBuffer) + localBuffer.position();
		localBuffer.position(0);
		
		//Update message time
		lastMessageTime = System.currentTimeMillis();
		
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

	/**
	 * Event which occurs when the ping timouts need checking
	 */
	void pingCheckEvent()
	{
		//Check for ping timeout
		long diffInSeconds = (System.currentTimeMillis() - lastMessageTime) / 1000;
		
		if(diffInSeconds >= connClass.pingFreq)
		{
			//Check if completely timed out
			if(diffInSeconds >= connClass.pingFreq + AFTER_PING_TIMEOUT)
			{
				queueClose("Ping Timeout");
			}
			else if(isRegistered())
			{
				//Send ping
				send(Message.newStringFromServer("PING " + id.nick));
			}
		}
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
	
	@Override
	protected void registeredEvent()
	{
		//Purpose of this is to allow NetworkServer access to this method
		super.registeredEvent();
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
		//Check if already in class
		if(connClass == clazz)
		{
			return;
		}
		
		//Update link count
		clazz.currentLinks++;
		
		if(connClass != null)
		{
			connClass.currentLinks--;
		}
		
		//Update buffer sizes
		try
		{
			channel.socket().setReceiveBufferSize(clazz.readQueue);
			channel.socket().setSendBufferSize(clazz.readQueue);
		}
		catch(IOException e)
		{
			logger.error("Error setting buffer sizes for client " + id.toString());
		}
		
		//Class changing causes an update in last message time
		lastMessageTime = System.currentTimeMillis();
		
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
