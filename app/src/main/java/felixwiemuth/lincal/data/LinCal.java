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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author Felix Wiemuth
 */
public class LinCal {

    private final String title;
    private final String author;
    private final String description;
    private final String version;
    private final Calendar date;

    private final List<CEntry> entries; // simple list, ordering is constructed at creation, no adaptions to list allowed

    public static class Builder {

        private String title;
        private String author;
        private String description;
        private String version;
        private Calendar date;
        private final SortedSet<CEntry> entries = new TreeSet<>();

        private Builder() {
        }

        public Builder title(final String value) {
            this.title = value;
            return this;
        }

        public Builder author(final String value) {
            this.author = value;
            return this;
        }

        public Builder description(final String value) {
            this.description = value;
            return this;
        }

        public Builder version(final String value) {
            this.version = value;
            return this;
        }

        public Builder date(final Calendar date) {
            this.date = Calendar.getInstance();
            this.date.setTime(date.getTime());
            return this;
        }

        public Builder addCEntry(final CEntry entry) {
            entries.add(entry);
            return this;
        }

        /**
         * Entries will be sorted by date.
         * @return
         */
        public LinCal build() {
            return new LinCal(title, author, description, version, date, new ArrayList<>(entries));
        }

    }

    public static LinCal.Builder builder() {
        return new LinCal.Builder();
    }

    private LinCal(final String title, final String author, final String description, final String version, final Calendar date, final List<CEntry> entries) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.version = version;
        this.date = date;
        this.entries = entries;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public String getVersion() {
        return version;
    }

    public Calendar getDate() {
        return date;
    }



}
