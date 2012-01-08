JIRC Server
===========
This is a basic [IRC](http://en.wikipedia.org/wiki/Internet_Relay_Chat) server implementation.
It implements all the main things in the IRC standards except multiple servers (this won't connect to other servers)

Compiling
---------
If you have [Eclipse 3.7](http://www.eclipse.org/), Eclipse should compile the project without needing anything else.

Alternatively, install the following:

*	[Java Development Kit 6 or 7](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
*	[Maven 2](http://maven.apache.org/) - I havn't tested Maven 3 but it may work

Then open the project folder in a terminal and type:

		mvn install

This should produce jars in the target folder

