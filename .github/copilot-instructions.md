# Copilot instructions for fx2048

## Build and run commands

- Maven is the build system; CI runs `./mvnw --batch-mode --no-transfer-progress -DskipTests package javafx:jlink jpackage:jpackage` on macOS, Windows, and Linux with Java 25.
- Use JDK 25 for local work. `pom.xml` and `.github/workflows/maven.yml` target Java/JavaFX 25.
- Run the app: `./mvnw javafx:run`
- Compile and package: `./mvnw clean package`
- Run tests: `./mvnw test`
- Run one Maven test when tests are present: `./mvnw -Dtest=SomeTest#someMethod test`
- Create a custom runtime: `./mvnw javafx:jlink`
- Create the platform package: `./mvnw clean package javafx:jlink jpackage:jpackage`

## Architecture

- This is a modular JavaFX desktop implementation of 2048. The JPMS module is `fxgame`, and the exported package is `io.fxgame.game2048`.
- `AppLauncher` is the command-line entry point. It launches `Game2048`, which creates the `Stage`, installs `game.css`, sizes the window, applies touch/ARM display behavior, and saves the best score on application stop.
- `GamePane` is the root JavaFX pane. It owns the `GameManager`, handles resize scaling/centering, keyboard shortcuts, arrow-key movement, and swipe input.
- `GameManager` owns the game rules and animation flow. It keeps the board state as `Map<Location, Tile>` with `null` values for empty cells, moves/merges tiles, adds random tiles, checks win/game-over conditions, and delegates UI updates to `Board`.
- `Board` builds the visible UI: score/best/time header, grid cells, toolbar, tile layer, overlays, buttons, score animation, and session/record integration. Overlay state is driven through `GameState` JavaFX properties.
- `GridOperator` centralizes grid size validation, traversal ordering, and direction-dependent sorting. `Location` is the immutable grid coordinate record and also contains tile layout coordinate helpers.
- Persistence goes through `UserSettings.LOCAL`, which stores properties under `${user.home}/.fx2048`. `SessionManager` stores/restores current grid, score, and elapsed time in `game2048_<gridSize>.properties`; `RecordManager` stores best score in `game2048_<gridSize>_record.properties`.
- UI styling lives in `src/main/resources/io/fxgame/game2048/game.css`; the Clear Sans font is loaded from the same resource package.

## Codebase conventions

- Prefer Maven for automation changes because GitHub Actions uses Maven.
- Keep the module name `fxgame` and main class `io.fxgame.game2048.AppLauncher` aligned across `module-info.java` and `pom.xml`.
- Preserve the JavaFX property/listener flow: toolbar and overlay button actions set `GameState` properties in `Board`; `GameManager` listens for confirmed reset/save/restore events and mutates the game grid.
- Do not bypass `GameManager.move(Direction)` for gameplay changes. It gates moves while overlays are shown and while tile animations are in progress.
- Empty cells are represented by keys present in `gameGrid` with `null` values. Movement logic depends on `Optional.ofNullable(gameGrid.get(location))` and on `GridOperator.sortGrid(direction)` before traversal.
- The default grid is 6x6. Valid custom grid sizes are enforced by `GridOperator.MIN_GRID_SIZE` and `MAX_GRID_SIZE`.
- CSS classes are part of tile behavior: `Tile` adds `game-label` and `game-tile-<value>` classes, and `merge` swaps the tile value class. Add matching `.game-tile-<value>` CSS when introducing new displayed tile values.
- Toolbar button IDs (`mSave`, `mRestore`, `mPause`, `mReplay`, `mInfo`, `mQuit`) are coupled to SVG shape rules in `game.css`.
- `GamePane` defines user controls: arrow keys/swipes move tiles; `S`, `R`, `P`, `Q`, and `F` trigger save, restore, pause, quit, and fullscreen.
