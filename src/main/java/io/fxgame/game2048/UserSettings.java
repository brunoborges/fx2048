package io.fxgame.game2048;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UserSettings
 * 
 * @author Bruno Borges
 */
public enum UserSettings {

    LOCAL;

    public final static int MARGIN = 36;
    private File userGameFolder;

    private UserSettings() {
        var userHome = System.getProperty("user.home");
        var gamePath = Path.of(userHome, ".fx2048");
        gamePath.toFile().mkdir();
        userGameFolder = gamePath.toFile();

        try {
            var isWindows = System.getProperty("os.arch").toUpperCase().contains("WINDOWS");
            if (isWindows) {
                Files.setAttribute(gamePath, "dos:hidden", true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void store(Properties data, String fileName) {
        try {
            data.store(new FileWriter(new File(userGameFolder, fileName)), fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void restore(Properties props, String fileName) {
        try (var reader = new FileReader(fileName)) {
            props.load(reader);
        } catch (FileNotFoundException e) {
            Logger.getLogger(UserSettings.class.getName()).log(Level.INFO, "Previous game record not found.");
        } catch (IOException ex) {
            Logger.getLogger(UserSettings.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

	public int getGridSize() {
        // @TODO save settings for grid size
		return GridOperator.DEFAULT_GRID_SIZE;
	}

}
