package uk.org.cowgill.james.jircd;

import java.io.File;
import java.net.URL;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class responsible for reading command line options and doing basic server setup
 * 
 * @author James
 */
class ServerStarter
{
	private final static Logger logger =
		LoggerFactory.getLogger(ServerStarter.class);
	
	public static void main(String[] args)
	{
		//JIRC Server entry point
		logger.info("JIRC Server 1.0  By James Cowgill");
		
		//Config file on command line?
		String configFileName;
		if(args.length != 0)
		{
			configFileName = args[0];
		}
		else
		{
			configFileName = "ircd.conf";
		}
		
		//Attempt to open config file
		File configFile;
		
		//Try to read from current directory first, then from JAR directory
		configFile = new File(configFileName);
		if(!configFile.canRead())
		{
			//Try jar path
			URL jarFile = ServerStarter.class.getProtectionDomain().getCodeSource().getLocation();
			if(jarFile.getProtocol().equals("file"))
			{
				configFile = new File(jarFile.getPath() + "/" + configFileName);
				if(!configFile.canRead())
				{
					//Cannot find config file
					logger.error("Cannot open configuration file \"" + configFileName + "\"");
					return;
				}
			}
		}
		
		//Create server and read config file
		Server server = new Server(configFile);
		if(!server.rehash())
		{
			return;
		}
		
		//TODO Run server
		
	}
	public static class CrashJava {
		  public String toString() {
		    return "CrashJava address: " + this + "\n";
		  }
		  public static void main(String[] args) {
		    Vector v = new Vector();
		    for(int i = 0; i < 10; i++)
		      v.addElement(new CrashJava());
		    System.out.println(v);
		  }
		}
}
