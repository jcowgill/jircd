package uk.org.cowgill.james.jircd.network;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import uk.org.cowgill.james.jircd.Message;

/**
 * Class used to decode string messages into IRC Message objects
 * 
 * @author James
 */
class MessageDecoder extends CumulativeProtocolDecoder
{
	private CharsetDecoder cDecoder = Charset.forName("UTF-8").newDecoder();
	
	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception
	{
		// From example at:
		//  http://mina.apache.org/report/trunk/apidocs/org/apache/mina/filter/codec/CumulativeProtocolDecoder.html
		
		// Remember the initial position.
		int start = in.position();

		// Now find the first CRLF in the buffer.
		byte previous = 0;
		while (in.hasRemaining())
		{
			byte current = in.get();

			if (previous == '\r' && current == '\n')
			{
				// Remember the current position and limit.
				int position = in.position();
				int limit = in.limit();
				
				try
				{
					in.position(start);
					in.limit(position - 2);

					// The bytes between in.position() and in.limit()
					// now contain a full CRLF terminated line.
					out.write(Message.parse(cDecoder.decode(in.slice().buf()).toString()));
				}
				finally
				{
					// Set the position to point right after the
					// detected line and set the limit to the old
					// one.
					in.position(position);
					in.limit(limit);
				}
				
				// Decoded one line; CumulativeProtocolDecoder will
				// call me again until I return false. So just
				// return true until there are no more lines in the
				// buffer.
				return true;
			}

			previous = current;
		}

		// Could not find CRLF in the buffer. Reset the initial
		// position to the one we recorded above.
		in.position(start);

		return false;
	}
}
