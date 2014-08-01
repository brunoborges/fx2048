package game2048;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author José Pereda
 * @date 22-abr-2014 - 12:11:11
 */
public class RecordManager {

    public final String SESSION_PROPERTIES_FILENAME;
    private final Properties props = new Properties();

    public RecordManager(int grid_size) {
        this.SESSION_PROPERTIES_FILENAME = "game2048_" + grid_size + "_record.properties";
    }

    public void saveRecord(Integer score) {
        int oldRecord = restoreRecord();

        try {
            props.setProperty("record", Integer.toString(Math.max(oldRecord, score)));
            props.store(new FileWriter(SESSION_PROPERTIES_FILENAME), SESSION_PROPERTIES_FILENAME);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int restoreRecord() {
        Reader reader = null;
        try {
            reader = new FileReader(SESSION_PROPERTIES_FILENAME);
            props.load(reader);
        } catch (FileNotFoundException ignored) {
            return 0;
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }

        String score = props.getProperty("record");
        if (score != null) {
            return new Integer(score);
        }
        return 0;
    }

}
