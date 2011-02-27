package uk.org.cowgill.james.jircd;

/**
 * Represents a server connection class
 * 
 * @author James
 */
public class ConnectionClass
{
	/**
	 * The ping frequency of connections on this class in seconds
	 */
	public int pingFreq;
	
	/**
	 * Maximum number of connections that can use this class
	 */
	public int maxLinks;
	
	/**
	 * Size of send buffer in bytes
	 */
	public int sendQueue;
	
	/**
	 * Size of read buffer in bytes
	 */
	public int readQueue;
	
	/**
	 * True if connections in this class gain the restricted (+r) flag
	 */
	public boolean restricted;
	
	/**
	 * Current number of connections using this class
	 */
	public int currentLinks;
}
