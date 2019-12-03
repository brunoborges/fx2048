# 2048FX

The game 2048 built using JavaFX and Java 11. This is a Java port based on the
Javascript version: https://github.com/gabrielecirulli/2048.



## Releases

[![Build Status](https://dev.azure.com/brunocborges/fx2048/_apis/build/status/brunoborges.fx2048?branchName=master)](https://dev.azure.com/brunocborges/fx2048/_build/latest?definitionId=1&branchName=master)

You may find binaries available for download, for Windows, Mac and Linux, with Java bundled in (using Java 11 jlink custom images). The ZIP files come with a binary that will start the game with the bundled optimized/trimmed JVM with only the needed modules, making the binaries extremely small comparably with the normal JRE download size. 

Check below the list of releases, and download the corresponding binary to your operating system.

- [game2048-20190903.1](https://github.com/brunoborges/fx2048/releases)
  - [Linux](https://github.com/brunoborges/fx2048/releases/download/game2048-20190903.1/game2048-linux.zip)
  - [Mac OS](https://github.com/brunoborges/fx2048/releases/download/game2048-20190903.1/game2048-mac.zip)
  - [Windows](https://github.com/brunoborges/fx2048/releases/download/game2048-20190903.1/game2048-win.zip)

## Building and running

You will need [OpenJDK 11](http://jdk.java.net/11/) (or newer) installed to build and run the project:

```bash
./gradlew run
```

### Create a distribution to your operating system (Windows, Linux, or Mac OS)

Run

```bash
./gradle distro
```

## Feedback / Contributing / Comments
Submit an issue and share your thoughts.

### Running with Java 8

If you want to run with Java 8, you can download the tag [java-8](https://github.com/brunoborges/fx2048/releases/tag/java-8). New features in the master branch will not be back-ported.

## License

The project is licensed under GPL 3. See [LICENSE](https://raw.githubusercontent.com/brunoborges/fx2048/master/LICENSE) file for the full license.
