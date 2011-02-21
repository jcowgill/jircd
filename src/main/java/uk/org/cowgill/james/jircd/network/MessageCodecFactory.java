package uk.org.cowgill.james.jircd.network;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * Creates message encoders and decoders for the protocol codec filter
 * 
 * @author James
 */
class MessageCodecFactory implements ProtocolCodecFactory
{
	private MessageEncoder encoder;
	private MessageDecoder decoder;
	
	public MessageCodecFactory()
	{
		//Create codec classes
		encoder = new MessageEncoder();
		decoder = new MessageDecoder();
	}
	
	@Override
	public ProtocolEncoder getEncoder(IoSession paramIoSession)
	{
		return encoder;
	}

	@Override
	public ProtocolDecoder getDecoder(IoSession paramIoSession)
	{
		return decoder;
	}
}
