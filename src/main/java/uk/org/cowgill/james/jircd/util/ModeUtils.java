package uk.org.cowgill.james.jircd.util;

/**
 * Contains a number of static members to manipulate mode bitsets
 * 
 * <p>This does not handle modes assigned to channel members. Use {@link ChannelMemberMode} for that.
 * 
 * @author James
 */
public final class ModeUtils
{
	/**
	 * Checks whether a mode is set
	 * 
	 * @param modeSet modeset to search
	 * @param mode the mode to check
	 * @return true if the mode is set
	 */
	public static boolean isModeSet(long modeSet, char mode)
	{
		//Check modes bitset
		if(mode >= 'A' && mode <= 'Z')
		{
			return (modeSet & (1 << ('Z' - mode))) != 0;
		}
		else if(mode >= 'a' && mode <= 'z')
		{
			return (modeSet & ((1 << 32) << ('z' - mode))) != 0;
		}
		else
		{
			//Invalid modes are never set
			return false;
		}
	}
	
	/**
	 * Sets a mode in the modeset
	 * 
	 * @param modeSet modeset to search
	 * @param mode the mode to check
	 * @return the new modeset
	 */
	public static long setMode(long modeSet, char mode)
	{
		if(mode >= 'A' && mode <= 'Z')
		{
			return modeSet | 1 << ('Z' - mode);
		}
		else if(mode >= 'a' && mode <= 'a')
		{
			return modeSet | (1 << 32) << ('z' - mode);
		}
		else
		{
			//Invalid mode
			throw new IllegalArgumentException("mode");
		}
	}
	
	/**
	 * Clears a mode in the modeset
	 * 
	 * @param modeSet modeset to search
	 * @param mode the mode to check
	 * @return the new modeset
	 */
	public static long clearMode(long modeSet, char mode)
	{
		if(mode >= 'A' && mode <= 'Z')
		{
			return modeSet & ~(1 << ('Z' - mode));
		}
		else if(mode >= 'a' && mode <= 'a')
		{
			return modeSet & ~((1 << 32) << ('z' - mode));
		}
		else
		{
			//Invalid mode
			throw new IllegalArgumentException("mode");
		}
	}

	/**
	 * Changes a mode in the modeset
	 * 
	 * @param modeSet modeset to search
	 * @param mode the mode to check
	 * @param true to set the mode, false to clear it
	 * @return the new modeset
	 */
	public static long changeMode(long modeSet, char mode, boolean adding)
	{
		if(adding)
		{
			return setMode(modeSet, mode);
		}
		else
		{
			return clearMode(modeSet, mode);
		}
	}

	/**
	 * Converts a mode bitmask to a string
	 * 
	 * @param mode the mode bitmask
	 * @return the string containing the mode
	 */
	public static String toString(long modeSet)
	{
		StringBuilder modeStr = new StringBuilder("+");
		
		//Lowercase first
		for(char c = 'a'; c <= 'z'; c++)
		{
			//Test most significant bit
			if((modeSet & (1 << 63)) != 0)
			{
				modeStr.append(c);
			}
			
			modeSet <<= 1;
		}
		
		//Uppercase
		for(char c = 'A'; c <= 'Z'; c++)
		{
			//Test most significant bit
			if((modeSet & (1 << 63)) != 0)
			{
				modeStr.append(c);
			}
			
			modeSet <<= 1;
		}
		
		if(modeStr.length() == 1)
		{
			return "";
		}
		else
		{
			return modeStr.toString();
		}
	}
	
	private ModeUtils()
	{
	}
}
