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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Holds configurations of all added calendars.
 *
 * @author Felix Wiemuth
 */
public class LinCalConfig {

    public static final String SEPARATOR = ";"; // separator for config values in a line of the configuration file
    public static final SimpleDateFormat SDF = new SimpleDateFormat("H:mm");

    public static final String NOTIFICATION_MODE_GIVEN_TIME = "GIVEN_TIME";
    public static final String NOTIFICATION_MODE_SCREEN_ON = "SCREEN_ON";
    public static final String CONFIG_FILE_NAME = "config.txt";
    public static final Date DEFAULT_NOTIFICATION_TIME; //TODO set in AddCalendarActivity

    static {
        try {
            DEFAULT_NOTIFICATION_TIME = SDF.parse("12:00");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public enum NotificationMode {
        GIVEN_TIME,
        SCREEN_ON
    }

    private final Context context;
    private List<Entry> entries = new ArrayList<>();

    /**
     * Creates an instance and loads all entries from the configuration file.
     *
     * @param context
     */
    public LinCalConfig(Context context) {
        this.context = context;
        load();
    }

    /**
     * Represents the configuration for one calendar.
     */
    public static class Entry {
        /**
         * Indicated that the entry to be parsed is not well-formatted.
         */
        public static class FormatException extends Exception {
            public FormatException(Throwable throwable) {
                super(throwable);
            }
        }

        private String calendarFile;
        private String calendarTitle;
        private NotificationMode notificationMode;
        private Date notificationTime;
        private int pos;

        public Entry(String calendarFile, String calendarTitle, NotificationMode notificationMode, Date notificationTime, int pos) {
            this.calendarFile = calendarFile;
            this.calendarTitle = calendarTitle;
            this.notificationMode = notificationMode;
            this.notificationTime = notificationTime;
            this.pos = pos;
        }

        public Entry(String calendarFile, String calendarTitle, NotificationMode notificationMode, Date notificationTime) {
            this(calendarFile, calendarTitle, notificationMode, notificationTime, 0);
        }

        public Entry(String calendarFile, String calendarTitle, NotificationMode notificationMode) {
            this(calendarFile, calendarTitle, notificationMode, DEFAULT_NOTIFICATION_TIME, 0);
        }

        /**
         * Create an entry from a formatted line in a configuration file.
         *
         * @param line
         */
        public Entry(String line) throws FormatException {
            String[] split = line.split(SEPARATOR);
            Iterator<String> values = Arrays.asList(split).iterator();
            calendarFile = values.next();
            calendarTitle = values.next();
            notificationMode = NotificationMode.valueOf(values.next());
            try {
                notificationTime = SDF.parse(values.next());
            } catch (ParseException e) {
                throw new FormatException(e);
            }
            try {
                pos = Integer.parseInt(values.next());
            } catch (NumberFormatException e) {
                throw new FormatException(e);
            }
        }

        public String getCalendarFile() {
            return calendarFile;
        }

        public NotificationMode getNotificationMode() {
            return notificationMode;
        }

        public Date getNotificationTime() {
            return notificationTime;
        }

        public int getPos() {
            return pos;
        }

        public String getCalendarTitle() {
            return calendarTitle;
        }

        public void setCalendarFile(String calendarFile) {
            this.calendarFile = calendarFile;
        }

        public void setNotificationMode(NotificationMode notificationMode) {
            this.notificationMode = notificationMode;
        }

        public void setNotificationTime(Date notificationTime) {
            this.notificationTime = notificationTime;
        }

        public void setPos(int pos) {
            this.pos = pos;
        }

        public void setCalendarTitle(String calendarTitle) {
            this.calendarTitle = calendarTitle;
        }

        /**
         * Create a formatted line representing this entry.
         *
         * @return
         */
        @Override
        public String toString() {
            return calendarFile + SEPARATOR + calendarTitle + SEPARATOR + notificationMode + SEPARATOR + SDF.format(notificationTime) + SEPARATOR + pos;
        }
    }

    /**
     * Load all entries from the configuration file. If the file is not present, the list of entries
     * will be empty.
     *
     * @throws IOException
     * @throws Entry.FormatException
     */
    public void load() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(context.openFileInput(CONFIG_FILE_NAME)));
            String line = null;
            while ((line = in.readLine()) != null) {
                entries.add(new Entry(line));
            }
            in.close();
        } catch (FileNotFoundException ex) {
            // the file does not exist, thus no entries have to be loaded
        } catch (IOException | Entry.FormatException ex) {
            throw new RuntimeException(ex); // unrecoverable errors
        }
    }

    /**
     * Write the configuration represented by this instance to the configuration file. If the file
     * is not present, it is created.
     *
     * @throws FileNotFoundException
     */
    public void save() throws FileNotFoundException {
        FileOutputStream outputStream;
        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(context.openFileOutput(CONFIG_FILE_NAME, Context.MODE_PRIVATE)))); //TODO check how to allow readable (other modes deprecated)
        for (Entry entry : entries) {
            writer.println(entry);
        }
        writer.flush();
        writer.close();
        if (writer.checkError()) {
            throw new RuntimeException("Error while writing to configuration file.");
        }
    }

    public void add(Entry entry) {
        entries.add(entry);
    }

    public boolean containsCalendarFile(String path) {
        for (Entry entry : entries) {
            if (entry.getCalendarFile().equals(path)) {
                return true;
            }
        }
        return false;
    }

    public List<Entry> getEntries() {
        return entries;
    }
}
