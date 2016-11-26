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

import felixwiemuth.lincal.Main;

/**
 * The representation of a calendar. All fields are guaranteed to be non-null (ensured with an
 * exception while building an object if this is not the case).
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

        public enum Field {
            TITLE, AUTHOR, DESCR, VERSION, DATE;
        }

        /**
         * Indicates that one of the fields is not set to a non-null value.
         */
        public static class MissingFieldException extends Exception {
            private Field field;

            public MissingFieldException(Field field) {
                this.field = field;
            }

            public Field getField() {
                return field;
            }

            @Override
            public String getMessage() {
                return "Cannot build calendar: missing field: " + field;
            }
        }

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
         * Build an instance of {@link LinCal} described by this builder. All properties must have
         * been set to non-null values. Entries will be sorted by date.
         *
         * @return
         * @throws MissingFieldException if one of the fields is not set
         */
        public LinCal build() throws MissingFieldException {
            if (title == null) {
                throw new MissingFieldException(Field.TITLE);
            }
            if (author == null) {
                throw new MissingFieldException(Field.AUTHOR);
            }
            if (description == null) {
                throw new MissingFieldException(Field.DESCR);
            }
            if (version == null) {
                throw new MissingFieldException(Field.VERSION);
            }
            if (date == null) {
                throw new MissingFieldException(Field.DATE);
            }
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

    public String getDateStr() {
        return Main.dfDay.format(date.getTime());
    }

    public CEntry get(int location) {
        return entries.get(location);
    }

    public int size() {
        return entries.size();
    }
}
