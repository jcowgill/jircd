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
 * Collection of flags used to mark whether clients can accept protocol enhancements
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
	public final static int NamesX = 1;

	/**
	 * Adds the username and hostname to /NAMES commands
	 */
	public final static int UhNames = 2;
}
