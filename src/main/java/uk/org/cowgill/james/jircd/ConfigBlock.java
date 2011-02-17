package uk.org.cowgill.james.jircd;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * Represents a generic block in a configuration file
 * 
 * Blocks have 0 or 1 parameter object and can contain
 * any number of sub-blocks each with a referencing name
 * 
 * @author James
 */
public class ConfigBlock
{	
	/**
	 * The blocks parameter
	 */
	public final String param;
	
	/**
	 * The child blocks of this block
	 */
	public final MultiMap<String, ConfigBlock> subBlocks;
	
	/**
	 * Creates a new config block
	 * 
	 * Block parameters are converted to empty strings if null is passed
	 * 
	 * @param param Parameter for block
	 * @param subBlocks The subblocks multimap for the block 
	 */
	public ConfigBlock(String param, MultiMap<String, ConfigBlock> subBlocks)
	{
		if(param == null)
		{
			this.param = "";
		}
		else
		{
			this.param = param;
		}
		
		this.subBlocks = subBlocks;
	}
	
	/**
	 * Returns the block parameter as an integer
	 * 
	 * @return The integer value
	 * @throws NumberFormatException If the parameter cannot be converted to an integer
	 */
	public int getParamAsInt() throws NumberFormatException
	{
		return Integer.parseInt(param);
	}
	
	/**
	 * Gets the parameter of the given sub block - requiring the block exists once
	 * 
	 * @param key The key of the sub-block
	 * @return The parameter
	 * @throws ConfigException If the block does not exist exactly once or has no parameter
	 */
	public String getSubBlockParam(String key) throws ConfigException
	{
		//Get collection
		Collection<ConfigBlock> blocks = subBlocks.get(key);
		
		if(blocks.size() == 1)
		{
			//Get block
			ConfigBlock block = blocks.iterator().next();
			
			//Return if param is not empty
			if(block.param.length() > 0)
			{
				return block.param;
			}
		}
		
		//Error occured
		throw new ConfigException("Directive " + key + " must exist exactly once and have a parameter");
	}
	
	//-----------------------------------------------------
	
	/**
	 * Skips any whitespace characters in the given stream
	 *
	 * This also handles single line comments at the start of lines
	 */
	private static void skipWhitespace(PushbackLineInputStream data) throws IOException
	{
		int c;
		
		do
		{
			c = data.read();
		}
		while(Character.isWhitespace(c));
		
		data.unread(c);
	}

	/**
	 * Called after a / has been read. Checks for c-style comments and skips them
	 * 
	 * Returns true if a comment was skipped. False if no comment was found; data will be unchanged.
	 */
	private static boolean checkAndSkipComment(PushbackLineInputStream data) throws IOException, ConfigException
	{
		int c = data.read();
		
		switch(c)
		{
			case -1:
				//EOF
				throw new ConfigException("Unexpected end of file", data);
		
			case '/':
				//Skip single line comment
				skipComment(data);
				return true;
				
			case '*':
				//Skip multi-line comment
				for(;;)
				{
					switch(data.read())
					{
						case -1:
							//EOF
							throw new ConfigException("Unexpected end of file", data);

						case '*':
							//Could be beginning of end of comment
							c = data.read();
							
							if(c == '/')
							{
								//Yes
								skipWhitespace(data);
								return true;
							}
							else
							{
								//No, continue comment
								data.unread(c);
								break;
							}
					}
				}
				
			default:
				//No comment
				data.unread(c);
				return false;
		}
	}
	
	/**
	 * Skips a single line comment which has already begun
	 */
	private static void skipComment(PushbackLineInputStream data) throws IOException
	{
		int c;
		
		for(;;)
		{
			c = data.read();
			switch(c)
			{
				case -1:
				case 10:
				case 13:
					//End of comment
					data.unread(c);
					skipWhitespace(data);
					return;
			}
		}
	}
	
	/**
	 * Parses a directive parameter and returns it
	 * 
	 * Will return on - ; { } EOF
	 */
	private static String parseParameter(PushbackLineInputStream data) throws IOException, ConfigException
	{
		//Terms can be separated with whitespace
		// terms are concated with one space replacing any whitespace
		StringBuilder outString = new StringBuilder();
		int c;
		boolean runLoop;

		for(;;)
		{
			c = data.read();

			//Check whitespace
			if(Character.isWhitespace(c))
			{
				//Insert space and skip other spaces
				outString.append(' ');
				skipWhitespace(data);
				c = data.read();
			}

			//Check special characters
			switch(c)
			{
				case -1:
				case '}':
				case ';':
				case '{':
					//Termination characters
					data.unread(c);
					return outString.toString();

				case '"':
					//Begin quotes - handle specially
					for(runLoop = true; runLoop;)
					{
						c = data.read();
						
						switch(c)
						{
							case -1:
								//EOF
								throw new ConfigException("Unexpected end of file", data);
	
							case '"':
								//End of string
								runLoop = false;
								break;

							default:
								outString.append(c);
								break;
						}
					}
					break;

				case '#':
					//Skip single line comment
					skipComment(data);
					break;
					
				case '/':
					//Could be start of comment
					if(checkAndSkipComment(data))
					{
						break;
					}

					//Or not, put back and fallthrough to write /

				default:
					//Copy verbatim to output
					outString.append(c);
					break;
			}
		}
	}
	
