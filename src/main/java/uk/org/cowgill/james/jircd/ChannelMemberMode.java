package uk.org.cowgill.james.jircd;

/**
 * Contains the channel member modes
 * 
 * @author James
 */
public class ChannelMemberMode
{
	private int mode = 0;
	
	/**
	 * Set if the ban lists have been checked for this member since they were last updated
	 */
	public static final int BANCHECKED = 1;

	/**
	 * Set if member is banned (do not believe if banChecked is clear)
	 */
	public static final int BANNED = 2;

	/**
	 * Set if member is voiced
	 */
	public static final int VOICE = 4;

	/**
	 * Set if member is a half operator=
	 */
	public static final int HALFOP = 8;

	/**
	 * Set if member is an operator
	 */
	public static final int OP = 16;

	/**
	 * Set if member is an administrator
	 */
	public static final int ADMIN = 32;
	
	/**
	 * Set if member is a channel owner
	 */
	public static final int OWNER = 64;
	
	/**
	 * Finds the highest mode of a member
	 * 
	 * @param mode member's mode
	 * @return a member mode with only 1 (or 0) bit set with the mode
	 */
	public int getHighestMode()
	{
		int currMode = mode;
		
		currMode |= currMode >> 1;
		currMode |= currMode >> 2;
		currMode |= currMode >> 4;
		
		return currMode - (currMode >> 1);
	}
	
	/**
	 * Gets the channel mode
	 * @return the channel mode
	 */
	public int getMode()
	{
		return mode;
	}
	
	/**
	 * Sets all modes to the specified value
	 * @param mode all modes
	 */
	public void setAllModes(int mode)
	{
		this.mode = mode;
	}
	
	/**
	 * Sets a mode
	 * @param mode mode to set
	 */
	public void setMode(int mode)
	{
		this.mode |= mode;
	}
	
	/**
	 * Clears a mode
	 * @param mode mode to clear
	 */
	public void clearMode(int mode)
	{
		this.mode &= ~mode;
	}
	
	/**
	 * Returns weather a mode has been set
	 * @param mode mode to check
	 * @return true if the mode specified is set
	 */
	public boolean isModeSet(int mode)
	{
		return (this.mode & mode) != 0;
	}
}
