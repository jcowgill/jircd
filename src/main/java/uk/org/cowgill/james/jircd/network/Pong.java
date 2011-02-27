package uk.org.cowgill.james.jircd.network;

import uk.org.cowgill.james.jircd.Client;
import uk.org.cowgill.james.jircd.Command;
import uk.org.cowgill.james.jircd.Message;
import uk.org.cowgill.james.jircd.RegistrationFlags;

/**
 * PONG command implementation
 * 
 * @author James
 */
class Pong implements Command
{
	@Override
	public void run(Client client, Message msg)
	{
		//Check for origin
		if(msg.paramCount() == 0)
		{
			client.send(Message.newStringFromServer("409 :No origin specified"));
		}
		
		//Ignore unless replying to a spoof check
		if(msg.paramCount() != 0 && client instanceof NetworkClient)
		{
			NetworkClient netClient = (NetworkClient) client;
			
			//Spoof check?
			if(netClient.spoofCheckChars != null)
			{
				//Compare
				if(msg.getParam(0).equals(netClient.spoofCheckChars))
				{
					client.setRegistrationFlag(RegistrationFlags.ServerCustom);
					netClient.spoofCheckChars = null;
				}
				else
				{
					//Bad pong
					client.send(Message.newStringFromServer(
							"513 :Bad Pong, To Connect type: /QUOTE PONG :"
							+ netClient.spoofCheckChars));
				}
			}
		}
	}

	@Override
	public int getMinParameters()
	{
		return 0;
	}

	@Override
	public String getName()
	{
		return "PONG";
	}

	@Override
	public int getFlags()
	{
		return FLAG_NORMAL | FLAG_REGISTRATION;
	}
}
