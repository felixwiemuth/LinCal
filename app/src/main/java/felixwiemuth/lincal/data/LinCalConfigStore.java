/*
 * Copyright (C) 2016 Felix Wiemuth
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

package felixwiemuth.lincal.data;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import felixwiemuth.lincal.util.Time;

/**
 * Holds configurations of all added calendars. Ensures that if an entry is added to the file also
 * the next ID in the first line of the file is updated such that no entries with the same id can
 * exist in the file.
 *
 * @author Felix Wiemuth
 */
public class LinCalConfigStore {

    public static final String NOTIFICATION_MODE_GIVEN_TIME = "GIVEN_TIME";
    public static final String NOTIFICATION_MODE_SCREEN_ON = "SCREEN_ON";
    public static final String CONFIG_FILE = "config.txt";
    public static final String CONFIG_FILE_OPENED = CONFIG_FILE + ".locked"; // name of the file while reading or writing from/to it

    private final Context context;
    private int nextId;
    private final List<LinCalConfig> entries = new ArrayList<>();

    private LinCalConfigStore(Context context, int nextId) {
        this.context = context;
        this.nextId = nextId;
    }

    /**
     * Creates an instance and loads all entries from the configuration file. If the file is not
     * present, the list of entries will be empty.
     *
     * @param context
     */
    public LinCalConfigStore(Context context) {
        this.context = context;
        //TODO check correct handling of exceptions
        lockConfigFile(); //NOTE: this assumes that file exists
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(context.openFileInput(CONFIG_FILE_OPENED)));
            try {
                String line = in.readLine();
                try {
                    nextId = Integer.parseInt(line);
                } catch (NumberFormatException ex) {
                    throw new RuntimeException("File must start with an integer in the first line.", ex);
                }
                while ((line = in.readLine()) != null) {
                    entries.add(new LinCalConfig(line));
                }
            } catch (IOException | LinCalConfig.FormatException ex) {
                throw new RuntimeException(ex); // unrecoverable errors
            } finally {
                try {
                    in.close();
                    unlockConfigFile(); //TODO how to deal with exceptions, unlock file?
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        } catch (FileNotFoundException ex) {
            // the file does not exist, thus no entries have to be loaded
            nextId = 0;
        }
    }

    /**
     * Write the configuration represented by this instance to the configuration file. If the file
     * is not present, it is created.
     */
    public void save() {
        lockConfigFile();
        FileOutputStream outputStream;
        PrintWriter writer = null;
        try {
            //TODO check how to allow file to be public (other modes deprecated)
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(context.openFileOutput(CONFIG_FILE_OPENED, Context.MODE_PRIVATE))));
        } catch (FileNotFoundException ex) { // this should not occur, as when locking the file succeeds, it should exist
            throw new RuntimeException(ex);
        }
        writer.println(nextId);
        for (LinCalConfig linCalConfig : entries) {
            writer.println(linCalConfig);
        }
        writer.close();
        unlockConfigFile(); //TODO if this throws an exception: ignoring error check at writer
        if (writer.checkError()) {
            throw new RuntimeException("Error while writing to configuration file.");
        }
    }

    /**
     * Add an entry to this loaded configuration (call{@link #save()} to persist).
     *
     * @param calendarFile
     * @param calendarTitle
     * @param entryDisplayMode
     *@param notificationMode
     * @param earliestNotificationTime   @return the id of the new calendar
     */
    public int add(String calendarFile, String calendarTitle, LinCalConfig.EntryDisplayMode entryDisplayMode, LinCalConfig.NotificationMode notificationMode, Time earliestNotificationTime) {
        int id = nextId++;
        entries.add(new LinCalConfig(id, calendarFile, calendarTitle, entryDisplayMode, notificationMode, earliestNotificationTime));
        return id;
    }


    public boolean containsCalendarFile(String file) {
        for (LinCalConfig linCalConfig : entries) {
            if (linCalConfig.getCalendarFile().equals(file)) {
                return true;
            }
        }
        return false;
    }

    public List<LinCalConfig> getEntries() {
        return entries;
    }

    private void lockConfigFile() {
        File dir = context.getFilesDir();
        // in increasing time intervals of up to ~15s try to lock file (6 times)
        for (int t = 5; t <= 15625; t *= 5) {
            // renaming should fail if it was already renamed
            if (new File(dir, CONFIG_FILE).renameTo(new File(dir, CONFIG_FILE_OPENED))) {
                return;
            }
            try {
                Thread.sleep(t, 0);
            } catch (InterruptedException ex) {
                // just try again
            }
        }
        throw new RuntimeException("Error: Could not lock config file to read/write.");
    }

    private void unlockConfigFile() {
        File dir = context.getFilesDir();
        if (!new File(dir, CONFIG_FILE_OPENED).renameTo(new File(dir, CONFIG_FILE))) {
            throw new RuntimeException("Error: Could not unlock config file.");
        }
    }

    public static void createInitialConfigurationFile(Context context) {
        new LinCalConfigStore(context, 0).save();
    }
}
