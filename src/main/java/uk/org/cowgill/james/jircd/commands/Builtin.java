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
package uk.org.cowgill.james.jircd.commands;

import uk.org.cowgill.james.jircd.ConfigBlock;
import uk.org.cowgill.james.jircd.Module;
import uk.org.cowgill.james.jircd.ModuleLoadException;
import uk.org.cowgill.james.jircd.ModuleManager;
import uk.org.cowgill.james.jircd.Server;

public class Builtin implements Module
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
		modMan.registerCommand(new Join.JoinA());
		modMan.registerCommand(new Part());
		modMan.registerCommand(new Part.Leave());
		modMan.registerCommand(new Topic());
		modMan.registerCommand(new Kick());
		modMan.registerCommand(new Invite());
		modMan.registerCommand(new Names());
		modMan.registerCommand(new Protoctl());
		
		modMan.registerCommand(new Msg.PrivMsg());
		modMan.registerCommand(new Msg.Notice());
		
		modMan.registerCommand(new List());
		
		modMan.registerCommand(new Admin());
		modMan.registerCommand(new Info());
		modMan.registerCommand(new Links());
		modMan.registerCommand(new LUsers());
		modMan.registerCommand(new MotD());
		modMan.registerCommand(new Time());
		modMan.registerCommand(new Version());
		
		modMan.registerCommand(new Who());
		modMan.registerCommand(new Whois());
		modMan.registerCommand(new Ison());
		
		modMan.registerCommand(new Kill());
		modMan.registerCommand(new Rehash());
		modMan.registerCommand(new RestartDie.Die());
		modMan.registerCommand(new RestartDie.Restart());
		modMan.registerCommand(new Wall());
		modMan.registerCommand(new WallOps());
		
		modMan.registerCommand(new Stats());
		modMan.registerCommand(new Away());
		modMan.registerCommand(new UserHostIp.UserHost());
		modMan.registerCommand(new UserHostIp.UserIp());
		
		modMan.registerCommand(new Xyzzy());

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
