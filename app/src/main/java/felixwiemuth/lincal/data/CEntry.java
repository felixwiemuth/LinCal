/*
 * Copyright (C) 2015 Felix Wiemuth
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

import java.util.Calendar;

/**
 * Represents one calendar in the list of calendars.
 *
 * @author Felix Wiemuth
 */
public class CEntry implements Comparable<CEntry> {

    private final Calendar date;
    private final String description;
    private final String link;

    public static class Builder {

        private Calendar date;
        private String description;
        private String link;

        private Builder() {
        }

        public Builder date(final Calendar date) {
            this.date = Calendar.getInstance();
            this.date.setTime(date.getTime());
            return this;
        }

        public Builder description(final String value) {
            this.description = value;
            return this;
        }

        public Builder link(final String value) {
            this.link = value;
            return this;
        }

        public CEntry build() {
            return new CEntry(date, description, link);
        }
    }

    public static CEntry.Builder builder() {
        return new CEntry.Builder();
    }

    public CEntry(final Calendar date, final String description, final String link) {
        this.date = date;
        this.description = description;
        this.link = link;
    }

    //TODO maybe want entries with same date to be equal
    @Override
    public int compareTo(CEntry another) {
        return date.compareTo(another.date);
    }

}
