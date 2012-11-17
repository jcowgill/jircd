ircd.conf documentation
========================
This page details all the directives you can use in your ircd.conf file and a
few other things you can do with them.

Directive Format
------------------------
The ircd.conf file uses a system based on the one used by UnrealIRCD.

The file is made up of a list of directives. Each directive ends with a
semicolon and optionally has a parameter
and sub-directives.

For example:

    some_directive "Some Parameter";
    some_directive Some Parameter;

    some_directive
    {
        sub_directive_1;
        sub_directive_2;
    }

You can use C style comments or # comments.

    # This is a comment
    blob 34;        # This is a comment
    // This is a C style comment
    /* Multiline comment
       of Death */

Parameters can be strings or numbers. You may use quotes around strings but they
are optional unless the string contains a special character. Only one parameter
can be specified - spaces can be used to separate words in the
same parameter. Spaces directly after the directive name are ignored.

    blob Hello World;

This creates a directive called blob with the parameter "Hello World".

In this document sub-directives may be referred to using the . operator.
For example:

    blob
    {
        echo 5;
        victor 9;
        sierra 8;
    }

Here blob.echo = 5, blob.victor = 9 and blob.sierra = 8

Wildcard Comparisons
------------------------
The config file uses simple wildcard comparisons in some places. These work the
same as they do in IRC.

- All matches are case-insensitive.
- Star (*) characters match any number of characters (including zero).
- Question (?) characters match exactly one character.

For example:
`ab*c` matches `abcc`, `abblobc` and `abc` but not `ab`, ` ` or `abcd`

Server Information (name, description, admin)
------------------------
These directives provide information about the server for your users.

### name (Required)
This directive specifies the name of the server. It should usually be the domain
users will be connecting from.

### description (Required)
This directive specified the description for the server (used by /LINKS).

### admin (Optional)
This directive contains information about the server administrators printed by
the /ADMIN command. Each time an admin directive appears, it adds a new line of
text to print.

    admin "First line of text";
    admin "Second line of text";
    admin "Third line of text";
    ...

Message of the day (motdtext, motdfile)
------------------------
These commands specify the message of the day displayed to everyone who connects
to the server and by using the /MOTD command.

### motdfile (Optional)
This directive gives the name of a file the motd is read from.
If this directive is used, any motdtext directives are ignored.

### motdtext (Optional)
This directive gives the motd text in the config file. You can use multiline
parameters or use multiple motdtext directives (like the admin directive).

    motdtext
    "Some MOTD text
    This is some more text";

    motdtext "This is a 3rd line of text";

Ports (listen)
------------------------
This command tells the server to listen on the given ports. The server always
listens on all interfaces.

### listen (Required)
Each listen directive causes the server to listen on one port. This directive
may optionally contain an "ssl" directive. This causes this port to be SSL only.

    listen 6667;            # Listen on port 6667
    listen 6697 { ssl; }    # Listen on port 6697 using SSL

To use SSL ports, an ssl directive is required (see below).

SSL Options (ssl)
------------------------
This directive contains other directives which change the behaviour of secure
connections.

### ssl.keystore (Required to use SSL)
This directive specifies the path to a java keystore (JKS) file containing the
server's certificate and private key. You can create JKS files using the
"keytool" application which comes with java.

### ssl.password (Required to use SSL)
This directive specifies the password used to open the keystore file.

Connection Classes (class)
------------------------
Everyone who connects to the server is assigned a connection class. These are
used to limit the amount of server resources users consume. Each class must be
given a name (as the parameter) which is used by other directives.

    class <name of class>
    {
        <class directives>
    }

### class.readq (Required)
The amount of buffer space (bytes) allocated for incoming messages from a user.
This should usually be at least 2048.

### class.sendq (Required)
The amount of buffer space (bytes) allocated for outgoing messages to the user.
This should be at least 4096 but you probably want it higher (maybe 16k?).

### class.pingfreq (Required)
The number of seconds between pings from the server (90 second is reasonable).

### class.maxlinks (Required)
The maximum number of users allowed to use this class. This can be used to limit
the number of users on the server.

Accept Lines (accept)
------------------------
Every user must connect using an accept line. Accept lines control which users
are assigned to which connection class. There must be at least one accept line
otherwise no-one will be able to connect! Accept lines are processed **in the
order they appear in the file** so the more specific accept lines should come
first.

