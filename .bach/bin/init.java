import java.io.File;
import java.lang.module.ModuleFinder;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/** Bach's init program. */
class init {

  public static Path BIN = Path.of(".bach/bin");

  public static void main(String... args) throws Exception {
    var update = Files.isDirectory(BIN);
    if (update) {
      var module = ModuleFinder.of(BIN).find("com.github.sormuras.bach");
      if (module.isPresent()) Files.delete(Path.of(module.get().location().orElseThrow()));
    } else {
      Files.createDirectories(BIN);
    }

    System.out.println(""); // Load scripts and modules...
    var version = args.length == 0 ? "17-ea" : args[0];
    loadScript("bach").toFile().setExecutable(true);
    loadScript("bach.bat");
    loadScript("boot.java");
    loadScript("boot.jsh");
    loadScript("init.java");
    loadModule("com.github.sormuras.bach", version);

    var appendPathMessage =
        """

        Append `%s` to the PATH environment variable in order to call
        Bach's launch script without using that path prefix every time.
        """
            .formatted(BIN);
    var prefix = computePathPrefixToBachBinDirectory(() -> System.out.print(appendPathMessage));

    if (!update)
      System.out.printf( // On initialize, print possible next steps.
          """

          %sbach boot
            Launch a JShell session with Bach booted into it.
          %sbach --help
            Print Bach's help message.
          """,
          prefix, prefix);

    System.out.printf( // Updated/Initialized Bach ${VERSION} in ${DIRECTORY}.
        """

        %sd Bach %s in %s.
        """,
        update ? "Update" : "Initialize", version, Path.of("").toAbsolutePath());
  }

  static String computePathPrefixToBachBinDirectory(Runnable runnable) {
    var prefix = BIN.toString() + File.separator;
    var path = System.getenv("PATH");
    if (path == null) return prefix;
    var target = BIN.toString();
    var elements = List.of(path.split(File.pathSeparator));
    for (var element : elements) if (element.strip().equals(target)) return "";
    runnable.run();
    return prefix;
  }

  static Path loadScript(String name) throws Exception {
    var uri = "https://github.com/sormuras/bach/raw/main/.bach/bin/" + name;
    return copy(uri, name);
  }

  static void loadModule(String name, String version) throws Exception {
    var jar = name + "@" + version + ".jar";
    var uri = "https://github.com/sormuras/bach/releases/download/" + version + "/" + jar;
    copy(uri, jar);
  }

  static Path copy(String uri, String name) throws Exception {
    var file = BIN.resolve(name);
    try (var stream = new URL(uri).openStream()) {
      var size = Files.copy(stream, file, StandardCopyOption.REPLACE_EXISTING);
      System.out.printf("%,7d %s%n", size, file);
    }
    return file;
  }
}
