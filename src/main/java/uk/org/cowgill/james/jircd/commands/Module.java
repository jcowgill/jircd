package uk.org.cowgill.james.jircd.commands;

import uk.org.cowgill.james.jircd.ConfigBlock;
import uk.org.cowgill.james.jircd.ModuleLoadException;

public class Module implements uk.org.cowgill.james.jircd.Module
{
	@Override
	public boolean startup(ConfigBlock config) throws ModuleLoadException
	{
		// TODO Auto-generated method stub
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
