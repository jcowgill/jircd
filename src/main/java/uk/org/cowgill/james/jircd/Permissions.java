package uk.org.cowgill.james.jircd;

/**
 * Class containing clinet permission constants
 * 
 * @author James
 */
public final class Permissions
{
    /**
     * Ability to reload the server config file remotely
     */
    public final static int rehash = 0x1;

    /**
     * Ability to restart / stop the server remotely
     */
    public final static int restartDie = 0x2;

    /**
     * Ability to send WALLOPS (all ops always receive them)
     */
    public final static int wallops = 0x4;

    /**
     * Ability to send WALL (WALLOPS to all clients)
     */
    public final static int wall = 0x8;

    /**
     * Ability to kill another user
     */
    public final static int kill = 0x10;

    /**
     * Ability to join channels as an administrator (JOINA)
     */
    public final static int joinAdmin = 0x20;

    /**
     * Ability to see when someone WHOIS's you
     */
    public final static int seeWhois = 0x40;

    /**
     * Receives server notices (anything which generates a log info or higher)
     */
    public final static int seeServerNotices = 0x80;

    /**
     * Ability to change anyone's user mode
     */
    public final static int userModeHack = 0x100;

    /**
     * Ability to see all channels in LIST
     */
    public final static int seeAllChannels = 0x200;

    /**
     * Ability to join any channel regardless of flags (JOINA)
     */
    public final static int joinAnyChannel = 0x400;

    /**
     * Ability to change another user's nickname
     * TODO not implemented
     */
    public final static int changeOtherNick = 0x800;

    /**
     * Ignores the flood limiter
     */
    public final static int noFloodLimit = 0x1000;

    /**
     * Can see users IPs
     */
    public final static int userIP = 0x2000;

    /**
     * Can see invisible (+i) users and all members in all channels
     */
    public final static int seeInvisible = 0x4000;


    /**
     * All permissions
     */
    public final static int all = 0xFFFFFFFF;

    private Permissions()
    {
    }
}
