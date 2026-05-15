# Maven Build Guide for fx2048

This project uses Maven as its build system.

## Prerequisites

- Java 25
- Maven 3.9.x (or use the included Maven wrapper)

## Building with Maven

### Using Maven Wrapper (Recommended)

The project includes a Maven wrapper (`mvnw`) that ensures everyone uses the same Maven version.

#### Compile the project
```bash
./mvnw clean compile
```

#### Run the application
```bash
./mvnw javafx:run
```

The Maven and packaged launchers default to `-Xmx48m -XX:+UseZGC`.

#### Package the application
```bash
./mvnw clean package
```

#### Run tests
```bash
./mvnw test
```

### Create a custom JRE with jlink
```bash
./mvnw javafx:jlink
```

This will create a custom Java runtime in `target/fx2048/` with only the modules needed for the application.

### Create a native package with jpackage
```bash
./mvnw clean package javafx:jlink jpackage:jpackage
```

This will:
1. Build the application
2. Create a custom JRE with jlink
3. Create a native package for your platform:
   - **macOS**: DMG disk image in `target/dist/`
   - **Windows**: MSI installer in `target/dist/`
   - **Linux**: DEB package in `target/dist/`

## Maven Project Structure

```
fx2048/
├── pom.xml                    # Maven project configuration
├── mvnw                       # Maven wrapper script (Unix)
├── mvnw.cmd                   # Maven wrapper script (Windows)
├── .mvn/                      # Maven wrapper configuration
├── src/
│   ├── main/
│   │   ├── java/             # Java source files
│   │   │   ├── module-info.java
│   │   │   └── io/github/brunoborges/fx2048/
│   │   └── resources/        # Application resources
│   │       └── io/github/brunoborges/fx2048/
│   └── test/                  # Test files
└── target/                    # Build output (generated)
```

## Maven Commands Cheat Sheet

| Command | Description |
|---------|-------------|
| `./mvnw clean` | Clean the build directory |
| `./mvnw compile` | Compile the source code |
| `./mvnw test` | Run unit tests |
| `./mvnw package` | Package the application as a JAR |
| `./mvnw javafx:run` | Run the JavaFX application |
| `./mvnw javafx:jlink` | Create a custom JRE with jlink |
| `./mvnw jpackage:jpackage` | Create a native package |
| `./mvnw clean install` | Clean, build, and install to local repo |

## Platform-Specific Packages

The project uses Maven profiles to automatically select the correct package type:

- **macOS**: Automatically creates `.dmg` disk image
- **Windows**: Automatically creates `.msi` installer  
- **Linux**: Automatically creates `.deb` package

## Dependencies

The project uses the following main dependencies:
- **JavaFX 25**: For the UI framework
  - javafx-base
  - javafx-graphics
  - javafx-controls

All dependencies are managed in `pom.xml` and automatically downloaded from Maven Central.

## Troubleshooting

### JavaFX not found
Make sure you're using Java 25. The JavaFX dependencies will be automatically downloaded by Maven.

### Module system issues
This project uses Java modules (JPMS). The module descriptor is in `src/main/java/module-info.java`.

### Maven wrapper not executable
On Unix-like systems, make the wrapper executable:
```bash
chmod +x mvnw
```
