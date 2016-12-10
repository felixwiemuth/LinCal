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

package felixwiemuth.lincal.util;

import java.util.Calendar;
import java.util.regex.Pattern;

/**
 * Represents a time consisting of hour and minute.
 *
 * @author Felix Wiemuth
 */
public class Time {
    public static final Pattern TIME_PATTERN = Pattern.compile(":");

    private int hour;
    private int minute;

    public Time(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public boolean before(Calendar calendar) {
        return (hour < calendar.get(Calendar.HOUR_OF_DAY) || hour == calendar.get(Calendar.HOUR_OF_DAY) && minute < calendar.get(Calendar.MINUTE));
    }

    public boolean after(Calendar calendar) {
        return (hour > calendar.get(Calendar.HOUR_OF_DAY) || hour == calendar.get(Calendar.HOUR_OF_DAY) && minute > calendar.get(Calendar.MINUTE));
    }

    /**
     * @param minute
     * @return true, if 0 <= minute <= 59
     */
    public boolean setMinute(int minute) {
        if (minute >= 0 && minute <= 59) {
            this.minute = minute;
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param hour
     * @return true, if 0 <= hour <= 23
     */
    public boolean setHour(int hour) {
        if (hour >= 0 && hour <= 23) {
            this.hour = hour;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Set the time
     *
     * @param timeSpec a specification in the format (h)h:mm, in 24h format
     * @return
     */
    public boolean set(String timeSpec) {
        String[] split = TIME_PATTERN.split(timeSpec);
        if (split.length != 2) {
            return false;
        }
        int hour;
        int minute;
        try {
            hour = Integer.parseInt(split[0]);
            minute = Integer.parseInt(split[1]);
        } catch (NumberFormatException ex) {
            return false;
        }
        this.hour = hour;
        this.minute = minute;
        return true;
    }

    public void setAtCalendar(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
    }

    @Override
    public String toString() {
        return String.format("%02d", hour) + TIME_PATTERN.pattern() + String.format("%02d", minute);
    }
}
