package uk.org.cowgill.james.jircd;

final class NetworkClient extends Client
{

	@Override
	public void send(String data)
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean rawClose()
	{
		return false;
		// TODO Auto-generated method stub

	}

	@Override
	protected void changeClass(ConnectionClass clazz, boolean defaultClass)
	{
		//Default = no class changes
	}
	
	@Override
	public void restoreClass()
	{
		//Default = no class changes
	}
	
	@Override
	public String ipAddress()
	{
		return null;
	}
}
