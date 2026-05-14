# 2048FX

The game 2048 built using JavaFX 26.0.1 and Java 25. This is a Java port based on the
JavaScript version: https://github.com/gabrielecirulli/2048.

Check down below for a screenshot.

## Quick start

You will need [OpenJDK 25](https://jdk.java.net/) installed to build and run the project.
Maven is the build system used by CI; use the included Maven wrapper:

```bash
./mvnw javafx:run
```

## Releases

[![JavaFX Build](https://github.com/brunoborges/fx2048/actions/workflows/maven.yml/badge.svg)](https://github.com/brunoborges/fx2048/actions/workflows/maven.yml)

Visit the [fx2048 website](https://brunoborges.github.io/fx2048/) or download ready-to-run packages for Windows, macOS, and Linux from [GitHub Releases](https://github.com/brunoborges/fx2048/releases). The release workflow builds macOS arm64 DMG, Windows x64 MSI, and Linux amd64/arm64 DEB packages, each with the Java runtime needed to run the game.

## Screenshot

![](screenshot.png)

## Building and running

Run the app:

```bash
./mvnw javafx:run
```

The application is launched with an 8 MB initial heap, maximum 32 MB heap, G1GC, 10-second periodic concurrent collections, a 512 KB thread stack, a 12 MB reserved code cache, and C1-only compilation by default.

### Create a distribution to your operating system (Windows, Linux, or Mac OS)

You can create a ZIP file that bundles a custom runtime and the game, or create a native OS package. On macOS, the package is a DMG that opens in Finder for drag-and-drop installation.

To create a ZIP bundle, run:

```bash
./mvnw javafx:jlink
```

To create a native OS package, run:

```bash
./mvnw clean package javafx:jlink jpackage:jpackage
```

### Run in a browser with JPro

JPro support is available through the Maven `jpro` profile for:

- macOS arm64 (`jpro-macos-aarch64`)
- Linux amd64 (`jpro-linux-amd64`)
- Linux arm64 (`jpro-linux-aarch64`)

Windows is intentionally not supported in this JPro setup.

JPro runs the JavaFX application on the server and streams the UI to a browser.
This path does not use the desktop `AppLauncher`, `jlink`, or `jpackage` launchers.
Instead, the `jpro` profile points JPro directly at `io.fxgame.game2048.Game2048`,
the class that extends `javafx.application.Application`, and combines it with a
platform profile that selects the JPro-compatible JavaFX native artifacts.

Start JPro in development mode:

```bash
./mvnw -Pjpro,jpro-macos-aarch64 jpro:run
# or:
# ./mvnw -Pjpro,jpro-linux-amd64 jpro:run
# ./mvnw -Pjpro,jpro-linux-aarch64 jpro:run
```

Start JPro in background/server mode:

```bash
./mvnw -Pjpro,jpro-macos-aarch64 jpro:restart
# or:
# ./mvnw -Pjpro,jpro-linux-amd64 jpro:restart
# ./mvnw -Pjpro,jpro-linux-aarch64 jpro:restart
```

Then open:

`http://localhost:8080/index.html`

The `jpro:restart` goal writes the server PID to `RUNNING_PID`. Stop it with:

```bash
kill $(cat RUNNING_PID)
rm -f RUNNING_PID
```

### Create a GitHub release

Run the [Release workflow](https://github.com/brunoborges/fx2048/actions/workflows/release.yml) manually from `main` to publish a new version. If `pom.xml` is at `1.0.2-SNAPSHOT`, the workflow prepares and tags `v1.0.2`, publishes the platform packages, and bumps `main` to `1.0.3-SNAPSHOT`.

Direct `v*` tag pushes are also supported when the tag points at a commit whose Maven project version exactly matches the tag without the leading `v`. The same version is shown in the application title/About dialog.

macOS release packages are signed and notarized. Configure these GitHub Actions secrets before running a release:

| Secret | Purpose |
|---|---|
| `MACOS_CERTIFICATE_BASE64` | Base64-encoded Developer ID Application `.p12` certificate |
| `MACOS_CERTIFICATE_PASSWORD` | Password for the `.p12` certificate |
| `MACOS_SIGNING_KEY_USER_NAME` | Developer ID Application signing identity user/team name |
| `APPLE_ID` | Apple ID used for notarization |
| `APPLE_TEAM_ID` | Apple Developer Team ID |
| `APPLE_APP_SPECIFIC_PASSWORD` | App-specific password for notarization |

## Architecture

2048FX is a modular JavaFX desktop app. The JPMS module is `fxgame`, with `io.fxgame.game2048.AppLauncher` as the desktop launcher.

- `Game2048` creates the JavaFX `Stage`, installs the stylesheet, sizes the window, and saves the best score when the app stops.
- `GamePane` is the root pane. It creates the `GameManager`, handles resizing, keyboard shortcuts, arrow-key movement, fullscreen, and swipe input.
- `GameManager` owns the rules, board state, moves, merges, random tile creation, win/game-over checks, undo flow, and animation sequencing.
- `Board` builds the visible UI, including score, best score, timer, toolbar, overlays, settings, and tile rendering.
- `SessionManager`, `RecordManager`, and `UserSettings` persist sessions, records, and preferences under `${user.home}/.fx2048`.

The default board is 6x6. The settings panel supports custom grid sizes from 4x4 to 16x16, auto-save, and animation speed.

## Troubleshooting

| Problem | Fix |
|---|---|
| Build fails because of the Java version | Use JDK 25 and make sure `JAVA_HOME` points to it. |
| `./mvnw` is not executable on macOS or Linux | Run `chmod +x mvnw`, then retry the Maven command. |
| JavaFX native libraries do not match your OS or CPU | Use the Maven wrapper and avoid overriding `javafx.platform`; OS-specific Maven profiles select the right JavaFX classifier. |
| Native packaging fails | Ensure the platform packaging tools are available for your OS, then run `./mvnw clean package javafx:jlink jpackage:jpackage`. |
| The JPro browser profile does not run on Windows | Use macOS arm64, Linux amd64, or Linux arm64; Windows is intentionally not supported for this JPro setup. |

## Attribution

2048FX is maintained by [Bruno Borges](https://github.com/brunoborges). [Jose Pereda](https://github.com/JPeredaDnr) contributed styling and features to the project.

The game is a JavaFX port based on [Gabriele Cirulli's 2048](https://github.com/gabrielecirulli/2048). The bundled Clear Sans font was downloaded from Intel's Clear Sans project and may be used and redistributed under the Apache License 2.0.

## Feedback / Contributing / Comments
Submit an issue and share your thoughts.

### Running with Java 8

If you want to run with Java 8, you can download the tag [java-8](https://github.com/brunoborges/fx2048/releases/tag/java-8). New features in the master branch will not be back-ported.

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=brunoborges/fx2048&type=Date)](https://star-history.com/#brunoborges/fx2048&Date)

## License

The project is licensed under GPL 3. See [LICENSE](https://raw.githubusercontent.com/brunoborges/fx2048/main/LICENSE) file for the full license.
