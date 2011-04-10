package uk.org.cowgill.james.jircd;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * ClassLoader which allows jars to be added to the classpath at runtime
 * 
 * @author James
 */
final class JarClassLoader extends URLClassLoader
{
	private static JarClassLoader singleClassLoader;
	
	private JarClassLoader()
	{
		super(new URL[0], ClassLoader.getSystemClassLoader());
	}
	
	/**
	 * Returns the JarClassLoader
	 * @return the JarClassLoader
	 */
	public static JarClassLoader getClassLoader()
	{
		if(singleClassLoader == null)
		{
			singleClassLoader = new JarClassLoader();
		}
		
		return singleClassLoader;
	}
	
	/**
	 * Gets weather the Jar ClassLoader has been loaded
	 * 
	 * @return true if the class loader has been loaded
	 */
	public static boolean isLoaded()
	{
		return singleClassLoader != null;
	}

	/**
	 * Adds a URL to this class loader
	 * 
	 * @param paramURL URL to add
	 */
	@Override
	public void addURL(URL paramURL)
	{
		super.addURL(paramURL);
	}
}
