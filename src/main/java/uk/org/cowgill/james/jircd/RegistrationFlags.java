package uk.org.cowgill.james.jircd;

/**
 * Collection of flags used during registration
 * 
 * @author James
 */
public final class RegistrationFlags
{
	private RegistrationFlags()
	{
	}
	
	/**
	 * Flag set if a nickname has been selected
	 */
	public static final int NickSet = 1;
	
	/**
	 * Flag set if a username has been selected
	 */
	public static final int UserSet = 2;
	
	/**
	 * Flag set if a hostname has been found
	 */
	public static final int HostSet = 4;
	
	/**
	 * Flag set if custom server actions have been carried out
	 * 
	 * For the NetworkServer, this is used to mark when the NOSPOOF ping test has been carried out
	 */
	public static final int ServerCustom = 8;
	
	/**
	 * All the registration flags together
	 */
	public static final int AllFlags = 15;
}
