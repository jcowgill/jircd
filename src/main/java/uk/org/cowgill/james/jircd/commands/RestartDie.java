package uk.org.cowgill.james.jircd.commands;

import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.Permissions;
import uk.org.cowgill.james.jircd.Server;
import org.apache.log4j.Logger;

/**
 * The RESTART and DIE commands - restarts or dies the server
 * 
 * @author James
 */
public abstract class RestartDie implements Command
{
	private static final Logger logger = Logger.getLogger(RestartDie.class);
	
	@Override
	public void run(Client client, Message msg)
	{
		//Must be an operator
		if(client.hasPermission(Permissions.restartDie))
		{
			logger.warn("Requesting server die or restart from " + client.id.toString());
			this.action("Request from " + client.id.nick + " (" + msg.getParam(0) + ")");
		}
		else
		{
			logger.warn(client.id.toString() + " failed to restart or die the server");
			client.send(client.newNickMessage("481").appendParam(getName() + ": Permission Denied"));
		}
	}

	@Override
	public int getMinParameters()
	{
		return 1;
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}

	protected abstract void action(String reason);
	
	/**
	 * The DIE command - stops the server
	 * 
	 * @author James
	 */
	public static class Die extends RestartDie
	{
		@Override
		public String getName()
		{
			return "DIE";
		}

		@Override
		protected void action(String reason)
		{
			Server.getServer().requestStop(reason);
		}
	}
	
	/**
	 * The RESTART command - restarts the server
	 * 
	 * @author James
	 */
	public static class Restart extends RestartDie
	{
		@Override
		public String getName()
		{
			return "RESTART";
		}

		@Override
		protected void action(String reason)
		{
			Server.getServer().requestRestart(reason);
		}
	}
}
