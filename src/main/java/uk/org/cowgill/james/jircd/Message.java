package uk.org.cowgill.james.jircd;

import java.util.Iterator;

/**
 * A parsed irc message
 * 
 * @author James
 */
public class Message
{
	private static final String BLANK_STR = "";
	private static final String[] EMPTY_ARRAY = new String[0];
	
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
	private String[] parameters;
	
	/**
	 * Returns this message's command
	 * @return this message's command
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
		return parameters[index];
	}
	
	/**
	 * Returns an iteration which can iterate over the parameters array
	 * @return parameters iterator
	 */
	public Iterator<String> paramIterator()
	{
		return new Iterator<String>()
			{
				private int pos = 0;
				
			
				@Override
				public boolean hasNext()
				{
					return parameters.length < pos;
				}

				@Override
				public String next()
				{
					return parameters[pos++];
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
		return parameters.length;
	}
	
	/**
	 * Creates a new message from the given string
	 * 
	 * @param data message data
	 * @return the new message
	 */
	public static Message parse(String data)
	{
		//TODO message parser
		return null;
	}
}
