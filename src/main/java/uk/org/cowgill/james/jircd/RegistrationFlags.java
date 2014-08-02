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
	 * Flag set when registration is complete
	 */
	public static final int RegComplete = 16;

	/**
	 * All the registration flags together
	 */
	public static final int AllFlags = 31;
}
