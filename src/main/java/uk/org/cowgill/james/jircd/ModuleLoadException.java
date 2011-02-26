package uk.org.cowgill.james.jircd;

/**
 * Exception which can be thrown when an error occurs in loading a module
 * 
 * @author James
 */
public class ModuleLoadException extends Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new ModuleLoadException
	 */
	public ModuleLoadException()
	{
	}

	/**
	 * Creates a new ModuleLoadException with a reason
	 * 
	 * @param reason the reason for throwing this exception
	 */
	public ModuleLoadException(String reason)
	{
		super(reason);
	}

	/**
	 * Creates a new ModuleLoadException with a cause
	 * 
	 * @param cause the exception which caused this exception
	 */
	public ModuleLoadException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * Creates a new ModuleLoadException with a reason and cause
	 * 
	 * @param reason the reason for throwing this exception
	 * @param cause the exception which caused this exception
	 */
	public ModuleLoadException(String reason, Throwable cause)
	{
		super(reason, cause);
	}
}
