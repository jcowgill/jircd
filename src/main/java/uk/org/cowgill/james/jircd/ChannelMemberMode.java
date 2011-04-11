package uk.org.cowgill.james.jircd;

/**
 * Contains the channel member modes
 * 
 * @author James
 */
public final class ChannelMemberMode
{
	public int member = 1;

	public int ban = 2;

	public int voice = 4;

	public int halfOp = 8;

	public int op = 16;

	public int admin = 32;
	
	public int owner = 64;
}
