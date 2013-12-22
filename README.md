JIRC Server
===========
This is a basic [IRC](http://en.wikipedia.org/wiki/Internet_Relay_Chat) server implementation. It implements all the main things in the IRC standards except multiple servers (this won't connect to other servers)

Compiling
---------
This project uses [Maven](http://maven.apache.org/) as its build system. Most Java IDEs will allow you to import / build maven projects these days.

Alternatively you can compile using maven directly by opening the project folder in a terminal and running:

    mvn package

Running
-------
To run the server, just run the generated JAR file (with dependencies)

    java -jar target/jircd-<INSERT VERSION HERE>-jar-with-dependencies.jar

By default, the server will read its configuration from an ircd.conf file in the current directory or the directory containing the JAR file. This can be overridden by providing a path to the config file as an argument to the application.

