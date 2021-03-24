// Bach's Boot Script

System.out.println(
"""
  , _
 /|/_)  _,    _   |)
  |  \\ / |   /    |/\\
  |(_/ \\/|_/ \\__/ |  |/.boot

                Bach %s
        Java Runtime %s
    Operating System %s
   Working Directory %s
"""
.formatted(
  com.github.sormuras.bach.Bach.version(),
  Runtime.version(),
  System.getProperty("os.name"),
  Path.of("").toAbsolutePath()
))

/reset

import static bin.boot.*
import com.github.sormuras.bach.*

void api() { utils.api(); }
void dir() { files.dir(); }
void pwd() { System.out.println(Path.of("").toAbsolutePath()); }
