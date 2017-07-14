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

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import felixwiemuth.lincal.util.Time;

/**
 * Represents the configuration of a calendar which can be changed by the user and contains options
 * independent from the calendar itself.
 */
public class LinCalConfig {
    public static final String SEPARATOR = ";"; // separator for config values in a line of the configuration file
    public static final int FORMAT_VERSION = 1;

    /**
     * Indicates that the entry to be parsed is not well-formatted.
     */
    public static class FormatException extends Exception {

        public FormatException(String detailMessage) {
            super(detailMessage);
        }

        public FormatException(Throwable throwable) {
            super(throwable);
        }
    }

    private int id;
    private String calendarFile;
    private String calendarTitle;
    private LinCal.EntryDisplayMode entryDisplayModeDate;
    private LinCal.EntryDisplayMode entryDisplayModeDescription;
    private boolean notificationsEnabled;
    private boolean earliestNotificationTimeEnabled;
    private Time earliestNotificationTime;
    private boolean onScreenOn;
    private int pos;

    /**
     * Create a new calendar configuration. The position is initialized with 0.
     *
     * @param id
     * @param calendarFile
     * @param calendarTitle
     * @param entryDisplayModeDate
     * @param entryDisplayModeDescription
     * @param notificationsEnabled
     * @param earliestNotificationTimeEnabled
     * @param earliestNotificationTime
     * @param onScreenOn
     */
    public LinCalConfig(int id, String calendarFile, String calendarTitle, LinCal.EntryDisplayMode entryDisplayModeDate, LinCal.EntryDisplayMode entryDisplayModeDescription, boolean notificationsEnabled, boolean earliestNotificationTimeEnabled, Time earliestNotificationTime, boolean onScreenOn) {
        this.id = id;
        this.calendarFile = calendarFile;
        this.calendarTitle = calendarTitle;
        this.entryDisplayModeDate = entryDisplayModeDate;
        this.entryDisplayModeDescription = entryDisplayModeDescription;
        this.notificationsEnabled = notificationsEnabled;
        this.earliestNotificationTimeEnabled = earliestNotificationTimeEnabled;
        this.earliestNotificationTime = earliestNotificationTime;
        this.onScreenOn = onScreenOn;
    }

    public LinCalConfig() {
    }

    /**
     * Create an entry from a formatted line in a configuration file, which is at the specified
     * format version.
     *
     * @param line
     */
    public LinCalConfig(String line, int formatVersion) throws FormatException {
        String[] split = line.split(SEPARATOR);
        Iterator<String> values = Arrays.asList(split).iterator();
        switch (formatVersion) {
            case 1:
                initialize1(values);
                break;
            case 0:
                initialize0(values);
                update1();
                break;
        } //NOTE: can also do a second switch which will run through the cases without break, performing all updates starting from the old version
    }

    /**
     * Initialize this configuration instance from the given values array (format version 0).
     *
     * @param values
     * @throws FormatException
     */
    public void initialize0(Iterator<String> values) throws FormatException {
        try {
            id = Integer.parseInt(values.next());
            calendarFile = values.next();
            calendarTitle = values.next();
            entryDisplayModeDescription = LinCal.EntryDisplayMode.valueOf(values.next());
            notificationsEnabled = Boolean.valueOf(values.next());
            earliestNotificationTimeEnabled = Boolean.valueOf(values.next());
            earliestNotificationTime = new Time(0, 0);
            if (!earliestNotificationTime.set(values.next())) {
                throw new FormatException("Invalid time specification.");
            }
            onScreenOn = Boolean.parseBoolean(values.next());
            pos = Integer.parseInt(values.next());
        } catch (NoSuchElementException | IllegalArgumentException ex) {
            throw new FormatException(ex);
        }
    }

    /**
     * Initialize this configuration instance from the given values array (format version 1).
     *
     * @param values
     * @throws FormatException
     */
    public void initialize1(Iterator<String> values) throws FormatException {
        try {
            id = Integer.parseInt(values.next());
            calendarFile = values.next();
            calendarTitle = values.next();
            entryDisplayModeDate = LinCal.EntryDisplayMode.valueOf(values.next());
            entryDisplayModeDescription = LinCal.EntryDisplayMode.valueOf(values.next());
            notificationsEnabled = Boolean.valueOf(values.next());
            earliestNotificationTimeEnabled = Boolean.valueOf(values.next());
            earliestNotificationTime = new Time(0, 0);
            if (!earliestNotificationTime.set(values.next())) {
                throw new FormatException("Invalid time specification.");
            }
            onScreenOn = Boolean.parseBoolean(values.next());
            pos = Integer.parseInt(values.next());
        } catch (NoSuchElementException | IllegalArgumentException ex) {
            throw new FormatException(ex);
        }
    }

    /**
     * Update an instance loaded with format version 0 to format version 1 (e.g. initialize new
     * fields).
     */
    private void update1() {
        entryDisplayModeDate = entryDisplayModeDescription;
    }

    public int getId() {
        return id;
    }

    public String getCalendarFile() {
        return calendarFile;
    }

    public String getCalendarTitle() {
        return calendarTitle;
    }

    public LinCal.EntryDisplayMode getEntryDisplayModeDate() {
        return entryDisplayModeDate;
    }

    public LinCal.EntryDisplayMode getEntryDisplayModeDescription() {
        return entryDisplayModeDescription;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public boolean isEarliestNotificationTimeEnabled() {
        return earliestNotificationTimeEnabled;
    }

    public Time getEarliestNotificationTime() {
        return earliestNotificationTime;
    }

    public boolean isOnScreenOn() {
        return onScreenOn;
    }

    public int getPos() {
        return pos;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCalendarFile(String calendarFile) {
        this.calendarFile = calendarFile;
    }

    public void setCalendarTitle(String calendarTitle) {
        this.calendarTitle = calendarTitle;
    }

    public void setEntryDisplayModeDate(LinCal.EntryDisplayMode entryDisplayModeDate) {
        this.entryDisplayModeDate = entryDisplayModeDate;
    }

    public void setEntryDisplayModeDescription(LinCal.EntryDisplayMode entryDisplayModeDescription) {
        this.entryDisplayModeDescription = entryDisplayModeDescription;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public void setEarliestNotificationTimeEnabled(boolean earliestNotificationTimeEnabled) {
        this.earliestNotificationTimeEnabled = earliestNotificationTimeEnabled;
    }

    public void setEarliestNotificationTime(Time earliestNotificationTime) {
        this.earliestNotificationTime = earliestNotificationTime;
    }

    public void setOnScreenOn(boolean onScreenOn) {
        this.onScreenOn = onScreenOn;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    /**
     * Create a formatted line representing this entry.
     *
     * @return
     */
    @Override
    public String toString() {
        return id + SEPARATOR
                + calendarFile + SEPARATOR
                + calendarTitle + SEPARATOR
                + entryDisplayModeDate + SEPARATOR
                + entryDisplayModeDescription + SEPARATOR
                + notificationsEnabled + SEPARATOR
                + earliestNotificationTimeEnabled + SEPARATOR
                + earliestNotificationTime + SEPARATOR
                + onScreenOn + SEPARATOR
                + pos;
    }
}
