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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import felixwiemuth.lincal.Calendars;
import felixwiemuth.lincal.R;
import felixwiemuth.lincal.data.LinCalConfig;
import felixwiemuth.lincal.util.Time;

public class AddCalendarActivity extends AppCompatActivity {

    public static final Time DEFAULT_EARLIEST_NOTIFICATION_TIME = new Time(12, 0);

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
                final String file = fileEditText.getText().toString();
                EditText titleEditText = (EditText) findViewById(R.id.ce_title);
                //TODO set notification mode and notification time from settings by a widget
                Calendars.addCalendarChecked(file, titleEditText.getText().toString(), LinCalConfig.NotificationMode.GIVEN_TIME, DEFAULT_EARLIEST_NOTIFICATION_TIME, AddCalendarActivity.this);

                // Return to CalendarListActivity
                AddCalendarActivity.this.finish();
                Intent intent = new Intent(AddCalendarActivity.this, CalendarListActivity.class);
                intent.putExtra(CalendarListActivity.EXTRA_ARG_CONFIG_CHANGED, true);
                //intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT); //NOTE this would prevent activity from restarting
                startActivity(intent);
            }
        });
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
