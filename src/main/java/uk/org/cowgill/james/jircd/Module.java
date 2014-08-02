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
 * Interface used to receive module events
 *
 * @author James
 */
public interface Module
{
	/**
	 * Called for a module to run its startup code
	 *
	 * Initialisation should occur here rather than in the constructor
	 *
	 * Returning false indicates that this module failed to load
	 *
	 * @param config configuration block for this module
	 * @return true on success or false on error
	 */
	public boolean startup(ConfigBlock config) throws ModuleLoadException;

	/**
	 * Called after the server is rehashed
	 *
	 * Modules are not loaded or unloaded as a result of a rehash.
	 * If the module line is dropped, null is passed as the config.
	 *
	 * @param config configuration block for this module (can be null, see above)
	 */
	public void rehash(ConfigBlock config);

	/**
	 * Called before the module is shutdown
	 *
	 * This is called after the network system has been shutdown (all clients have already been disconnected)
	 */
	public void shutdown();
}
