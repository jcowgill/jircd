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
 * Implementation for a command event
 * 
 * @author James
 */
public interface Command
{
	/**
	 * Allows a command to be run from a client after registration
	 */
	public static final int FLAG_NORMAL = 1;

	/**
	 * Allows a command to be run from a client before and during registration
	 */
	public static final int FLAG_REGISTRATION = 2;
	
	/**
	 * Event occurs when a command is received by any client
	 * 
	 * @param client client the message was received from
	 * @param msg message object representing the message received
	 */
	public void run(Client client, Message msg);
	
	/**
	 * Gets the minimum number of parameters required to run this command
	 * 
	 * @return parameters required for this command
	 */
	public int getMinParameters();
	
	/**
	 * Gets the name of this command used to tell when to run the command
	 * 
	 * This method should return the command in UPPER CASE
	 * 
	 * @return the name of this command
	 */
	public String getName();
	
	/**
	 * Returns flags about the behaviour of the command
	 * 
	 * Generally, most commands should return FLAG_NORMAL
	 * 
	 * @return flags about the behaviour of the command
	 */
	public int getFlags();
}
