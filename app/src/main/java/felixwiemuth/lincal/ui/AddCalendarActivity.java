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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.FileNotFoundException;

import felixwiemuth.lincal.Calendars;
import felixwiemuth.lincal.R;
import felixwiemuth.lincal.Util;
import felixwiemuth.lincal.data.LinCal;
import felixwiemuth.lincal.data.LinCalConfig;

public class AddCalendarActivity extends AppCompatActivity {

    private LinCalConfig config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_calendar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.hide();

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
                final String path = fileEditText.getText().toString();
                if (config.containsCalendarFile(path)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddCalendarActivity.this);
                    builder.setMessage("The calendar has already been added. Do you want to add another, independent copy?").setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            addCalendar(path); //NOTE: as this starts another activity, the dialog is still displayed when switching
                        }
                    });
                    builder.show();
                } else {
                    addCalendar(path);
                }
            }
        });

        config = new LinCalConfig(getApplicationContext());
    }

    private void addCalendar(String file) {
        EditText titleEditText = (EditText) findViewById(R.id.ce_title);
        LinCal calendar = Calendars.loadCalendar(file, this);
        if (calendar == null) {
            return;
        }

        // Before adding a calendar, load it to check syntax and get information

        String calendarTitle = titleEditText.getText().toString();
        if (calendarTitle.equals("")) {
            calendarTitle = calendar.getTitle();
        }
        if (calendarTitle.contains(LinCalConfig.SEPARATOR)) {
            showErrorDialog(R.string.dialog_error_title, String.format(getString(R.string.dialog_symbol_not_allowed_message), LinCalConfig.SEPARATOR));
            return;
        }
        config.add(new LinCalConfig.Entry(file, calendarTitle, LinCalConfig.NotificationMode.GIVEN_TIME)); //TODO set notification mode from settings by a widget
        try {
            config.save();
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex); //TODO show error dialog?
        }
        //Main.get().addCalendar(calendar);
        AddCalendarActivity.this.finish();
        Intent intent = new Intent(AddCalendarActivity.this, CalendarListActivity.class);
        intent.putExtra(CalendarListActivity.EXTRA_ARG_CONFIG_CHANGED, true);
        //intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT); //NOTE this would prevent activity from restarting
        startActivity(intent);
    }

    private void showErrorDialog(int title, String message) {
        Util.showErrorDialog(title, message, this);
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
