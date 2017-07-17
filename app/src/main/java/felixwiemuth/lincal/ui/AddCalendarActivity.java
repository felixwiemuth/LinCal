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

package felixwiemuth.lincal.ui;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import felixwiemuth.lincal.Calendars;
import felixwiemuth.lincal.R;
import felixwiemuth.lincal.data.LinCal;
import felixwiemuth.lincal.data.LinCalConfig;
import felixwiemuth.lincal.util.Time;
import felixwiemuth.lincal.util.Util;

public class AddCalendarActivity extends AppCompatActivity {

    public static final Time DEFAULT_EARLIEST_NOTIFICATION_TIME = new Time(12, 0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_calendar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Calendars.getInstance(this); // load calendars already here to check for updates before adding calendar (important for downgrade warning to be shown before clicking "add")

        Button chooseFileButton = (Button) findViewById(R.id.cb_file);
        Button addButton = (Button) findViewById(R.id.cb_add);
        final EditText fileEditText = (EditText) findViewById(R.id.ce_file);

        // Set file if activity was opened by file
        Uri uri = getIntent().getData();
        if (uri != null) {
            fileEditText.setText(uri.getPath());
        }

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String file = fileEditText.getText().toString();
                EditText titleEditText = (EditText) findViewById(R.id.ce_title);
                CheckBox notificationsCheckBox = (CheckBox) findViewById(R.id.cc_hideall);
                CheckBox hideAllCheckBox = (CheckBox) findViewById(R.id.cc_hideall);
                //TODO set notification mode and notification time from UI widgets
                LinCalConfig config = new LinCalConfig();
                config.setCalendarFile(file);
                config.setCalendarTitle(titleEditText.getText().toString());
                config.setNotificationsEnabled(notificationsCheckBox.isChecked());
                config.setEarliestNotificationTimeEnabled(true);
                config.setEarliestNotificationTime(DEFAULT_EARLIEST_NOTIFICATION_TIME);
                config.setOnScreenOn(false);
                if (hideAllCheckBox.isChecked()) {
                    config.setEntryDisplayModeDate(LinCal.EntryDisplayMode.HIDE_ALL);
                    config.setEntryDisplayModeDescription(LinCal.EntryDisplayMode.HIDE_ALL);
                }
                // adding the calendar sets initial entryDisplayModeDate and entryDisplayModeDescription from the calendar (or to defaults) if not set by the user (above)
                Calendars.addCalendarChecked(config, AddCalendarActivity.this, new Runnable() {
                    @Override
                    public void run() {
                        // Return to CalendarListActivity
                        AddCalendarActivity.this.finish();
                    }
                });
            }
        });

        SharedPreferences preferences = getPreferences(0); //TODO performance issue? If so, load beforehand to make use of caches etc.
        if (!preferences.contains("warningMessageShown")) {
            Util.showMessageDialog(R.string.dialog_warning_add_cal_title, R.string.dialog_warning_add_cal_msg, this);
            preferences.edit().putBoolean("warningMessageShown", true).apply();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
