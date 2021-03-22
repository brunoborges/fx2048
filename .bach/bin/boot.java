package bin;

import com.github.sormuras.bach.Bach;
import com.github.sormuras.bach.Command;
import com.github.sormuras.bach.Options;
import com.github.sormuras.bach.ProjectInfo;
import com.github.sormuras.bach.project.Property;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.spi.ToolProvider;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class boot {

  public static Bach bach() {
    return bach.get();
  }

  public static void beep() {
    System.out.print("\007"); // 🔔
    System.out.flush();
  }

  public static void refresh() {
    utils.refresh(ProjectInfo.MODULE);
  }

  public static void scaffold() throws Exception {
    files.createBachInfoModuleDescriptor();
    files.createBachInfoBuilderClass();
    exports.idea();
  }

  public interface exports {

    static void idea() throws Exception {
      var idea = bach().folders().root(".idea");
      if (Files.exists(idea)) {
        out("IntelliJ's IDEA directory already exits: %s", idea);
        return;
      }

      var name = bach().project().name();
      Files.createDirectories(idea);
      ideaMisc(idea);
      ideaRootModule(idea, name);
      ideaModules(idea, List.of(name, "bach.info"));
      if (Files.exists(bach().folders().root(".bach/bach.info"))) ideaBachInfoModule(idea);
      ideaLibraries(idea);
    }

    private static void ideaMisc(Path idea) throws Exception {
      Files.writeString(
          idea.resolve(".gitignore"),
          """
          # Default ignored files
          /shelf/
          /workspace.xml
          """);

      Files.writeString(
          idea.resolve("misc.xml"),
          """
          <?xml version="1.0" encoding="UTF-8"?>
          <project version="4">
            <component name="ProjectRootManager" version="2" languageLevel="JDK_16" default="true" project-jdk-name="16" project-jdk-type="JavaSDK">
              <output url="file://$PROJECT_DIR$/.idea/out" />
            </component>
          </project>
          """);
    }

    private static void ideaModules(Path idea, List<String> files) throws Exception {
      var modules = new StringJoiner("\n");
      for (var file : files) {
        modules.add(
            """
            <module fileurl="file://$PROJECT_DIR$/.idea/{{FILE}}.iml" filepath="$PROJECT_DIR$/.idea/{{FILE}}.iml" />"""
                .replace("{{FILE}}", file)
                .strip());
      }

      Files.writeString(
          idea.resolve("modules.xml"),
          """
          <?xml version="1.0" encoding="UTF-8"?>
          <project version="4">
            <component name="ProjectModuleManager">
              <modules>
          {{MODULES}}
              </modules>
            </component>
          </project>
          """
              .replace("{{MODULES}}", modules.toString().indent(6)));
    }

    private static void ideaRootModule(Path idea, String name) throws Exception {
      Files.writeString(
          idea.resolve(name + ".iml"),
          """
          <?xml version="1.0" encoding="UTF-8"?>
          <module type="JAVA_MODULE" version="4">
            <component name="NewModuleRootManager" inherit-compiler-output="true">
              <exclude-output />
              <content url="file://$MODULE_DIR$">
                <excludeFolder url="file://$MODULE_DIR$/.bach/workspace" />
              </content>
              <orderEntry type="sourceFolder" forTests="false" />
              <orderEntry type="inheritedJdk" />
            </component>
          </module>
          """);
    }

    private static void ideaBachInfoModule(Path idea) throws Exception {
      Files.writeString(
          idea.resolve("bach.info.iml"),
          """
          <?xml version="1.0" encoding="UTF-8"?>
          <module type="JAVA_MODULE" version="4">
            <component name="NewModuleRootManager" inherit-compiler-output="true">
              <exclude-output />
              <content url="file://$MODULE_DIR$/.bach/bach.info">
                <sourceFolder url="file://$MODULE_DIR$/.bach/bach.info" isTestSource="false" />
              </content>
              <orderEntry type="sourceFolder" forTests="false" />
              <orderEntry type="library" name="bach-bin" level="project" />
              <orderEntry type="inheritedJdk" />
            </component>
          </module>
          """);
    }

    private static void ideaLibraries(Path idea) throws Exception {
      var libraries = Files.createDirectories(idea.resolve("libraries"));

      Files.writeString(
          libraries.resolve("bach_bin.xml"),
          """
          <component name="libraryTable">
            <library name="bach-bin">
              <CLASSES>
                <root url="file://$PROJECT_DIR$/.bach/bin" />
              </CLASSES>
              <JAVADOC />
              <SOURCES />
              <jarDirectory url="file://$PROJECT_DIR$/.bach/bin" recursive="false" />
            </library>
          </component>
          """);

      Files.writeString(
          libraries.resolve("bach_external_modules.xml"),
          """
          <component name="libraryTable">
            <library name="bach-external-modules">
              <CLASSES>
                <root url="file://$PROJECT_DIR$/.bach/external-modules" />
              </CLASSES>
              <JAVADOC />
              <SOURCES />
              <jarDirectory url="file://$PROJECT_DIR$/.bach/external-modules" recursive="false" />
            </library>
          </component>
          """);
    }
  }

  public interface files {

    static void createBachInfoModuleDescriptor() throws Exception {
      var file = bach().folders().root(".bach/bach.info/module-info.java");
      if (Files.exists(file)) {
        out("File already exists: %s", file);
        return;
      }
      out("Create build information module descriptor: %s", file);
      var text =
          """
          @com.github.sormuras.bach.ProjectInfo (
            name = "{{PROJECT-NAME}}",
            version = "{{PROJECT-VERSION}}"
          )
          module bach.info {
            requires com.github.sormuras.bach;
            provides com.github.sormuras.bach.Bach.Provider with bach.info.Builder;
          }
          """
              .replace("{{PROJECT-NAME}}", bach().project().name())
              .replace("{{PROJECT-VERSION}}", bach().project().version());
      Files.createDirectories(file.getParent());
      Files.writeString(file, text);
    }

    static void createBachInfoBuilderClass() throws Exception {
      var file = bach().folders().root(".bach/bach.info/bach/info/Builder.java");
      out("Create builder class: %s", file);
      var text =
          """
          package bach.info;

          import com.github.sormuras.bach.*;

          public class Builder extends Bach {
            public static void main(String... args) {
              Bach.main(args);
            }

            public static Provider<Builder> provider() {
              return Builder::new;
            }

            private Builder(Options options) {
              super(options);
            }
          }
          """;
      Files.createDirectories(file.getParent());
      Files.writeString(file, text);
    }

    static void dir() {
      dir("");
    }

    static void dir(String folder) {
      dir(folder, "*");
    }

    static void dir(String folder, String glob) {
      var win = System.getProperty("os.name", "?").toLowerCase(Locale.ROOT).contains("win");
      var directory = Path.of(folder).toAbsolutePath().normalize();
      var paths = new ArrayList<Path>();
      try (var stream = Files.newDirectoryStream(directory, glob)) {
        for (var path : stream) {
          if (win && Files.isHidden(path)) continue;
          paths.add(path);
        }
      } catch (Exception exception) {
        out(exception);
      }
      paths.sort(
          (Path p1, Path p2) -> {
            var one = Files.isDirectory(p1);
            var two = Files.isDirectory(p2);
            if (one && !two) return -1; // directory before file
            if (!one && two) return 1; // file after directory
            return p1.compareTo(p2); // order lexicographically
          });
      long files = 0;
      long bytes = 0;
      for (var path : paths) {
        var name = path.getFileName().toString();
        if (Files.isDirectory(path)) out("%-15s %s", "[+]", name);
        else
          try {
            files++;
            var size = Files.size(path);
            bytes += size;
            out("%,15d %s", size, name);
          } catch (Exception exception) {
            out(exception);
            return;
          }
      }
      var all = paths.size();
      if (all == 0) {
        out("Directory %s is empty", directory);
        return;
      }
      out("");
      out("%15d path%s in directory %s", all, all == 1 ? "" : "s", directory);
      out("%,15d bytes in %d file%s", bytes, files, files == 1 ? "" : "s");
    }

    static void tree() {
      tree("");
    }

    static void tree(String folder) {
      tree(folder, name -> name.contains("module-info"));
    }

    static void tree(String folder, Predicate<String> fileNameFilter) {
      var directory = Path.of(folder).toAbsolutePath();
      out("%s", folder.isEmpty() ? directory : folder);
      var files = tree(directory, "  ", fileNameFilter);
      out("");
      out("%d file%s in tree of %s", files, files == 1 ? "" : "s", directory);
    }

    private static int tree(Path directory, String indent, Predicate<String> filter) {
      var win = System.getProperty("os.name", "?").toLowerCase(Locale.ROOT).contains("win");
      var files = 0;
      try (var stream = Files.newDirectoryStream(directory, "*")) {
        for (var path : stream) {
          if (win && Files.isHidden(path)) continue;
          var name = path.getFileName().toString();
          if (Files.isDirectory(path)) {
            out(indent + name + "/");
            if (name.equals(".git")) continue;
            files += tree(path, indent + "  ", filter);
            continue;
          }
          files++;
          if (filter.test(name)) out(indent + name);
        }
      } catch (Exception exception) {
        out(exception);
      }
      return files;
    }
  }

  public interface modules {

    /**
     * Prints a module description of the given module.
     *
     * @param module the name of the module to describe
     */
    static void describe(String module) {
      ModuleFinder.compose(
              ModuleFinder.of(bach().folders().workspace("modules")),
              ModuleFinder.of(bach().folders().externalModules()),
              ModuleFinder.ofSystem())
          .find(module)
          .ifPresentOrElse(
              reference -> out.accept(describe(reference)),
              () -> out.accept("No such module found: " + module));
    }

    /**
     * Print a sorted list of all modules locatable by the given module finder.
     *
     * @param finder the module finder to query for modules
     */
    static void describe(ModuleFinder finder) {
      var all = finder.findAll();
      all.stream()
          .map(ModuleReference::descriptor)
          .map(ModuleDescriptor::toNameAndVersion)
          .sorted()
          .forEach(out);
      out("%n-> %d module%s", all.size(), all.size() == 1 ? "" : "s");
    }

    // https://github.com/openjdk/jdk/blob/80380d51d279852f4a24ebbd384921106611bc0c/src/java.base/share/classes/sun/launcher/LauncherHelper.java#L1105
    static String describe(ModuleReference mref) {
      var md = mref.descriptor();
      var writer = new StringWriter();
      var print = new PrintWriter(writer);

      // one-line summary
      print.print(md.toNameAndVersion());
      mref.location().filter(uri -> !isJrt(uri)).ifPresent(uri -> print.format(" %s", uri));
      if (md.isOpen()) print.print(" open");
      if (md.isAutomatic()) print.print(" automatic");
      print.println();

      // unqualified exports (sorted by package)
      md.exports().stream()
          .filter(e -> !e.isQualified())
          .sorted(Comparator.comparing(ModuleDescriptor.Exports::source))
          .forEach(e -> print.format("exports %s%n", toString(e.source(), e.modifiers())));

      // dependences (sorted by name)
      md.requires().stream()
          .sorted(Comparator.comparing(ModuleDescriptor.Requires::name))
          .forEach(r -> print.format("requires %s%n", toString(r.name(), r.modifiers())));

      // service use and provides (sorted by name)
      md.uses().stream().sorted().forEach(s -> print.format("uses %s%n", s));
      md.provides().stream()
          .sorted(Comparator.comparing(ModuleDescriptor.Provides::service))
          .forEach(
              ps -> {
                var names = String.join("\n", new TreeSet<>(ps.providers()));
                print.format("provides %s with%n%s", ps.service(), names.indent(2));
              });

      // qualified exports (sorted by package)
      md.exports().stream()
          .filter(ModuleDescriptor.Exports::isQualified)
          .sorted(Comparator.comparing(ModuleDescriptor.Exports::source))
          .forEach(
              e -> {
                var who = String.join("\n", new TreeSet<>(e.targets()));
                print.format("qualified exports %s to%n%s", e.source(), who.indent(2));
              });

      // open packages (sorted by package)
      md.opens().stream()
          .sorted(Comparator.comparing(ModuleDescriptor.Opens::source))
          .forEach(
              opens -> {
                if (opens.isQualified()) print.print("qualified ");
                print.format("opens %s", toString(opens.source(), opens.modifiers()));
                if (opens.isQualified()) {
                  var who = String.join("\n", new TreeSet<>(opens.targets()));
                  print.format(" to%n%s", who.indent(2));
                } else print.println();
              });

      // non-exported/non-open packages (sorted by name)
      var concealed = new TreeSet<>(md.packages());
      md.exports().stream().map(ModuleDescriptor.Exports::source).forEach(concealed::remove);
      md.opens().stream().map(ModuleDescriptor.Opens::source).forEach(concealed::remove);
      concealed.forEach(p -> print.format("contains %s%n", p));

      return writer.toString().stripTrailing();
    }

    private static <T> String toString(String name, Set<T> modifiers) {
      var strings = modifiers.stream().map(e -> e.toString().toLowerCase());
      return Stream.concat(Stream.of(name), strings).collect(Collectors.joining(" "));
    }

    private static boolean isJrt(URI uri) {
      return (uri != null && uri.getScheme().equalsIgnoreCase("jrt"));
    }

    private static void findRequiresDirectivesMatching(ModuleFinder finder, String regex) {
      var descriptors =
          finder.findAll().stream()
              .map(ModuleReference::descriptor)
              .sorted(Comparator.comparing(ModuleDescriptor::name))
              .toList();
      for (var descriptor : descriptors) {
        var matched =
            descriptor.requires().stream()
                .filter(requires -> requires.name().matches(regex))
                .toList();
        if (matched.isEmpty()) continue;
        out.accept(descriptor.toNameAndVersion());
        matched.forEach(requires -> out.accept("  requires " + requires));
      }
    }

    interface external {

      static void delete(String module) throws Exception {
        var jar = bach().computeExternalModuleFile(module);
        out("Delete %s", jar);
        Files.deleteIfExists(jar);
      }

      static void purge() throws Exception {
        var externals = bach().folders().externalModules();
        if (!Files.isDirectory(externals)) return;
        try (var jars = Files.newDirectoryStream(externals, "*.jar")) {
          for (var jar : jars)
            try {
              out("Delete %s", jar);
              Files.deleteIfExists(jar);
            } catch (Exception exception) {
              out("Delete failed: %s", jar);
            }
        }
      }

      /** Prints a list of all external modules. */
      static void list() {
        describe(ModuleFinder.of(bach().folders().externalModules()));
      }

      static void load(String module) {
        bach().loadExternalModules(module);
        var set = bach().computeMissingExternalModules();
        if (set.isEmpty()) return;
        out("");
        missing.list(set);
      }

      static void findRequires(String regex) {
        var finder = ModuleFinder.of(bach().folders().externalModules());
        findRequiresDirectivesMatching(finder, regex);
      }

      interface missing {

        static void list() {
          list(bach().computeMissingExternalModules());
        }

        private static void list(Set<String> modules) {
          var size = modules.size();
          modules.stream().sorted().forEach(out);
          out("%n-> %d module%s missing", size, size == 1 ? " is" : "s are");
        }

        static void resolve() {
          bach().loadMissingExternalModules();
        }
      }

      interface prepared {
        static void loadComGithubSormurasModules() {
          loadComGithubSormurasModules("0-ea");
        }

        static void loadComGithubSormurasModules(String version) {
          var module = "com.github.sormuras.modules";
          var jar = module + "@" + version + ".jar";
          var uri = "https://github.com/sormuras/modules/releases/download/" + version + "/" + jar;
          bach().browser().load(uri, bach().computeExternalModuleFile(module));
        }
      }
    }

    interface system {

      /** Prints a list of all system modules. */
      static void list() {
        describe(ModuleFinder.ofSystem());
      }

      static void findRequires(String regex) {
        findRequiresDirectivesMatching(ModuleFinder.ofSystem(), regex);
      }
    }
  }

  public interface tools {

    static String describe(ToolProvider provider) {
      var name = provider.name();
      var module = provider.getClass().getModule();
      var by =
          Optional.ofNullable(module.getDescriptor())
              .map(ModuleDescriptor::toNameAndVersion)
              .orElse(module.toString());
      var info =
          switch (name) {
            case "bach" -> "Build modular Java projects";
            case "google-java-format" -> "Reformat Java sources to comply with Google Java Style";
            case "jar" -> "Create an archive for classes and resources, and update or restore them";
            case "javac" -> "Read Java compilation units (*.java) and compile them into classes";
            case "javadoc" -> "Generate HTML pages of API documentation from Java source files";
            case "javap" -> "Disassemble one or more class files";
            case "jdeps" -> "Launch the Java class dependency analyzer";
            case "jlink" -> "Assemble and optimize a set of modules into a custom runtime image";
            case "jmod" -> "Create JMOD files and list the content of existing JMOD files";
            case "jpackage" -> "Package a self-contained Java application";
            case "junit" -> "Launch the JUnit Platform";
            default -> provider.toString();
          };
      return "%s (provided by module %s)\n%s".formatted(name, by, info.indent(2)).trim();
    }

    static void list() {
      var providers = bach().computeToolProviders().toList();
      var size = providers.size();
      providers.stream()
          .map(tools::describe)
          .sorted()
          .map(description -> "\n" + description)
          .forEach(out);
      out("%n-> %d tool%s", size, size == 1 ? "" : "s");
    }

    static void runs() {
      var list = bach().logbook().runs();
      var size = list.size();
      list.forEach(out);
      out("%n-> %d run%s", size, size == 1 ? "" : "s");
    }

    static void run(String tool, Object... args) {
      var command = Command.of(tool).addAll(args);
      var recording = bach().run(command);
      if (!recording.errors().isEmpty()) out.accept(recording.errors());
      if (!recording.output().isEmpty()) out.accept(recording.output());
      if (recording.isError())
        out.accept("Tool " + tool + " returned exit code " + recording.code());
    }
  }

  public interface utils {
    private static void describeClass(Class<?> type) {
      Stream.of(type.getDeclaredMethods())
          .filter(utils::describeOnlyInterestingMethods)
          .sorted(Comparator.comparing(Method::getName).thenComparing(Method::getParameterCount))
          .map(utils::describeMethod)
          .forEach(out);
      list(type);
    }

    private static boolean describeOnlyInterestingClasses(Class<?> type) {
      if (type.isRecord()) return false;
      if (type.equals(utils.class)) return false;
      var modifiers = type.getModifiers();
      return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers);
    }

    private static boolean describeOnlyInterestingMethods(Method method) {
      var modifiers = method.getModifiers();
      return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers);
    }

    private static String describeMethod(Method method) {
      var generic = method.toGenericString();
      var line = generic.replace('$', '.');
      var head = line.indexOf("bin.boot.");
      if (head > 0) line = line.substring(head + 9);
      var tail = line.indexOf(") throws");
      if (tail > 0) line = line.substring(0, tail + 1);
      if (!line.endsWith("()")) {
        line = line.replace("com.github.sormuras.bach.", "");
        line = line.replace("java.util.function.", "");
        line = line.replace("java.util.spi.", "");
        line = line.replace("java.util.", "");
        line = line.replace("java.lang.module.", "");
        line = line.replace("java.lang.", "");
      }
      if (line.isEmpty()) throw new RuntimeException("Empty description line for: " + generic);
      return line;
    }

    static void api() {
      list(boot.class);
    }

    private static void list(Class<?> current) {
      Stream.of(current.getDeclaredClasses())
          .filter(utils::describeOnlyInterestingClasses)
          .sorted(Comparator.comparing(Class::getName))
          .peek(declared -> out(""))
          .forEach(utils::describeClass);
    }

    static void refresh(String module) {
      var load = Options.key(Property.BACH_INFO);
      var options = Options.of(load, module);
      try {
        var bach = Bach.of(options);
        set(bach);
      } catch (Exception exception) {
        out(
            """

            Refresh failed: %s

              Falling back to default Bach instance.
            """,
            exception.getMessage());
        set(new Bach(Options.of()));
      }
    }

    private static void set(Bach instance) {
      bach.set(instance);
    }
  }

  private static final Consumer<Object> out = System.out::println;
  private static final AtomicReference<Bach> bach = new AtomicReference<>();

  static {
    refresh();
  }

  private static void out(Exception exception) {
    out("""
        #
        # %s
        #
        """, exception);
  }

  private static void out(String format, Object... args) {
    out.accept(args == null || args.length == 0 ? format : String.format(format, args));
  }

  /** Hidden default constructor. */
  private boot() {}
}
