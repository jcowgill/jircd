package uk.org.cowgill.james.jircd;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
	private HashMap<Class<?>, Module> loadedModules = new HashMap<Class<?>, Module>();
	
	/**
	 * Map of all commands on the system
	 */
	private HashMap<String, CommandInfo> commands = new HashMap<String, CommandInfo>();
	
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
		HashSet<Class<?>> rehashed = new HashSet<Class<?>>();
		
		//Rehash all modules
		for(ConfigBlock block : Server.getServer().getConfig().modules)
		{
			//Get class
			Class<?> clazz = getClassFromString(block.param);

			//Find in loaded modules
			Module module = loadedModules.get(clazz);
			
			if(module == null)
			{
				//Not already loaded
				logger.warn("Module " + block.param + " has not been loaded - new modules require a server restart");
				continue;
			}
			
			//Rehash module
			try
			{
				module.rehash(block);
			}
			catch(Exception e)
			{
				logger.error("Module " + block.param + " threw exception while rehashing", e);
			}
			
			rehashed.add(clazz);
		}
		
		//Anything which has not been rehashed will be notified
		for(Class<?> clazz : loadedModules.keySet())
		{
			if(!rehashed.contains(clazz))
			{
				logger.warn("Module " + clazz.getName() + " has not been unloaded - this requires a server restart");

				//Rehash module
				try
				{
					loadedModules.get(clazz).rehash(null);
				}
				catch(Exception e)
				{
					logger.error("Module " + clazz.getName() + " threw exception while rehashing", e);
				}
			}
		}
	}
	
	/**
	 * Shuts down all modules
	 */
	void serverStopEvent()
	{
		//Unload all modules
		for(Module module : loadedModules.values())
		{
			module.shutdown();
		}
		
		loadedModules.clear();
	}
	
	/**
	 * Finds the class for a given string
	 * 
	 * Errors are displayed
	 * 
	 * @param module Module string
	 * @return the class or null on error
	 */
	private static Class<?> getClassFromString(String module)
	{
		try
		{
			if(JarClassLoader.isLoaded())
			{
				return JarClassLoader.getClassLoader().loadClass(module);
			}
			else
			{
				return ClassLoader.getSystemClassLoader().loadClass(module);
			}
		}
		catch(LinkageError e)
		{
			logger.error("Module class " + module + " is corrupt and cannot be loaded", e);
			return null;
		}
		catch(Exception e)
		{
			logger.error("Error loading module", e);
			return null;
		}
	}
	
	/**
	 * Loads a module with the given class and configuration
	 * 
	 * This silently returns true if the module has been loaded
	 * 
	 * @param moduleClass class of the module to load
	 * @param config configuration
	 * @return true if the module has been loaded
	 */
	public boolean loadModule(Class<?> moduleClass, ConfigBlock config)
	{
		//Find module in hashmap
		Module module = loadedModules.get(moduleClass);
		
		//If null, create module
		if(module == null)
		{
			try
			{
				module = (Module) moduleClass.newInstance();
				if(!module.startup(config))
				{
					//Failed to load
					logger.error("Error loading module " + moduleClass.getName());
					return false;
				}
			}
			catch(Exception e)
			{
				logger.error("Error loading module " + moduleClass.getName(), e);
				return false;
			}
			
			//Add module to loadedModules
			loadedModules.put(moduleClass, module);
		}
		
		//Module loaded
		return true;
	}

	/**
	 * Loads a module with the given class name and configuration
	 * 
	 * This silently returns true if the module has been loaded.
	 * This function will also attempt to load any jar files in the module configuration.
	 * 
	 * @param className class name of the module to load
	 * @param config configuration
	 * @return true if the module has been loaded
	 */
	public boolean loadModule(String className, ConfigBlock config)
	{
		//If config block contains a jar directive, load the jars
		Collection<ConfigBlock> jars = config.subBlocks.get("jar");
		
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
		Class<?> moduleClass = getClassFromString(className);
		
		if(moduleClass == null)
		{
			return false;
		}
		
		//Call startup commands
		return loadModule(moduleClass, config);
	}

	/**
	 * Loads a module with the given configuration
	 * 
	 * This silently returns true if the module has been loaded.
	 * This function will also attempt to load any jar files in the module configuration.
	 * 
	 * @param config configuration for module
	 * @return true if the module has been loaded
	 */
	public boolean loadModule(ConfigBlock config)
	{
		//Load module using parameter as class name
		return loadModule(config.param, config);
	}
	
	/**
	 * Loads all the modules from the given module collection
	 * 
	 * @param moduleBlocks configuration blocks to load from
	 * @return number of modules which were loaded successfully
	 */
	public int loadModules(Collection<ConfigBlock> moduleBlocks)
	{
		//Load each module in turn
		int moduleCount = 0;
		
		for(ConfigBlock block : moduleBlocks)
		{
			if(loadModule(block))
			{
				moduleCount++;
			}
		}
		
		return moduleCount;
	}
	
	/**
	 * Registers a command to receive events when it is used by a client
	 * 
	 * @param command Command object to register
	 * @throws ModuleLoadException thrown if the command has already been registered
	 */
	public void registerCommand(Command command) throws ModuleLoadException
	{
		//Get module name and add it
		String modName = command.getName();

		//If we already have this command, raise exception
		if(commands.containsKey(modName))
		{
			throw new ModuleLoadException("Command " + modName + " is already registered");
		}
		
		//Add command if it has any valid flags
		if((command.getFlags() & (Command.FLAG_NORMAL | Command.FLAG_REGISTRATION)) != 0)
		{
			commands.put(modName, new CommandInfo(command));
		}
	}
	
	/**
	 * Unregisters a command
	 * 
	 * You do not need to do this when shutting down - it is done automatically
	 * 
	 * @param command Command to unregister
	 */
	public void unregisterCommand(Command command)
	{
		//Find module
		String modName = command.getName();
		CommandInfo foundModule = commands.get(modName);
		
		//Delete of the command is the same
		if(foundModule.getCommand() == command)
		{
			commands.remove(modName);
		}
	}
	
	/**
	 * Dispatches a message to the correct command handler
	 * 
	 * @param client Client who sent the message
	 * @param msg Message to dispatch
	 */
	public void executeCommand(Client client, Message msg)
	{
		//Find command
		boolean registeredCheck = false;
		CommandInfo commandInfo = commands.get(msg.getCommand());
		
		if(commandInfo == null)
		{
			//Only notify if command != nothing
			if(msg.getCommand().trim().length() != 0)
			{
				//Unknown Command
				client.send(Message.newMessageFromServer("421")
						.appendParam(msg.getCommand())
						.appendParam("Unknown Command"));
			}

			return;
		}
		
		//Extract command
		Command command = commandInfo.getCommand();
		
		//Check max params
		if(msg.paramCount() < command.getMinParameters())
		{
			//Not Enough Parameters
			client.send(Message.newMessageFromServer("461")
					.appendParam(msg.getCommand())
					.appendParam("Not enough parameters"));

			return;
		}
		
		//Check if registered
		if(client.isRegistered())
		{
			//Allow registered only
			if((command.getFlags() & Command.FLAG_NORMAL) == 0)
			{
				client.send(Message.newMessageFromServer("462")
								.appendParam(msg.getCommand())
								.appendParam("You cannot reregister"));
				
				return;
			}
		}
		else
		{
			//Allow registration commands only
			if((command.getFlags() & Command.FLAG_REGISTRATION) == 0)
			{
				client.send(Message.newStringFromServer("451 :You have not registered"));
				return;
			}
			
			registeredCheck = true;
		}
		
		//Increment execute counter
		commandInfo.incrementCounter();
		
		try
		{
			//Dispatch message
			command.run(client, msg);
		}
		catch(Exception e)
		{
			logger.error("Exception occured while dispatching command", e);
		}
		
		//Check if registered
		if(registeredCheck)
		{
			client.registeredEvent();
		}
	}
	
	/**
	 * Returns an unmodifiable map of all registered commands
	 * @return an unmodifiable map of all registered commands
	 */
	public Map<String, CommandInfo> getCommands()
	{
		return Collections.unmodifiableMap(commands);
	}
	
	/**
	 * Information about a command
	 * 
	 * @author James
	 */
	public static class CommandInfo
	{
		private final Command command;
		private int timesRun;
		
		/**
		 * Creates a new command information class from a command
		 * 
		 * @param command command to use
		 */
		CommandInfo(Command command)
		{
			this.command = command;
		}
		
		/**
		 * Increment numer of times this command has been run
		 */
		void incrementCounter()
		{
			++timesRun;
		}
		
		/**
		 * Gets the number of times this command has been run
		 */
		public int getTimesRun()
		{
			return timesRun;
		}
		
		/**
		 * Gets the raw command interface
		 */
		public Command getCommand()
		{
			return command;
		}
	}
}