	/**
	 * Parses a list of directives and puts them in a multi-map
	 *
	 * If topLevel is true, will throw on }. Otherwise will throw on EOF
	 * 
	 * Ends only on EOF or end brace
	 */
	private static MultiMap<String, ConfigBlock> parseDirectives(PushbackLineInputStream data, boolean topLevel)
				throws ConfigException, IOException
	{
		//Loop until an EOF or } is found
		MultiMap<String, ConfigBlock> map = new MultiHashMap<String, ConfigBlock>();
		
		while(parseDirective(data, map))
			;
		
		//Check final char
		switch(data.read())
		{
			case -1:
				//EOF
				if(!topLevel)
				{
					throw new ConfigException("Unexpected end of file", data);
				}
				break;

			case '}':
				//Closing brace
				if(topLevel)
				{
					throw new ConfigException("Unexpected }", data);
				}
				break;

		}

		return map;
	}
	
	/**
	 * Parses a single directive from the config file
	 * 
	 * Returns true if the map was updated
	 * 	false on EOF or end brace (use data.read to find out)
	 */
	private static boolean parseDirective(PushbackLineInputStream data,
			MultiMap<String, ConfigBlock> map) throws ConfigException, IOException
	{
		int c;
		boolean runLoop = true;
		
		//Check for characters at the start
		do
		{
			skipWhitespace(data);
			
			//Check valid things at the start
			c = data.read();
			switch(c)
			{
				case -1:
				case '}':
					//EOF / end brace
					data.unread(c);
					return false;
					
				case ';':
					//Empty directive, ignore
					break;
					
				case '{':
					//Empty block, parse sub directives and merge with map
					map.putAll(parseDirectives(data, false));
					
					//Expect end brace
					skipWhitespace(data);
					if(data.read() == -1)
					{
						throw new ConfigException("Unexpected end of file", data);
					}
					
					return true;
					
				case '#':
					//Single line comment
					skipComment(data);
					break;
					
				case '/':
					//Could be multi-line comment
					if(checkAndSkipComment(data))
					{
						break;
					}
					else
					{
						//Not a comment. Put back and fallthough
						data.unread('/');
					}
					
				default:
					//No special characters
					runLoop = false;
					break;
			}
		}
		while(runLoop);
		
		data.unread(c);
		
		//Loop around reading directive name
		StringBuilder dName = new StringBuilder();
		String param = null;
		
		for(;;)
		{
			c = data.read();

			//Check whitespace
			if(Character.isWhitespace(c))
			{
				//End of directive name, parse parameter
				skipWhitespace(data);
				param = parseParameter(data);
				
				//What data caused the end of the parameter
				c = data.read();
				
				// c = character after param and will be a special character
			}
			
			//Handle special chars
			switch(c)
			{
				case -1:
					//End of file???
					throw new ConfigException("Unexpected end of file", data);
					
				case ';':
					//End of directive - name only
					map.putValue(dName.toString(), new ConfigBlock(param, null));
					return true;
					
				case '{':
					//Start subblock
					map.putValue(dName.toString(), new ConfigBlock(param, parseDirectives(data, false)));
					return true;
					
				case '}':
					//End of block???
					throw new ConfigException("Unexpected }", data);
					
				case '#':
					//Single line comment
					skipComment(data);
					break;
					
				case '/':
					//Could be multi-line comment
					if(checkAndSkipComment(data))
					{
						break;
					}
					
					//Or else fallthrough to write character
					
				default:
					//Add to name
					dName.append(c);
					break;
			}
		}
	}
	
	/**
	 * Parses the given string data into a heirachy of ConfigBlocks
	 * 
	 * @param data The data stream to parse into blocks
	 * @return The config block
	 * @throws IOException Thrown when an IO error occurs in the input stream
	 * @throws ConfigException Thrown when an error occurs in parsing the config file
	 */
	public static ConfigBlock parse(InputStream data) throws IOException, ConfigException
	{
		/*
		 
		 hello "blah";
		 op 7;
		 op yuo;
		 
		 ui 76
		 {
		 	ty;
		 	uy "to";
		 	oiu {};;;;
		 }
		 */
		
		return new ConfigBlock(null, parseDirectives(new PushbackLineInputStream(data), true));
	}
}
