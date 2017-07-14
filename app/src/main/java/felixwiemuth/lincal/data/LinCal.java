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
import java.util.Collections;
import java.util.List;

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
    private final EntryDisplayMode entryDisplayModeDate;
    private final EntryDisplayMode entryDisplayModeDescription;
    private final boolean forceEntryDisplayModeDate;
    private final boolean forceEntryDisplayModeDescription;

    private final List<CEntry> entries; // simple list, ordering is constructed at creation, no adaptation of list allowed

    //NOTE order of constants must correspond to order of strings in spinner for UI
    public enum EntryDisplayMode {
        HIDE_ALL,
        HIDE_FUTURE,
        SHOW_ALL
    }

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
        private EntryDisplayMode entryDisplayModeDate = EntryDisplayMode.HIDE_FUTURE;
        private EntryDisplayMode entryDisplayModeDescription = EntryDisplayMode.HIDE_FUTURE;
        private boolean forceEntryDisplayModeDate = false;
        private boolean forceEntryDisplayModeDescription = false;
        private final List<CEntry> entries = new ArrayList<>();

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

        public Builder entryDisplayModeDate(final EntryDisplayMode value) {
            this.entryDisplayModeDate = value;
            return this;
        }

        public Builder entryDisplayModeDescription(final EntryDisplayMode value) {
            this.entryDisplayModeDescription = value;
            return this;
        }

        public void forceEntryDisplayModeDate(final boolean forceEntryDisplayModeDate) {
            this.forceEntryDisplayModeDate = forceEntryDisplayModeDate;
        }

        public void forceEntryDisplayModeDescription(final boolean forceEntryDisplayModeDescription) {
            this.forceEntryDisplayModeDescription = forceEntryDisplayModeDescription;
        }

        public Builder addCEntry(final CEntry entry) {
            entries.add(entry);
            return this;
        }

        /**
         * Build an instance of {@link LinCal} described by this builder. The properties from {@link
         * Field} must have been set to non-null values, other properties are given default values
         * if not set. Entries will be sorted by date, entries with the same date stay in the order
         * added.
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
            List<CEntry> sortedEntries = new ArrayList<>(entries); // make a copy to keep builder valid
            Collections.sort(sortedEntries);
            return new LinCal(title, author, description, version, date, entryDisplayModeDate, entryDisplayModeDescription, forceEntryDisplayModeDate, forceEntryDisplayModeDescription , sortedEntries);
        }

    }

    public static LinCal.Builder builder() {
        return new LinCal.Builder();
    }

    private LinCal(final String title, final String author, final String description, final String version, final Calendar date, final EntryDisplayMode entryDisplayModeDate, final EntryDisplayMode entryDisplayModeDescription, final boolean forceEntryDisplayModeDate, boolean forceEntryDisplayModeDescription, final List<CEntry> entries) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.version = version;
        this.date = date;
        this.entryDisplayModeDate = entryDisplayModeDate;
        this.entryDisplayModeDescription = entryDisplayModeDescription;
        this.forceEntryDisplayModeDate = forceEntryDisplayModeDate;
        this.forceEntryDisplayModeDescription = forceEntryDisplayModeDescription;
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

    /**
     * The date of entries of this calendar should be shown
     * according to the returned mode.
     *
     * @return
     */
    public EntryDisplayMode getEntryDisplayModeDate() {
        return entryDisplayModeDate;
    }

    /**
     * The description of entries of this calendar should be shown
     * according to the returned mode.
     *
     * @return
     */
    public EntryDisplayMode getEntryDisplayModeDescription() {
        return entryDisplayModeDescription;
    }

    /**
     * If {@code true}, the date of entries of this calendar should only be shown according to the
     * mode returned by {@link #getEntryDisplayModeDate()} and should not be changeable.
     *
     * @return
     */
    public boolean hasForceEntryDisplayModeDate() {
        return forceEntryDisplayModeDate;
    }

    /**
     * If {@code true}, the date of entries of this calendar should only be shown according to the
     * mode returned by {@link #getEntryDisplayModeDescription()} and should not be changeable.
     *
     * @return
     */
    public boolean hasForceEntryDisplayModeDescription() {
        return forceEntryDisplayModeDescription;
    }

    public CEntry get(int location) {
        return entries.get(location);
    }

    public int size() {
        return entries.size();
    }
}
