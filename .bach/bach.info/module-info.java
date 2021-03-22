import com.github.sormuras.bach.ProjectInfo;
import com.github.sormuras.bach.ProjectInfo.Externals;
import com.github.sormuras.bach.ProjectInfo.Externals.Name;

@ProjectInfo(lookupExternals = @Externals(name = Name.JAVAFX, version = "16"))
module bach.info {
  requires com.github.sormuras.bach;
}
