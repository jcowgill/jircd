package uk.org.cowgill.james.jircd;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Contains all the configuration information for a server instance
 * 
 * Configurations can be created and filled manually or use the built-in
 *  config reader which uses ConfigBlocks to parse the config file
 * 
 * @author James
 */
public final class Config
{
	/**
	 * The server name
	 */
	public String serverName;
	
	/**
	 * The server description
	 */
	public String serverDescription;
	
	/**
	 * Admin lines displayed to the user after issuing a /admin
	 */
	public String[] admin;
	
	/**
	 * MotD. This is formatted with IRC commands ready to send
	 */
	public String motdFormatted;
	
	/**
	 * Ports the server should listen on
	 */
	public int[] ports;
	
	/**
	 * List of accept lines
	 * 
	 * The list is ordered with the most sepific at the beginning
	 */
	public List<Accept> accepts;
	
	/**
	 * Map of operators
	 */
	public Map<String, Operator> operators;
	
	/**
	 * Collection of banned nicknames - checked when nickname changes
	 */
	public Collection<Ban> banNick;
	
	/**
	 * Collection of banned ip addresses - checked ASAP after connect
	 */
	public Collection<Ban> banIP;
	
	/**
	 * Collection of user and host bans - checked at end of registration
	 */
	public Collection<Ban> banUserHost;
	
	/**
	 * Collection of modules to load
	 * 
	 * The block parameter says the main class
	 * A sub-block called jar specifies a jar file to add to the class path
	 */
	public Collection<ConfigBlock> modules;
	
	/**
	 * Set of permissions granted to operators
	 */
	public int permissionsOp;
	
	/**
	 * Set of permissions granted to super operators
	 */
	public int permissionsSuperOp;
	
	/**
	 * Represents an accept entry
	 * 
	 * @author James
	 */
	public final static class Accept
	{
		/**
		 * IP mask for accept line
		 * 
		 * Accept line will be used if either ipmask or hostmask matches
		 */
		public String ipMask;
		
		/**
		 * Host mask for accept line
		 * 
		 * Accept line will be used if either ipmask or hostmask matches
		 */
		public String hostMask;
		
		/**
		 * Maximum number of connections using the same IP allowed when using this accept line
		 */
		public int maxClones;
		
		/**
		 * Reference to the accept line's connection class
		 */
		public ConnectionClass classLine;
	}
	
	/**
	 * An operator entry
	 * 
	 * @author James
	 */
	public final static class Operator
	{
		/**
		 * Mask which must be matched to use this operator line
		 */
		public String mask;
		
		/**
		 * Password for this operator in SHA-1 format
		 */
		public byte[] password;
		
		/**
		 * New connection class
		 */
		public ConnectionClass newClass;
		
		/**
		 * True if client becomes a super operator when using this line
		 */
		public boolean isSuperOp;
	}
	
	/**
	 * Represents a banning entry
	 * 
	 * @author James
	 */
	public final static class Ban
	{
		/**
		 * Mask used to check ban
		 */
		public String mask;
		
		/**
		 * Reason for banning
		 */
		public String reason;
	}

	public static Config parse(InputStream data) throws IOException
	{
            return null;
	}

}
