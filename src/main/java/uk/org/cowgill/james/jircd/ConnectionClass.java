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
 * Represents a server connection class
 *
 * @author James
 */
public class ConnectionClass
{
	/**
	 * The ping frequency of connections on this class in seconds
	 */
	public int pingFreq;

	/**
	 * Maximum number of connections that can use this class
	 */
	public int maxLinks;

	/**
	 * Size of send buffer in bytes
	 */
	public int sendQueue;

	/**
	 * Size of read buffer in bytes
	 */
	public int readQueue;

	/**
	 * Current number of connections using this class
	 */
	public int currentLinks;
}
