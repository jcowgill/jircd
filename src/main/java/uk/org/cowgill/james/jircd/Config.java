package uk.org.cowgill.james.jircd;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Contains all the configuration information for a server instance
 * 
 * Configurations can be created and filled manually or use the built-in
 *  config reader which uses ConfigBlocks to parse the config file
 * 
 * Valid config files never contain nulls in ANY field;
 * 
 * @author James
 */
public final class Config
{
	private final static Logger logger = LoggerFactory.getLogger(Config.class);
	
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
	public Set<Integer> ports;
	
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

	/**
	 * Parses a new config file from the specified InputStream
	 *
	 * Parse errors and warnings are logged
	 * 
	 * @param data InputStream data is from
	 * @return The new config object
	 * @throws IOException when an IOExeption reading data occurs
	 */
	public static Config parse(InputStream data) throws IOException
	{ 
		//Parse config blocks
		Config config= new Config();
		ConfigBlock root = ConfigBlock.parse(data);

		//Read name and description
		config.serverName = root.getSubBlockParam("name");
		config.serverDescription = root.getSubBlockParam("description");

		//Read admin lines
		Collection<ConfigBlock> adminBlock = root.subBlocks.get("admin");
		if(adminBlock == null)
		{
			//Empty admin block
			config.admin = new String[0];
		}
		else
		{
			//Fill admin sections
			config.admin = new String[adminBlock.size()];

			int i = 0;
			for(ConfigBlock block : adminBlock)
			{
				config.admin[i] = block.param;
				i++;	
			}
		}

		//MotD
		Collection<ConfigBlock> motdBlock = root.subBlocks.get("motdfile");
		String motdBaseStr;
		
		if(motdBlock == null)
		{
			//Try motd string
			motdBlock = root.subBlocks.get("motdtext");
			
			if(motdBlock == null)
			{
				//No message of the day
				motdBaseStr = "";
				logger.warn("No message of the day found");
			}
			else
			{
				motdBaseStr = motdBlock.iter;
			}
		}
		
		TODO MOTD

		//Ports to listen on
	}

}