### accept.host and accept.ip (Required)
One of (or both) accept.host and accept.ip must be in every accept line. This
controls which connections match this line. If both host and ip are specified,
only one of them has to match.

Both use wildcard comparisons. accept.ip is tested against the user's ip
address, whereas accept.host is tested against the users username and hostname
(separated with a @). This means for accept.host you must use *@<hostname> to
just match a hostname.

All IPv6 addresses use the full form with leading zeros stripped
(eg the loopback address `::1` becomes `0:0:0:0:0:0:0:1`)

    ip *;               # Matches anyone
    host *@death.com    # Matches anyone from death.com

### accept.class
The name of the connection class associated with this accept line.

### accept.maxclones (Optional)
Maximum number of users allowed from the same ip address.

Ban Lines (ban - Optional)
------------------------
Each line specifies one type of server wide ban. There are 3 ban types, one of
which must be specified in the parameter of the ban directive.

### nick bans
Nick bans prevent anyone from using any nickname matching a mask.

### user bans
User bans are tested against the **user and hostname pair** (separated by @) and
are checked when a user connects.

### ip bans
IP bans are checked against the user's ip address. Any ban reason given for
these types of bans is ignored. See "Accept Lines" (above) for information about
IPv6 addresses.

### ban.mask (Required)
The wildcard mask used for the ban (depends on ban type)

### ban.reason (Optional)
The ban reason displayed to the user (nick and user bans only)

Operator Lines (operator - Optional)
------------------------
These directives specify who can become an IRC operator on the server (the part
you've been waiting for). The name of the operator (used as the first argument
to the /OPER command) is specified in the parameter. Operator names with spaces
in cannot be used.

### operator.mask (Required)
The wildcard mask which operators using this line must match. This mask is
compared to the **full** irc name (<nick>!<user>@<host>).

### operator.password (Required)
The password for this operator in SHA-1 format.

### operator.class (Optional)
The new connection class for the operator. If no class is specified, it is not
changed.

### operator.superop (Optional)
If specified, makes this operator a super op (see Permissions)

Permissions (permissions)
------------------------
There are 2 levels of operators in the IRC server - normal operators (+o) and
super operators (+O). Both can be configured independently using the permissions
directive. If there is no permissions block for a type, they get no permissions.

The permissions block can have 2 sub-directives for each level of operators.

    permissions
    {
        op
        {
            <operator permissions here>
        }

        superop
        {
            <super operator permissions here>
        }
    }

Each permission can be preceded with a "not" directive. This prevents that level
of operator from having the permission. All not directives (regardless of order)
are processed after the other directives.

### all
Grants all permissions available.

### rehash
Ability to use the /REHASH command to reload the configuration file.

### restartDie
Ability to use the /RESTART and /DIE commands to close the server down.

### wallops
Ability to use the /WALLOPS command to send messages to all other IRC operators.

### wall
Ability to use the /WALL command to send messages to everyone.

### kill
Ability to use the /KILL command to kick people off the server.

### joinAdmin
Ability to use the /JOINA command which allows operators to gain channel ops
(+a) when joining channels.

### seeWhois
Operator receives a notice when someone uses /WHOIS on them.

### seeServerNotices
Operator receives a notice when a server event occurs (anything printed to the
console logs).

### userModeHack
Ability to use /MODE on other users. Operator modes (+o and +O) cannot be set.
Modes of other operators cannot be set unless you are a super op.

### seeAllChannels
Sees secret and private channels in /LIST

### joinAnyChannel
Ability to join any channel using the /JOINA command.

### noFloodLimit
Is not affected by the flood limiter.

### userIP
Ability to use the /USERIP command to view IP addresses.

### seeInvisible
Can see invisible (+i) members in /WHO and all channel members in /WHO and
/NAMES even if not in the channel.

Modules (module)
------------------------
Code for commands and servlets can be loaded into the server using the module
directive. The parameter for the directive must be the full name of a java class
which implements the Module interface. Any sub-directives of a module directive
are passed to the module.

### module.jar (Optional)
If this sub-directive is specified, the specified jar file is added to the
classpath before the class is loaded. If this is not used, you must ensure the
module is located in the java classpath

### Commands Module
The built-in commands module should be loaded so that users can use the server.
To do this, add this line to the end of the config file.

    module uk.org.cowgill.james.jircd.commands.Builtin;
