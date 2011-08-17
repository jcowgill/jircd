package uk.org.cowgill.james.jircd;

/**
 * Collection of flags used to mark wheather clients can accept protocol enhancements
 * 
 * @author James
 */
public final class ProtocolEnhancements
{
	private ProtocolEnhancements()
	{
	}
	
	/**
	 * Allows multiple prefixes in /NAMES
	 */
	public static int NamesX = 1;
	
	/**
	 * Adds the username and hostname to /NAMES commands
	 */
	public static int UhNames = 2;
}
