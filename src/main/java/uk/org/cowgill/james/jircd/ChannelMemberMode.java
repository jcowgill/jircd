/*
   Copyright 2011 James Cowgill

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
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
	 * Finds the highest mode of this member
	 *
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
	void setAllModes(int mode)
	{
		this.mode = mode;
	}

	/**
	 * Sets a mode
	 * @param mode mode to set
	 */
	void setMode(int mode)
	{
		this.mode |= mode;
	}

	/**
	 * Clears a mode
	 * @param mode mode to clear
	 */
	void clearMode(int mode)
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

	/**
	 * Returns the mode string for this member
	 */
	public String toModeString()
	{
		String str = "+";

		if((mode & OWNER) != 0)
		{
			str += 'q';
		}
		if((mode & ADMIN) != 0)
		{
			str += 'a';
		}
		if((mode & OP) != 0)
		{
			str += 'o';
		}
		if((mode & HALFOP) != 0)
		{
			str += 'h';
		}
		if((mode & VOICE) != 0)
		{
			str += 'v';
		}

		if(str.length() == 1)
		{
			return "";
		}
		else
		{
			return str;
		}
	}

	/**
	 * Returns the prefix string for this member
	 *
	 * @param oneChar return only the highest prefix
	 */
	public String toPrefixString(boolean oneChar)
	{
		String str = "";

		if((mode & OWNER) != 0)
		{
			str += '~';
		}
		if((mode & ADMIN) != 0)
		{
			str += '&';
		}
		if((mode & OP) != 0)
		{
			str += '@';
		}
		if((mode & HALFOP) != 0)
		{
			str += '%';
		}
		if((mode & VOICE) != 0)
		{
			str += '+';
		}

		if(oneChar && str.length() >= 2)
		{
			return str.substring(0, 1);
		}
		else
		{
			return str;
		}
	}
}
