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

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import java.util.Calendar;

import felixwiemuth.lincal.Main;
import felixwiemuth.lincal.R;

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

        /**
         * Takes hour and minute from the given calendar and sets these for the date previously set
         * with {@link #date}. (which must not be null).
         *
         * @param time
         * @return
         */
        public Builder time(Calendar time) {
            this.date.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
            this.date.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
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

    public Calendar getDate() {
        return date;
    }

    public String getDateStr() {
        return Main.dfDay.format(date.getTime());
    }

    public String getDateTimeStr() {
        return Main.dfDayTime.format(date.getTime());
    }

    public String getDescription() {
        return description;
    }

    public String getLink() {
        return link;
    }

    public void open(Context context) {
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(getLink()));
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException ex) { // there is no activity to handle the link - show a dialog with the string
            showNonLinkEntryAsDialog(context);
        }
    }

    /**
     * Based on the user's settings, decides whether an entry is due.
     *
     * @param config the configuration to consider
     * @param now the time to compare to
     * @return
     */
    public boolean isDue(LinCalConfig config, Calendar now) {
        //TODO consider in addition user settings
        //TODO check whether all calendars are initialized correctly (same locale etc.)
        return (!getDate().after(now) && !config.getNotificationTime().after(now.getTime()));
    }

    private void showNonLinkEntryAsDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getDateStr())
                .setMessage(getLink())
                .setPositiveButton(R.string.dialog_dismiss, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
