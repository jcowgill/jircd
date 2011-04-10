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
    public final static int Rehash = 0x1;

    /**
     * Ability to restart / stop the server remotely
     */
    public final static int RestartDie = 0x2;

    /**
     * Ability to send WALLOPS (all ops always receive them)
     */
    public final static int Wallops = 0x4;

    /**
     * Ability to send WALL (WALLOPS to all clients)
     */
    public final static int Wall = 0x8;

    /**
     * Ability to kill another user
     */
    public final static int Kill = 0x10;

    /**
     * Automatically become admin in all channels
     */
    public final static int AutoAdmin = 0x20;

    /**
     * Ability to see when someone WHOIS's you
     */
    public final static int SeeWhois = 0x40;

    /**
     * Ability to see actual hostnames (not masked versions)
     */
    public final static int SeeHost = 0x80;


    /**
     * Receives server notices (anything which generates a log info or higher)
     */
    public final static int SeeServerNotices = 0x100;

    /**
     * Ability to change anyone's user mode
     */
    public final static int UserModeHack = 0x200;

    /**
     * Ability to see all channels in LIST
     */
    public final static int SeeAllChannels = 0x400;

    /**
     * Ability to join any channel regardless of flags
     */
    public final static int JoinAnyChannel = 0x800;

    /**
     * Ability to change another user's nickname
     */
    public final static int ChangeOtherNick = 0x1000;

    /**
     * Ignores the flood limiter
     */
    public final static int NoFloodLimit = 0x2000;

    /**
     * Can see users IPs
     */
    public final static int UserIP = 0x4000;

    /**
     * Can see invisible (+i) users
     */
    public final static int SeeInvisible = 0x8000;


    /**
     * All permissions
     */
    public final static int AllPermissions = 0xFFFFFFFF;

    private Permissions()
    {
    }
}
