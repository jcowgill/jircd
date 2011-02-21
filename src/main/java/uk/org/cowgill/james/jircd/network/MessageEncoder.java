package uk.org.cowgill.james.jircd.network;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 * Class which encodes irc messages and sends them on
 * 
 * @author James
 */
class MessageEncoder extends ProtocolEncoderAdapter
{
	/**
	 * Encoder for IRC messages
	 * 
	 * IRC requires chars 0-127 be ASCII but the rest are implementation defined
	 * These days most clients use UTF-8
	 */
	private static final CharsetEncoder cEncoder = Charset.forName("UTF-8").newEncoder();
	
	// Carriage Return + Line Feed Combinations
	private static final byte CR = '\r';
	private static final byte LF = '\n';
	private static final byte[] CRLF = new byte[] { CR, LF };
	
	@Override
	public void encode(IoSession session, Object data, ProtocolEncoderOutput out) throws Exception
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
		
		//Encode object as string then add CRLF
		IoBuffer buffer = IoBuffer.allocate((int) (cEncoder.averageBytesPerChar() * strData.length()) + 2);
		buffer.setAutoExpand(true);

		buffer.putString(strData, cEncoder);
		buffer.put(CRLF);
		
		out.write(buffer);
	}
}
