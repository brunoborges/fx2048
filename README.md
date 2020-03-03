# 2048FX

The game 2048 built using JavaFX and Java 11. This is a Java port based on the
Javascript version: https://github.com/gabrielecirulli/2048.

Check down below for a screenshot.

## Releases

[![Build Status](https://dev.azure.com/brunocborges/fx2048/_apis/build/status/brunoborges.fx2048?branchName=master)](https://dev.azure.com/brunocborges/fx2048/_build/latest?definitionId=1&branchName=master)

You may find binaries available for download, for Windows, Mac and Linux, with Java bundled in (using Java 11 jlink custom images). The ZIP files come with a binary that will start the game with the bundled optimized/trimmed JVM with only the needed modules, making the binaries extremely small comparably with the normal JRE download size. 

Check below the list of releases, and download the corresponding binary to your operating system.

- [game2048-20190903.1](https://github.com/brunoborges/fx2048/releases)
  - [Linux](https://github.com/brunoborges/fx2048/releases/download/game2048-20190903.1/game2048-linux.zip)
  - [Mac OS](https://github.com/brunoborges/fx2048/releases/download/game2048-20190903.1/game2048-mac.zip)
  - [Windows](https://github.com/brunoborges/fx2048/releases/download/game2048-20190903.1/game2048-win.zip)
  
## Screenshot

![](screenshot.png)

## Building and running

You will need [OpenJDK 11](http://jdk.java.net/11/) (or newer) installed to build and run the project:

```bash
./gradlew run
```

### Create a distribution to your operating system (Windows, Linux, or Mac OS)

You can create a ZIP file that will bundle a small JRE and the game, or you can create a native OS installer (e.g. MSI, DMG, DEB).

To create a ZIP bundle, run:

```bash
./gradlew dist
```

To create a native OS installer, follow these steps:

1. Download JDK 14 with `jpackage` for your specific OS: https://jdk.java.net/jpackage/
1. Configure the environment variable `BADASS_JLINK_JPACKAGE_HOME` to point to the extracted path of the JDK 14 with `jpackage`
1. Run `./gradlew dist jpackage`

## Running using Docker
You can build container image from source code using the Dockerfile and run the containerized game. 
You have to share the X11 socket with the container. For that you need to install [VcXsrv Windows X Server](https://sourceforge.net/projects/vcxsrv/) for windows or [Xquartz](https://www.xquartz.org) if you're using macOS. Make sure to allow connections from network during setup.
On macOS, you'll need to run `xhost +127.0.0.1` every time you re-open Xquartz.
The final step is to run the container: `docker run -it --rm -e DISPLAY=host.docker.internal:0.0 image_name`

## Feedback / Contributing / Comments
Submit an issue and share your thoughts.

### Running with Java 8

If you want to run with Java 8, you can download the tag [java-8](https://github.com/brunoborges/fx2048/releases/tag/java-8). New features in the master branch will not be back-ported.

## License

The project is licensed under GPL 3. See [LICENSE](https://raw.githubusercontent.com/brunoborges/fx2048/master/LICENSE) file for the full license.
