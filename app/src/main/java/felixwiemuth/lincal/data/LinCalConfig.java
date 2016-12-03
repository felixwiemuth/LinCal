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

import felixwiemuth.lincal.util.Time;

/**
 * Represents the configuration of a calendar.
 */
public class LinCalConfig {
    public static final String SEPARATOR = ";"; // separator for config values in a line of the configuration file

    public enum NotificationMode {
        GIVEN_TIME,
        SCREEN_ON
    }

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

    private final int id;
    private String calendarFile;
    private String calendarTitle;
    private NotificationMode notificationMode;
    private Time earliestNotificationTime;
    private boolean active;
    private int pos;

    /**
     * Create a new entry. The position is always initialized with 0.
     *
     * @param id
     * @param calendarFile
     * @param calendarTitle
     * @param notificationMode
     * @param earliestNotificationTime
     */
    public LinCalConfig(int id, String calendarFile, String calendarTitle, NotificationMode notificationMode, Time earliestNotificationTime) {
        this.id = id;
        this.calendarFile = calendarFile;
        this.calendarTitle = calendarTitle;
        this.notificationMode = notificationMode;
        this.earliestNotificationTime = earliestNotificationTime;
        this.active = true;
        this.pos = 0;
    }

    /**
     * Create an entry from a formatted line in a configuration file.
     *
     * @param line
     */
    public LinCalConfig(String line) throws FormatException {
        String[] split = line.split(SEPARATOR);
        Iterator<String> values = Arrays.asList(split).iterator();
        try {
            id = Integer.parseInt(values.next());
        } catch (NumberFormatException ex) {
            throw new FormatException(ex);
        }
        calendarFile = values.next();
        calendarTitle = values.next();
        notificationMode = NotificationMode.valueOf(values.next());
        earliestNotificationTime = new Time(0, 0);
        if (!earliestNotificationTime.set(values.next())) {
            throw new FormatException("Invalid time specification.");
        }
        active = Boolean.parseBoolean(values.next());
        try {
            pos = Integer.parseInt(values.next());
        } catch (NumberFormatException ex) {
            throw new FormatException(ex);
        }
    }

    public int getId() {
        return id;
    }

    public String getCalendarFile() {
        return calendarFile;
    }

    public NotificationMode getNotificationMode() {
        return notificationMode;
    }

    public Time getEarliestNotificationTime() {
        return earliestNotificationTime;
    }

    public boolean isActive() {
        return active;
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

    public void setEarliestNotificationTime(Time earliestNotificationTime) {
        this.earliestNotificationTime = earliestNotificationTime;
    }

    public void setActive(boolean active) {
        this.active = active;
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
        return id + SEPARATOR
                + calendarFile + SEPARATOR
                + calendarTitle + SEPARATOR
                + notificationMode + SEPARATOR
                + earliestNotificationTime + SEPARATOR
                + active + SEPARATOR
                + pos;
    }
}
