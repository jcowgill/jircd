package uk.org.cowgill.james.jircd.util;

/**
 * Contains the avaliable types of mode used by the mode parser
 * 
 * @author James
 */
public enum ModeType
{
	/**
	 * Mode can either be on or off
	 */
	OnOff,
	
	/**
	 * Mode has 1 parameter when set
	 */
	Param,
	
	/**
	 * Mode is a list of strings
	 */
	List,
	
	/**
	 * Mode is a list of clients in a channel (invalid for user modes)
	 */
	MemberList,
}
