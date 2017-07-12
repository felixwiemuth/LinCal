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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import felixwiemuth.lincal.Calendars;
import felixwiemuth.lincal.R;

/**
 * Holds configurations of all added calendars. Ensures that if an entry is added to the file also
 * the next ID in the first line of the file is updated such that no entries with the same id can
 * exist in the file. Automatically updates the configuration file if necessary (and the (removal
 * of) the config-0 directory) on access.
 *
 * @author Felix Wiemuth
 */
public class LinCalConfigStore {

    public static final String NOTIFICATION_MODE_GIVEN_TIME = "GIVEN_TIME";
    public static final String NOTIFICATION_MODE_SCREEN_ON = "SCREEN_ON";
    public static final String CONFIG_FILE = "config.txt";
    public static final String CONFIG_FILE_OPENED = CONFIG_FILE + ".locked"; // name of the file while reading or writing from/to it
    public static final String PREFFILE_CONFIG_FILE_ENTRY_VERSION = "config";
    public static final String PREF_CONFIG_FILE_ENTRY_VERSION = "CONFIG_FILE_ENTRY_VERSION";

    private int nextId;
    private final List<LinCalConfig> entries = new ArrayList<>();

    /**
     * Creates an instance and loads all entries from the configuration file. If the file is not
     * present, the list of entries will be empty.
     *
     * @param context
     */
    public LinCalConfigStore(Context context) {
        boolean stop = update(context); // if updating, this will also load, but to verify it works, it is good to load again anyway with the next statement
        if (stop) {
            return; // Note: this leaves the application with entries being empty and an invalid nextId which is fine
        }
        load(context, LinCalConfig.FORMAT_VERSION, null);
    }

