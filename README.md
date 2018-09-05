fx2048
======

The game 2048 built using JavaFX and Java 8. This is a fork based on the
Javascript version: https://github.com/gabrielecirulli/2048

Building fx2048 (Gradle - Java 11 with OpenJFX)
====================

You will need [OpenJDK 11](http://jdk.java.net/11/) and [Gradle](https://gradle.org/) installed to build and run the project:

```bash
gradle build
gradle run
```

Building fx2048 (Ant - Java 1.8)
====================

You will need [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
and [ANT](http://ant.apache.org/) installed to build the project. Just
execute ant in the project root.

```bash
ant
```

Running fx2048 (Ant - Java 1.8)
===================

After you've built the project you can run this with a simple java command.

```bash
java -jar dist/Game2048.jar
```

License
===================

The project is licensed under GPL 3. See [LICENSE](https://raw.githubusercontent.com/brunoborges/fx2048/master/LICENSE)
file for the full license.
