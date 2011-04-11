package uk.org.cowgill.james.jircd;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import uk.org.cowgill.james.jircd.util.ModeType;

/**
 * Represents an IRC channel
 * 
 * @author James
 */
public class Channel
{
	/**
	 * Avaliable channel modes
	 * 
	 * <p>You are allowed to add ONOFF modes to this (do not add modes requiring parameters)
	 * <p>When adding modes, all existing channels will not have that mode set
	 */
	public static final Map<Character, ModeType> modes;
	
	/**
	 * The current onoff channel mode
	 */
	private long mode;

	public String key;
	
	public int limit;
	
	public Set<String> banList;
	
	public Set<String> banExceptList;
	
	public Set<String> inviteExceptList;
	
	public Map<Client, ChannelMemberMode> members;

	static
	{
		//Setup channel modes
		modes = new HashMap<Character, ModeType>();
		modes.put('q', ModeType.MemberList);
		modes.put('a', ModeType.MemberList);
		modes.put('o', ModeType.MemberList);
		modes.put('h', ModeType.MemberList);
		modes.put('v', ModeType.MemberList);
		modes.put('b', ModeType.List);
		modes.put('e', ModeType.List);
		modes.put('I', ModeType.List);
		modes.put('k', ModeType.Param);
		modes.put('k', ModeType.Param);
		modes.put('p', ModeType.OnOff);
		modes.put('s', ModeType.OnOff);
		modes.put('t', ModeType.OnOff);
		modes.put('n', ModeType.OnOff);
		modes.put('m', ModeType.OnOff);
		modes.put('i', ModeType.OnOff);
		modes.put('O', ModeType.OnOff);
	}
}
