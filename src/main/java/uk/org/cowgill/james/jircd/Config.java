package uk.org.cowgill.james.jircd;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.security.MessageDigest;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

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
public final class Config implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(Config.class);

	/**
	 * ASCII character set encoder
	 */
	private static final CharsetEncoder cEncoder = Charset.forName("US-ASCII").newEncoder();
	
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
	 * MotD. Each entry in the array is 1 MotD line.
	 */
	public List<String> motd = new ArrayList<String>();
	
	/**
	 * Ports the server should listen on
	 */
	public Set<Integer> ports = new HashSet<Integer>();
	
	/**
	 * Map containing all connection classes
	 * 
	 * Used mostly during rehash to merge connection class changes
	 */
	public Map<String, ConnectionClass> classes = new HashMap<String, ConnectionClass>();
	
	/**
	 * List of accept lines
	 * 
	 * The list is ordered with the most specific at the beginning
	 */
	public List<Accept> accepts = new ArrayList<Accept>();
	
	/**
	 * Map of operators
	 */
	public Map<String, Operator> operators = new HashMap<String, Operator>();
	
	/**
	 * Collection of banned nicknames - checked when nickname changes
	 */
	public Collection<Ban> banNick = new ArrayList<Ban>();
	
	/**
	 * Collection of banned ip addresses - checked ASAP after connect
	 */
	public Collection<Ban> banIP = new ArrayList<Ban>();
	
	/**
	 * Collection of user and host bans - checked at end of registration
	 */
	public Collection<Ban> banUserHost = new ArrayList<Ban>();
	
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
	public int permissionsOp = 0;
	
	/**
	 * Set of permissions granted to super operators
	 */
	public int permissionsSuperOp = 0;
	
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
		 * 
		 * This can be null if the class should not be changed
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
		 * Reason for banning (or null if no reason)
		 */
		public String reason;
	}
	
	/**
	 * Finds a connection class
	 * 
	 * @param name name of the class to find
	 * @param mergeWith previous configuration to merge with or null
	 * @return the connection class found
	 * @throws ConfigException thrown if the class requested was not found
	 */
	private ConnectionClass findClass(String name, Config mergeWith) throws ConfigException
	{
		//Must be in this list
		if(classes.containsKey(name))
		{
			//Try other first, then use ours
			if(mergeWith != null)
			{
				ConnectionClass clazz = mergeWith.classes.get(name);
				
				if(clazz != null)
				{
					return clazz;
				}
			}

			return classes.get(name);
		}
		
		throw new ConfigException("Unknown class " + name);
	}

	/**
	 * Uses SHA-1 to hash a given password
	 * 
	 * @param password password to hash
	 * @return a byte array containing the hash
	 */
	public static byte[] passwordHash(String password) throws Exception
	{
		MessageDigest md = MessageDigest.getInstance("sha-1");
		
		return md.digest(cEncoder.encode(CharBuffer.wrap(password)).array());
	}
	
	/**
	 * Parses a hexadecimal string into a byte array
	 * 
	 * @param str String to parse
	 * @return The parsed byte array
	 * @throws ParseException Part of the string is not a hex character
	 */
	public static byte[] hexStringToByteArray(String str) throws ParseException
	{
	    int len = str.length();
	    byte[] data = new byte[len / 2];
	    
	    //Process each byte
	    for (int i = 0; i < len; i += 2)
	    {
	    	int digit = Character.digit(str.charAt(i), 16) << 4;
	    	
	    	if(digit < -1)
	    	{
	    		throw new ParseException("Password must be in hex format", i);
	    	}

	    	digit += Character.digit(str.charAt(i + 1), 16);
	    	
	    	if(digit < -1)
	    	{
	    		throw new ParseException("Password must be in hex format", i + 1);
	    	}
	    	
	    	data[i / 2] = (byte) digit;
	    }
	    
	    return data;
	}
	
	/**
	 * Calculates the permission mask from the given text input
	 * 
	 * @param permissions Permissions to calculate from
	 * @return Permissions as an int
	 * @throws ConfigException Thrown when an error occurs in calculating permissions
	 */
	private static int calculatePermissions(Collection<ConfigBlock> permissions) throws ConfigException
	{
		int notMask = 0;
		int mask = 0;
		
		try
		{
			//Process blocks
			if(permissions != null)
			{
				for(ConfigBlock headBlock : permissions)
				{
					for(Entry<String, Collection<ConfigBlock>> block : headBlock.subBlocks.entrySet())
					{
						//Is it a not?
						if(block.getKey().equals("not"))
						{
							//Use collection of parameters
							for(ConfigBlock perm : block.getValue())
							{
								notMask |= Permissions.class.getField(perm.param).getInt(null);
							}
						}
						else
						{
							//Use key
							mask |= Permissions.class.getField(block.getKey()).getInt(null);
						}
					}
				}
			}
		}
		catch (NoSuchFieldException e)
		{
			throw new ConfigException("Unknown permission " + e.getMessage(), e);
		}
		catch (Exception e)
		{
			throw new ConfigException("Error calculating permissions", e);
		}
		
		return mask & (~notMask);
	}

	/**
	 * Parses a new config file from the specified InputStream
	 *
	 * Parse errors and warnings are logged
	 * 
	 * @param data InputStream data is from
	 * @param mergeWith previous config to merge classes with
	 * 
	 * @return The new config object
	 * @throws ConfigException 
	 * @throws IOException when an IOExeption reading data occurs
	 */
	public static Config parse(InputStream data, Config mergeWith) throws ConfigException, IOException
	{ 
		//Parse config blocks
		final Config config = new Config();
		final ConfigBlock root = ConfigBlock.parse(data);

		//Read name and description
		config.serverName = root.getSubBlockParam("name");
		if(config.serverName.length() > 32)
		{
			throw new ConfigException("Server name cannot be more than 32 characters long");
		}
		
		config.serverDescription = root.getSubBlockParam("description");

		//Read admin lines
		final Collection<ConfigBlock> adminBlock = root.subBlocks.get("admin");
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
		
		if(motdBlock == null)
		{
			//Try motd string
			motdBlock = root.subBlocks.get("motdtext");
			
			if(motdBlock == null)
			{
				//No message of the day
				logger.warn("No message of the day found");
			}
			else
			{
				//Create message
				for(ConfigBlock line : motdBlock)
				{
					//Split line and add to list
					String[] subLines = line.param.split("\n");
					
					for(int i = 0; i < subLines.length; ++i)
					{
						config.motd.add(subLines[i].trim());
					}
				}
			}
		}
		else
		{
			try
			{
				//Read file line by line
				BufferedReader reader = new BufferedReader(
											new InputStreamReader(
											new FileInputStream(motdBlock.iterator().next().param)));
				
				String currLine = reader.readLine();
				
				while(currLine != null)
				{
					config.motd.add(currLine);
					currLine = reader.readLine();
				}
			}
			catch(IOException e)
			{
				//Propogate upwards
				throw new ConfigException("Failed to read MotD file", e);
			}
		}
		
		//Ports
		for(ConfigBlock block : root.subBlocks.get("listen"))
		{
			config.ports.add(block.getParamAsInt());
		}
		
		//Classes
		for(ConfigBlock block : root.subBlocks.get("class"))
		{
			//Read sub information
			ConnectionClass clazz = new ConnectionClass();
			
			clazz.readQueue = Integer.parseInt(block.getSubBlockParam("readq"));
			clazz.sendQueue = Integer.parseInt(block.getSubBlockParam("sendq"));
			clazz.maxLinks = Integer.parseInt(block.getSubBlockParam("maxlinks"));
			clazz.pingFreq = Integer.parseInt(block.getSubBlockParam("pingfreq"));
			
			//Read class name
			String name = block.param;
			if(name.trim().length() == 0)
			{
				throw new ConfigException("All classes must have names");
			}
			
			//Add to our config temporarily
			config.classes.put(name, clazz);
		}
		
		//Accept lines
		for(ConfigBlock block : root.subBlocks.get("accept"))
		{
			//Create new accept line
			Accept acceptLine = new Accept();
			
			//Must have host, ip or both
			acceptLine.hostMask = block.getSubBlockParamOptional("host");
			acceptLine.ipMask = block.getSubBlockParamOptional("ip");
			
			if(acceptLine.hostMask == null && acceptLine.ipMask == null)
			{
				throw new ConfigException("Accept line " + block.param + " must have a host or ip mask");
			}
			
			//Get clones
			String maxClones = block.getSubBlockParamOptional("maxclones");
			if(maxClones == null)
			{
				acceptLine.maxClones = Integer.MAX_VALUE;
			}
			else
			{
				acceptLine.maxClones = Integer.parseInt(maxClones);
			}

			//Get class
			acceptLine.classLine = config.findClass(block.getSubBlockParam("class"), mergeWith);
			
			//Add to config
			config.accepts.add(acceptLine);
		}
		
		//Operators
		for(ConfigBlock block : root.subBlocks.get("operator"))
		{
			//Create new operator line
			Operator operator = new Operator();
			
			//Get mask
			operator.mask = block.getSubBlockParam("mask");
			
			//Get isSuperOp
			operator.isSuperOp = block.subBlocks.containsKey("superop");
			
			//Find class
			String className = block.getSubBlockParamOptional("class");
			if(className != null)
			{
				operator.newClass = config.findClass(className, mergeWith);
			}
			
			//Read password
			try
			{
				operator.password = hexStringToByteArray(block.getSubBlockParam("password"));
			}
			catch (ParseException e)
			{
				throw new ConfigException("Error parsing operator password", e);
			}
			
			//Add to config
			String opName = block.param;
			if(opName.length() == 0)
			{
				throw new ConfigException("Operators must have names as parameters");
			}
			
			if(config.operators.put(opName, operator) != null)
			{
				throw new ConfigException("Duplicate operator " + opName);
			}
		}
		
		//Bans
		for(ConfigBlock block : root.subBlocks.get("ban"))
		{
			//Create new ban line
			Ban ban = new Ban();
			
			//Get mask
			ban.mask = block.getSubBlockParam("mask");
			
			//Get reason
			ban.reason = block.getSubBlockParamOptional("reason");
			
			//Place in relevant section
			if(block.param.equals("nick"))
			{
				config.banNick.add(ban);
			}
			else if(block.param.equals("user"))
			{
				config.banUserHost.add(ban);
			}
			else if(block.param.equals("ip"))
			{
				config.banIP.add(ban);
			}
			else
			{
				throw new ConfigException("Ban types must be in parameters (nick, user or ip)");
			}
		}

		//Modules
		config.modules = root.subBlocks.get("module");
		
		//Permissions
		final ConfigBlock permBlock = root.subBlocks.get("permissions").iterator().next();
		if(permBlock != null)
		{
			config.permissionsSuperOp = calculatePermissions(permBlock.subBlocks.get("superop"));
			config.permissionsOp = calculatePermissions(permBlock.subBlocks.get("op"));
		}
		
		//Merge classes with previous config
		for(Entry<String, ConnectionClass> classEntry : config.classes.entrySet())
		{
			//If class is in previous config, merge users
			ConnectionClass otherClass = mergeWith.classes.get(classEntry.getKey());
			
			if(otherClass != null)
			{
				//Copy class options
				otherClass.readQueue = classEntry.getValue().readQueue;
				otherClass.sendQueue = classEntry.getValue().sendQueue;
				otherClass.maxLinks = classEntry.getValue().maxLinks;
				otherClass.pingFreq = classEntry.getValue().pingFreq;
				
				//Use other class
				classEntry.setValue(otherClass);
			}
		}
		
		//Return parsed config
		return config;
	}
}
