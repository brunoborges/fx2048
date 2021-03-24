import com.github.sormuras.bach.ProjectInfo;
import com.github.sormuras.bach.ProjectInfo.*;

@ProjectInfo(
    lookupExternals = @Externals(name = Externals.Name.JAVAFX, version = "16"),
    launcher = @Launcher(command = "fx2048", module = "fxgame", mainClass = "io.fxgame.game2048.AppLauncher")
)
module bach.info {
  requires com.github.sormuras.bach;
}
