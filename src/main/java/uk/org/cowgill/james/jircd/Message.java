package uk.org.cowgill.james.jircd;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A parsed irc message
 * 
 * @author James
 */
public class Message
{	
	/**
	 * The prefix (sender) of the message
	 */
	private String prefix;
	
	/**
	 * Command of the message
	 */
	private String command;
	
	/**
	 * Message parameters
	 */
	private ArrayList<String> parameters = new ArrayList<String>();

	/**
	 * Creates a new message with a blank prefix
	 * 
	 * @param command command for the message
	 */
	public Message(String command)
	{
		this(command, (String) null);
	}

	/**
	 * Creates a new message from a Client
	 * 
	 * @param command command for the message
	 * @param client origin of this message
	 */
	public Message(String command, Client client)
	{
		this(command, client.id.toString());
	}

	/**
	 * Creates a new message with an IRCMask prefix
	 * 
	 * @param command command for the message
	 * @param id origin of this message
	 */
	public Message(String command, IRCMask id)
	{
		this(command, id.toString());
	}
	
	/**
	 * Creates a new message
	 * 
	 * @param command command for the message
	 * @param prefix origin of this message
	 */
	public Message(String command, String prefix)
	{
		if(prefix == null)
		{
			this.prefix = "";
		}
		else
		{
			this.prefix = prefix;
		}
		
		if(command == null)
		{
			this.command = "";
		}
		else
		{
			this.command = command;
		}
	}
	
	/**
	 * Creates a new message from the server
	 * 
	 * @param command command for the message
	 * @return the new message
	 */
	public static Message newMessageFromServer(String command)
	{
		return new Message(command, Server.getServer().getConfig().serverName);
	}
	
	/**
	 * Creates a new message as a string from the server
	 * 
	 * @param command command for the message - you are allowed to add parameters here as well
	 * @return the new message as a string
	 */
	public static String newStringFromServer(String command)
	{
		return ":" + Server.getServer().getConfig().serverName + " " + command;
	}
	
	/**
	 * Returns this message's command in upper case
	 * @return this message's command in upper case
	 */
	public String getCommand()
	{
		return command;
	}
	
	/**
	 * Returns this message's prefix
	 * @return this message's prefix
	 */
	public String getPrefix()
	{
		return prefix;
	}
	
	/**
	 * Gets a parameter from the message
	 * @param index array index to get
	 * @return the parameter string
	 */
	public String getParam(int index)
	{
		return parameters.get(index);
	}
	
	/**
	 * Appends a parameter to this message
	 * @param str parameter to append
	 * @return this message
	 */
	public Message appendParam(String str)
	{
		parameters.add(str);
		return this;
	}

	@Override
	public int hashCode()
	{
		int hash = prefix.hashCode();
		
		hash = hash * 47 + command.hashCode();
		hash = hash * 37 + parameters.hashCode();
		
		return hash;
	}
	
	@Override
	public boolean equals(Object object)
	{
		if(object == null)
			return false;
		
		if(object == this)
			return true;
		
		if(object.getClass() != this.getClass())
			return false;
		
		Message other = (Message) object;
		
		return toString().equals(other.toString());
	}
	
	/**
	 * Returns an iteration which can iterate over the parameters array
	 * @return parameters iterator
	 */
	public Iterator<String> paramIterator()
	{
		final Iterator<String> source = parameters.iterator();
		
		return new Iterator<String>()
			{
				@Override
				public boolean hasNext()
				{
					return source.hasNext();
				}

				@Override
				public String next()
				{
					return source.next();
				}

				@Override
				public void remove()
				{
					throw new UnsupportedOperationException();
				}
			};
	}
	
	/**
	 * Returns the number of parameters in this message
	 * @return the number of parameters in this message
	 */
	public int paramCount()
	{
		return parameters.size();
	}
	
	@Override
	public String toString()
	{
		//Convert message to string
		final StringBuilder builder = new StringBuilder();
		
		//Check for prefix
		if(prefix.length() != 0)
		{
			builder.append(':');
			builder.append(prefix);
			builder.append(' ');
		}
		
		//Add command
		builder.append(command);
		
		//Add parameters
		final Iterator<String> paramIter = parameters.iterator();
		String param;
		
		while(paramIter.hasNext())
		{
			//Get param
			param = paramIter.next();
			
			//Add space before param
			builder.append(' ');
			
			//Check if parameter has spaces
			// of if it is not the last parameter
			if(!paramIter.hasNext() && param.indexOf(' ') != -1)
			{
				//Use prefix if last param has spaces
				builder.append(':');
			}
			
			builder.append(param);
		}
		
		return builder.toString();
	}
	
	/**
	 * Creates a new message from the given string
	 * 
	 * @param data message data
	 * @return the new message
	 */
	public static Message parse(String data)
	{
		int pos = 0;	//Current position in string
		int oldPos;
		
		String prefix = "";
		
		//Trim the data
		data = data.trim();
		
		try
		{
			//Extract prefix
			if(data.charAt(0) == ':')
			{
				//Find first space and extract until that
				pos = data.indexOf(' ') + 1;
				prefix = data.substring(1, pos - 1);
				
				//Suck up spaces
				while(data.charAt(pos) == ' ')
				{
					pos++;
				}
			}
		}
		catch(IndexOutOfBoundsException e)
		{
			//Invalid message
			return new Message(null);
		}
		
		//Extract command
		String command;
		
		oldPos = pos;
		pos = data.indexOf(' ', pos) + 1;
		
		if(pos == 0)
		{
			//Get till end of string
			command = data.substring(oldPos);
			pos = data.length();
		}
		else
		{
			//Get till space
			command = data.substring(oldPos, pos - 1);
		}
		
		//Create base message
		final Message baseMsg = new Message(command.toUpperCase(), prefix);
		
		
		//Extract parameters
		while(pos < data.length())
		{
			//Suck up spaces
			while(data.charAt(pos) == ' ')
			{
				pos++;
			}
			
			//Test if character is a :
			if(data.charAt(pos) == ':')
			{
				//Use all other characters as last parameter
				if(pos != (data.length() - 1))
				{
					baseMsg.appendParam(data.substring(pos + 1));
				}
				
				break;
			}
			else
			{
				//Find next space
				oldPos = pos;
				pos = data.indexOf(' ', pos) + 1;
				
				//Append parameter
				if(pos == 0)
				{
					baseMsg.appendParam(data.substring(oldPos));
					break;
				}
				else
				{
					baseMsg.appendParam(data.substring(oldPos, pos - 1));
				}
			}
		}
		
		//Return finished message
		return baseMsg;
	}
}
