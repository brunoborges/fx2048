package game2048;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 *
 * @author José Pereda
 * Created on 22-abr-2014 - 12:11:11
 */
public class SessionManager {
    
    public final String SESSION_PROPERTIES_FILENAME;
    private final Properties props = new Properties();
    private final int grid_size;
    
    public SessionManager(int grid_size){
        this.grid_size=grid_size;
        this.SESSION_PROPERTIES_FILENAME="game2048_"+grid_size+".properties";
    }

    public void saveSession(Map<Location, Tile> gameGrid, Integer score) {
        try {
            IntStream.range(0, grid_size).boxed().forEach(t_x->{
               IntStream.range(0, grid_size).boxed().forEach(t_y-> {
                   Tile t=gameGrid.get(new Location(t_x,t_y));
                   props.setProperty("Location_"+t_x.toString()+"_"+t_y.toString(),
                                       t!=null?t.getValue().toString():"0");
               });
            });
            props.setProperty("score", score.toString());
            props.store(new FileWriter(SESSION_PROPERTIES_FILENAME), SESSION_PROPERTIES_FILENAME);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public int restoreSession(Map<Location, Tile> gameGrid) {
        
        Reader reader = null;
        try {
            reader = new FileReader(SESSION_PROPERTIES_FILENAME);
            props.load(reader);
        } catch (FileNotFoundException ignored) {
            return -1;
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
        
        IntStream.range(0, grid_size).boxed().forEach(t_x->{
           IntStream.range(0, grid_size).boxed().forEach(t_y-> {
               String val=props.getProperty("Location_"+t_x.toString()+"_"+t_y.toString());
               if(!val.equals("0")){
                   Tile t=Tile.newTile(new Integer(val));
                   Location l=new Location(t_x,t_y);
                   t.setLocation(l);
                   gameGrid.put(l,t);
               }
           });
        });
        
        String score=props.getProperty("score");
        if(score!=null){
            return new Integer(score);
        }
        return 0;
    }
    
}
