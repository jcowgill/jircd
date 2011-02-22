package uk.org.cowgill.james.jircd;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * Controls loading and unloading of modules, and keeping track of registered commands
 * 
 * @author James
 */
public final class ModuleManager
{
	private static final Logger logger = Logger.getLogger(ModuleManager.class);
	
	/**
	 * Map of all loaded modules
	 */
	private HashMap<Class<?>, ModuleInfo> loadedModules = new HashMap<Class<?>, ModuleInfo>();
	
	/**
	 * Current module context. null = root context
	 */
	private ModuleInfo currentContext = null;
	
	/**
	 * Event which starts up the modules in the configuration file
	 * 
	 * @return true if all modules started successfully
	 */
	boolean serverStartupEvent()
	{
		//Load modules in config
		Collection<ConfigBlock> modules = Server.getServer().getConfig().modules;
		
		if(loadModules(modules) != modules.size())
		{
			//Module load failiure
			serverStopEvent();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Raises the rehash event in all modules
	 * 
	 * Note: Modules are not loaded or unloaded as a result of this
	 */
	void serverRehashEvent()
	{
		//
	}
	
	/**
	 * Shuts down all modules
	 */
	void serverStopEvent()
	{
		//Unload all modules
		
	}
	
	public Module loadModule(Class<?> moduleClass, ConfigBlock config)
	{
		//Find module in hashmap
		ModuleInfo info = loadedModules.get(moduleClass);
		
		//If null, create module
		if(info == null)
		{
			info = new ModuleInfo();
			info.loadCount = 1;
			
			try
			{
				info.module = (Module) moduleClass.newInstance();
				info.module.startup(config);
			}
			catch(Exception e)
			{
				logger.error("Error loading module", e);
				return null;
			}
			
			//Add module to loadedModules
			loadedModules.put(moduleClass, info);
		}
		
		//Return loaded module
		return info.module;
	}
	
	public Module loadModule(String className, ConfigBlock config)
	{
		//If config block contains a jar directive, load the jars
		Collection<ConfigBlock> jars = config.subBlocks.get("jar");
		boolean jarsUsed = false;
		
		if(jars != null)
		{
			for(ConfigBlock jarBlock : jars)
			{
				if(jarBlock.param.length() != 0)
				{
					//Add jar
					try
					{
						JarClassLoader.getClassLoader().addURL(new URL(jarBlock.param));
						jarsUsed = true;
					}
					catch(MalformedURLException e)
					{
						//Error in jar path
						logger.warn("Malformed JAR path: " + jarBlock.param, e);
					}
					catch(SecurityException e)
					{
						//Cannot create class loader
						logger.warn("Loading JARs at runtime requires the createClassLoader permission", e);
						break;		//Don't bother again
					}
				}
			}
		}
		
		//Attempt to find class
		Class<?> moduleClass = null;
		
		try
		{
			if(jarsUsed)
			{
				moduleClass = JarClassLoader.getClassLoader().loadClass(className);
			}
			else
			{
				moduleClass = ClassLoader.getSystemClassLoader().loadClass(className);
			}
		}
		catch(LinkageError e)
		{
			logger.error("Module class " + className + " is corrupt and cannot be loaded", e);
			return null;
		}
		catch(Exception e)
		{
			logger.error("Error loading module", e);
			return null;
		}
		
		//Call startup commands
		return loadModule(moduleClass, config);
	}
	
	public Module loadModule(ConfigBlock config)
	{
		//Load module using parameter as class name
		return loadModule(config.param, config);
	}
	
	/**
	 * Loads all the modules from the given module collection
	 * 
	 * @param moduleBlocks configuration blocks to load from
	 * @return number of loaded modules
	 */
	public int loadModules(Collection<ConfigBlock> moduleBlocks)
	{
		//Load each module in turn
		int moduleCount = 0;
		
		for(ConfigBlock block : moduleBlocks)
		{
			if(loadModule(block) != null)
			{
				moduleCount++;
			}
		}
		
		return moduleCount;
	}
	
	public void unloadModules(Collection<ConfigBlock> module)
	{
	}
	
	public void unloadModule(ConfigBlock module)
	{
	}
	
	public void unloadModule(Class<?> module)
	{
	}
	
	public void unloadModule(String module)
	{
	}
	
	public void unloadModule(Module module)
	{
	}
	
	public Iterator<Module> iterator()
	{
		return null;
	}
	
	public void registerCommand(Command command)
	{
	}
	
	public void registerCommand(Command command, Module module)
	{
	}
	
	public void unregisterCommand(Command command)
	{
	}
	
	public void unregisterCommand(Command command, Module module)
	{
	}
	
	public void executeCommand(Client client, Message msg)
	{
	}
	
	public Iterator<Command> commandIterator()
	{
		return null;
	}
	
	/**
	 * Stores information about a loaded module
	 * 
	 * @author James
	 */
	private class ModuleInfo
	{
		/**
		 * The loaded module
		 */
		public Module module;
		
		/**
		 * Number of references to this module (with dependencies)
		 */
		public int loadCount;
	}
}