    /**
     * Load the configuration, replacing the configuration this instance represents.
     *
     * @param context
     * @param configFileEntryVersion
     * @param action                 action to be performed while the file is locked (can be null)
     */
    private void load(Context context, int configFileEntryVersion, Runnable action) {
        entries.clear();
        //TODO check correct handling of exceptions
        lockConfigFile(context); //NOTE: this assumes that file exists
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
                    entries.add(new LinCalConfig(line, configFileEntryVersion));
                }
                if (action != null) {
                    action.run();
                }
            } catch (IOException | LinCalConfig.FormatException ex) {
                throw new RuntimeException(ex); // unrecoverable errors
            } finally {
                try {
                    in.close();
                    unlockConfigFile(context); //TODO how to deal with exceptions, unlock file?
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
     *
     * @param context
     * @param action  action to be performed after saving but before unlocking the file (can be
     *                null)
     */
    private void save(Context context, Runnable action) {
        lockConfigFile(context);
        PrintWriter writer;
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
        if (action != null) {
            action.run();
        }
        unlockConfigFile(context); //TODO if this throws an exception: ignoring error check at writer
        if (writer.checkError()) {
            throw new RuntimeException("Error while writing to configuration file.");
        }
    }

    /**
     * Write the configuration represented by this instance to the configuration file. If the file
     * is not present, it is created.
     *
     * @param context
     */
    public void save(Context context) {
        save(context, null);
    }

    /**
     * Add an entry to this loaded configuration (call {@link #save(Context, Runnable)} to
     * persist).
     *
     * @param config the configuration for the new calendar (the id will be overwritten).
     * @return the id of the new calendar
     */
    public int add(LinCalConfig config) {
        int id = nextId++;
        config.setId(id);
        entries.add(config);
        return id;
    }


    /**
     * @param file
     * @return
     */
    public boolean containsCalendarFile(String file) {
        for (LinCalConfig linCalConfig : entries) {
            if (linCalConfig.getCalendarFile().equals(file)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return
     */
    public List<LinCalConfig> getEntries() {
        return entries;
    }

    private void lockConfigFile(Context context) {
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

    private void unlockConfigFile(Context context) {
        File dir = context.getFilesDir();
        if (!new File(dir, CONFIG_FILE_OPENED).renameTo(new File(dir, CONFIG_FILE))) {
            throw new RuntimeException("Error: Could not unlock config file.");
        }
    }

    /**
     * Update the configuration file to the current format if in an older format.
     *
     * @param context
     * @return true, if further initialization is handled by this method
     */
    private boolean update(final Context context) {
        final SharedPreferences pref = getVersionPref(context);
        if (!pref.contains(PREF_CONFIG_FILE_ENTRY_VERSION)) { // This is the first request of the configuration file OR the first after updating to version 1
            File dir = context.getFilesDir();
            File configDir = new File(dir, "config-0"); // the existence of this directory in version 0 indicated that an initial configuration file had been created
            if (configDir.exists()) { // This indicates that a configuration file of version 0 exists, a special update procedure to version 1 follows
                configDir.delete(); // delete the empty directory used with version 0
                load(context, 0, null); // load the configuration file with entry version 0
                save(context, new Runnable() { // and save it in the new version, updating the version variable
                    @Override
                    public void run() {
                        setVersion(pref, 1);
                    }
                });
                return false;
            } else {
                createInitialConfigurationFile(context, pref);
                setVersion(pref, LinCalConfig.FORMAT_VERSION); // it is okay if program fails before setting version here, it would just do the same procedure of creating an initial file on next request
                return false;
            }
        } else { // This is the general update case
            int fromVersion = pref.getInt(PREF_CONFIG_FILE_ENTRY_VERSION, -1);
            if (fromVersion == LinCalConfig.FORMAT_VERSION) {
                return false;
            } else if (fromVersion > LinCalConfig.FORMAT_VERSION) {
                if (!(context instanceof Activity)) { // check whether the context is an Activity and dialogs can be shown - if not throw an exception
                    throw new RuntimeException("Application downgrade with incompatible config file formats - start app for further options.");
                }
                final Activity activity = (Activity) context;
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(R.string.dialog_downgrade_title).setMessage(R.string.dialog_downgrade_msg).setNegativeButton(R.string.dialog_downgrade_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // At this point, the application should stop
                        System.exit(0); // this should be safe here as it is called right after starting the app and no files should be open //TODO find an alternative which really terminates the app such that
                    }
                }).setPositiveButton(R.string.dialog_downgrade_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createInitialConfigurationFile(context, pref); // reset configuration file
                        Calendars.invalidate(); // the initial configuration file just created has to be loaded (e.g. to set correct nextId)
                        setVersion(pref, LinCalConfig.FORMAT_VERSION); // set the current version
                    }
                }).setCancelable(false); // the app may not be used without selecting an option
                builder.show();
                return true; // leave application with an empty ConfigStore - should it try to load the configuration again, another dialog will be shown if the previous one has not been exited with
            } else { // have to update config file entries from a lower version to the current
                load(context, fromVersion, null);
                save(context, new Runnable() {
                    @Override
                    public void run() {
                        setVersion(pref, LinCalConfig.FORMAT_VERSION);
                    }
                });
                return false; // to make sure the new format works, the file is directly loaded again
            }
        }
    }

    private SharedPreferences getVersionPref(Context context) {
        return context.getSharedPreferences(PREFFILE_CONFIG_FILE_ENTRY_VERSION, 0);
    }

    private void setVersion(SharedPreferences pref, int version) {
        pref.edit().putInt(PREF_CONFIG_FILE_ENTRY_VERSION, version).apply();
    }

    /**
     * Creates the configuration file in an initialized state and overwrites it if already existing,
     * sets the version variable of the configuration to the current version.
     *
     * @param context
     * @param pref
     */
    private void createInitialConfigurationFile(Context context, SharedPreferences pref) {
        PrintWriter writer;
        try {
            //TODO check how to allow file to be public (other modes deprecated)
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(context.openFileOutput(CONFIG_FILE, Context.MODE_PRIVATE))));
        } catch (FileNotFoundException ex) { // this should not occur, as when locking the file succeeds, it should exist
            throw new RuntimeException(ex);
        }
        writer.println(0);
        if (writer.checkError()) {
            throw new RuntimeException("Error while creating initial configuration file.");
        }
    }
}
