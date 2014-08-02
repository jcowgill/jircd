/*
   Copyright 2011 James Cowgill

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
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
