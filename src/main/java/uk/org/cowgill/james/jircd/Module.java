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
	 *  The module manager does not guarantee that registering of events
	 *  will work in the constructor.
	 *  
	 * Returning false causes this module to discarded
	 *  Even on returning false, the module MUST leave all other server objects
	 *  exactly how they were when entering this function
	 * 
	 * @param config configuration block for this module
	 * @return true on success or false on error
	 */
	public boolean startup(ConfigBlock config);
	
	/**
	 * Called after the server is rehashed
	 * @param config configuration block for this module
	 */
	public void rehash(ConfigBlock config);
	
	/**
	 * Called before the module is shutdown
	 * 
	 * The server will automatically remove all commands registered by the module when this returns
	 * 
	 * However, servlets and other modules loaded will not be removed.
	 */
	public void shutdown();
}
