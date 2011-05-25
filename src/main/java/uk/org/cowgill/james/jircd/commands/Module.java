package uk.org.cowgill.james.jircd.commands;

import uk.org.cowgill.james.jircd.ConfigBlock;
import uk.org.cowgill.james.jircd.ModuleLoadException;
import uk.org.cowgill.james.jircd.ModuleManager;
import uk.org.cowgill.james.jircd.Server;

public class Module implements uk.org.cowgill.james.jircd.Module
{
	@Override
	public boolean startup(ConfigBlock config) throws ModuleLoadException
	{
		//Add commands
		ModuleManager modMan = Server.getServer().getModuleManager();
		
		modMan.registerCommand(new Nick());
		modMan.registerCommand(new User());
		modMan.registerCommand(new Quit());
		
		modMan.registerCommand(new Oper());
		modMan.registerCommand(new Mode());
		
		modMan.registerCommand(new Join());
		modMan.registerCommand(new Part());
		modMan.registerCommand(new Part.Leave());
		modMan.registerCommand(new Topic());
		modMan.registerCommand(new Kick());
		modMan.registerCommand(new Invite());
		modMan.registerCommand(new Names());
		
		modMan.registerCommand(new Msg.PrivMsg());
		modMan.registerCommand(new Msg.Notice());
		
		modMan.registerCommand(new List());
		
		return true;
	}

	@Override
	public void rehash(ConfigBlock config)
	{
		//I have no config
	}

	@Override
	public void shutdown()
	{
		//Don't care
	}
}
