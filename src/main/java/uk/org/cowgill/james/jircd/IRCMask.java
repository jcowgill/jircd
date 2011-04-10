package uk.org.cowgill.james.jircd;

/**
 * Represents a Nick, User and Host trupal in the IRC server
 * 
 * Used to store a client's nick, user and host.
 * Also used to store masks for bans or other checking.
 * 
 * IRCMasks are case-insensitive
 * 
 * @author James
 *
 */
public class IRCMask implements Cloneable, Comparable<IRCMask>
{
	/**
	 * The mask's nickname
	 * 
	 * This field must not be set to null
	 */
	public String nick;
	
	/**
	 * The mask's username
	 * 
	 * This field must not be set to null
	 */
	public String user;
	
	/**
	 * The mask's hostname
	 * 
	 * This field must not be set to null
	 */
	public String host;
	
	/**
	 * Clones this IRC mask
	 */
	@Override
	public IRCMask clone()
	{
		IRCMask mask = new IRCMask();
		
		mask.nick = nick;
		mask.user = user;
		mask.host = host;
		
		return mask;
	}
	
	/**
	 * Calculates the hash code for this mask
	 */
	@Override
	public int hashCode()
	{
		int hash = nick.toLowerCase().hashCode();
		
		hash = hash * 47 + user.toLowerCase().hashCode();
		hash = hash * 37 + host.toLowerCase().hashCode();
		
		return hash;
	}
	
	/**
	 * Gets weather this IRC mask is equal to another mask
	 */
	@Override
	public boolean equals(Object object)
	{
		if(object == null)
			return false;
		
		if(object == this)
			return true;
		
		if(object.getClass() != this.getClass())
			return false;
		
		IRCMask mask = (IRCMask) object;
		
		return nick.equalsIgnoreCase(mask.nick) &&
				user.equalsIgnoreCase(mask.user) &&
				host.equalsIgnoreCase(mask.host);
	}
	
	/**
	 * Compares this mask with another mask
	 */
	@Override
	public int compareTo(IRCMask mask)
	{
		int value = host.compareToIgnoreCase(mask.host);
		
		if(value == 0)
		{
			value = user.compareToIgnoreCase(mask.user);
			
			if(value == 0)
			{
				value = nick.compareToIgnoreCase(mask.nick);
			}
		}
		
		return value;
	}
	
	/**
	 * Converts the mask to a string without the nickname part
	 */
	public String toStringUser()
	{
		//User + host
		return user + "@" + host; 
	}
	
	@Override
	public String toString()
	{
		if(nick == null || nick.length() == 0)
		{
			//User + host
			return user + "@" + host; 
		}
		else
		{
			//Nick + user + host
			return nick + "!" + user + "@" + host;
		}
	}
	
	//Wildcard comparison with starting indexes
	private static boolean wildcardCompare(String data, String mask, int strPos, int wildPos)
	{
		char c;

		for (; wildPos < mask.length(); )
		{
			c = mask.charAt(wildPos++);		//Increases wildPos as well

			switch (c)
			{
				case '?':
					if (strPos == data.length())
					{
						return false;
					}

					++strPos;
					break;

				case '*':
					if (wildPos == mask.length())
					{
						//If * is the last character, accept rest of string
						return true;
					}

					//Collapse *s
					while (mask.charAt(wildPos) == '*')
					{
						//Increace for next part of loop
						++wildPos;

						if (wildPos == mask.length())
						{
							//If * is the last character, accept rest of string
							return true;
						}
					}

					//wildPos now points to the character after the *s

					while (strPos < data.length())
					{
						if (wildcardCompare(data, mask, strPos, wildPos))
						{
							return true;
						}

						++strPos;
					}

					//End of string
					return false;

				case '\\':
					if (wildPos != mask.length())
					{
						//Skip the \ character, fallthough to
						// treating the next character as normal (whatever it is)
						c = mask.charAt(wildPos);
						++wildPos;
					}

					//Otherwize c = the \ character

				default:
					if (strPos == data.length() || c != data.charAt(strPos))
					{
						return false;
					}

					++strPos;
					break;
			}
		}

		//End of wild string reached
		return strPos == data.length();
	}
	
	/**
	 * Performs a wildcard comparison between data and a mask
	 * 
	 * The comparison is case-insensitive
	 * 
	 * @param data The data to be checked
	 * @param mask The mask to check against
	 * @return True if the data matches the mask
	 */
	public static boolean wildcardCompare(String data, String mask)
	{
		return wildcardCompare(data.toLowerCase(), mask.toLowerCase(), 0, 0);
	}
	
	/**
	 * Performs a wildcard comparison between an IRCMask and a wildcard mask
	 * 
	 * The comparison is case-insensitive
	 * 
	 * @param data The IRCMask to be checked
	 * @param mask The wildcard mask to check against
	 * @return True if the data matches the mask
	 */
	public static boolean wildcardCompare(IRCMask data, String mask)
	{
		return wildcardCompare(data.toString(), mask);
	}
	
	/**
	 * Performs a wildcard comparison between this IRCMask and a wildcard mask
	 * 
	 * The comparison is case-insensitive
	 * 
	 * @param mask The wildcard mask to check against
	 * @return True if the data matches the mask
	 */
	public boolean wildcardCompareTo(String mask)
	{
		return wildcardCompare(this, mask);
	}
	
	/**
	 * Completes a wildcard mask by adding an empty nickname or hostname part
	 * 
	 * This must be done before wildcardCompareTo on a mask without ! and @ characters in
	 * 
	 * @param mask The mask to complete
	 * @return The completed mask
	 */
	public String completeWildMask(String mask)
	{
		//Check for user part
		if(mask.indexOf('@') == -1)
		{
			//Prepend user part
			mask = "*@" + mask;
		}
		
		//Check if it contains nick part
		if(mask.indexOf('!') == -1)
		{
			//Prepend nick part
			mask = "*!" + mask;
		}
		
		return mask;
	}
}
