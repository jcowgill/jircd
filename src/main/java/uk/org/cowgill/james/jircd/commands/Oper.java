package uk.org.cowgill.james.jircd.commands;

import java.nio.charset.CharacterCodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Config;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.Server;

import org.apache.log4j.Logger;

/**
 * The OPER command - authenticates as an IRC operator
 * 
 * @author James
 */
public class Oper implements Command
{
	private static final Logger logger = Logger.getLogger(Oper.class);
	
	@Override
	public void run(Client client, Message msg)
	{
		//Get parameters
		String name = msg.getParam(0);
		String password = msg.getParam(1);
		
		//Find operator name
		Config.Operator operator = Server.getServer().getConfig().operators.get(name);
		
		if(operator == null || !client.id.wildcardCompareTo(operator.mask))
		{
			//No valid oper lines
			logOperMsg(false, client, name, false);
			client.send(client.newNickMessage("491").appendParam("No O-Lines for your host"));
			return;
		}
		
		//Hash password
		byte[] passwordHash = null;
		
		try
		{
			passwordHash = Config.passwordHash(password);
		}
		catch(NoSuchAlgorithmException e)
		{
			logger.warn("Exception while hashing OPER request ", e);
		}
		catch(CharacterCodingException e)
		{
			logger.warn("Exception while hashing OPER request ", e);
		}
		
		//Check password
		if(!Arrays.equals(passwordHash, operator.password))
		{
			//Invalid Password
			logOperMsg(false, client, name, true);
			client.send(client.newNickMessage("464").appendParam("Password Incorrect"));
			return;
		}
		
		//Make operator
		logOperMsg(true, client, name, false);
		client.changeClass(operator.newClass);
		client.setMode(operator.isSuperOp ? 'O' : 'o', true);
	}
	
	/**
	 * Logs an operator message
	 */
	private static void logOperMsg(boolean valid, Client client, String operName, boolean badPasswd)
	{
		StringBuilder msg = new StringBuilder();
		
		if(valid)
		{
			msg.append("Valid OPER attempt from ");
		}
		else
		{
			msg.append("Failed OPER attempt from ");
		}
		
		msg.append(client.id.toString());
		msg.append("( ");
		msg.append(client.getIpAddress());
		msg.append("), using oper name ");
		msg.append(operName);
		
		if(badPasswd)
		{
			msg.append(". Incorrect Password");
		}
		else
		{
			msg.append('.');
		}
		
		if(valid)
		{
			logger.info(msg.toString());
		}
		else
		{
			logger.warn(msg.toString());
		}
	}

	@Override
	public int getMinParameters()
	{
		return 2;
	}

	@Override
	public String getName()
	{
		return "OPER";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL;
	}
}
