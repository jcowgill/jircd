package uk.org.cowgill.james.jircd;

/**
 * Exception which is thrown when an error is encountered parsing a config file
 * 
 * @author James
 */
public class ConfigException extends Exception
{
	private static final long serialVersionUID = 1L;
		
	/**
	 * The line number the exception was generated at in the file
	 * 
	 * If lineNo <= 0, no line or character was specified
	 */
	public final int lineNo;
		
	/**
	 * The character number the exception was generated at in the file
	 * 
	 * If charNo <= 0, no character is unknown
	 */
	public final int charNo;

	public ConfigException()
	{
		this.lineNo = 0;
		this.charNo = 0;
	}
	
	public ConfigException(String str)
	{
		super(str);
		this.lineNo = 0;
		this.charNo = 0;
	}
	
	public ConfigException(String str, Throwable err)
	{
		super(str, err);
		this.lineNo = 0;
		this.charNo = 0;
	}
	
	public ConfigException(Throwable err)
	{
		super(err);
		this.lineNo = 0;
		this.charNo = 0;
	}
	
	public ConfigException(String str, int lineNo, int charNo)
	{
		super(str);
		this.lineNo = lineNo;
		this.charNo = charNo;
	}
	
	ConfigException(String str, PushbackLineInputStream stream)
	{
		super(str);
		this.lineNo = stream.getLineNo();
		this.charNo = stream.getCharNo();
	}
	
	@Override
	public String toString()
	{
		return super.toString() + " at line " + lineNo + " col " + charNo;
	}
}
