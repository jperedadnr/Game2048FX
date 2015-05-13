package org.jpereda.game2048;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gluon.charm.down.common.PlatformFactory;

/**
 *
 * @author José Pereda
 * @date 22-abr-2014 - 12:11:11
 */
public class RecordManager {

    public final String SESSION_PROPERTIES_FILENAME;
    private final File path = PlatformFactory.getPlatform().getPrivateStorage();
    private final Properties props = new Properties();

    public RecordManager(int grid_size) {
        this.SESSION_PROPERTIES_FILENAME = "game2048_" + grid_size + "_record.properties";
    }

    public void saveRecord(Integer score) {
        int oldRecord = restoreRecord();

        try {
            props.setProperty("record", Integer.toString(Math.max(oldRecord, score)));
            File file=new File(path,SESSION_PROPERTIES_FILENAME);
            props.store(new FileWriter(file), SESSION_PROPERTIES_FILENAME);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int restoreRecord() {
        Reader reader = null;
        try {
            File file=new File(path,SESSION_PROPERTIES_FILENAME);
            reader = new FileReader(file);
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
