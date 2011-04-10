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
