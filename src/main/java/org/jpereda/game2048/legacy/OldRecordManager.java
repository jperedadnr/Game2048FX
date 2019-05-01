/*
 * Copyright (C) 2013-2019 2048FX
 * Jose Pereda, Bruno Borges & Jens Deters
 * All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jpereda.game2048.legacy;

import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.StorageService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author José Pereda
 */
public class OldRecordManager {

    public final String SESSION_PROPERTIES_FILENAME;
    private final File path;
    private final Properties props = new Properties();

    public OldRecordManager(int grid_size) {
        
        path = Services.get(StorageService.class)
                .flatMap(service -> service.getPrivateStorage())
                .orElse(new File(System.getProperty("java.io.tmpdir")));
        
        this.SESSION_PROPERTIES_FILENAME = "game2048_" + grid_size + "_record.properties";
    }

    public int restoreRecord() {
        Reader reader = null;
        try {
            File file = new File(path,SESSION_PROPERTIES_FILENAME);
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
