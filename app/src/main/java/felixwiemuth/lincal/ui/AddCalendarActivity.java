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
import felixwiemuth.lincal.Main;
import felixwiemuth.lincal.R;
import felixwiemuth.lincal.data.LinCal;
import felixwiemuth.lincal.parser.LinCalParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import linearfileparser.ParseException;

public class AddCalendarActivity extends AppCompatActivity {

    public static final String ARG_ADD_POSITION = "addPosition";

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
        final EditText file = (EditText) findViewById(R.id.ce_file);

        // Set file if activity was opened by file
        Uri uri = getIntent().getData();
        if (uri != null) {
            file.setText(uri.getPath());
        }

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinCalParser parser = new LinCalParser(getApplicationContext());
                String path = file.getText().toString();
                try {
                    LinCal calendar = parser.parse(new File(path));
                    //TODO instead of adding the calendar, add entry to the configuration file and return "true" as result - then MainActivity knows that the last entry is the new calendar
                    //TODO also lock configuration file and let main unlock it
                    int pos = Main.get().addCalendar(calendar);
                    AddCalendarActivity.this.finish();
                    Intent intent = new Intent(AddCalendarActivity.this, CalendarListActivity.class);
                    intent.putExtra(ARG_ADD_POSITION, pos);
                    startActivity(intent);
                } catch (IOException | ParseException ex) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddCalendarActivity.this);
                    builder.setMessage(ex.getMessage()).setPositiveButton(R.string.dialog_error_dismiss, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    if (ex instanceof FileNotFoundException) {
                        builder.setTitle(R.string.dialog_io_error);
                        builder.setMessage(String.format(getApplicationContext().getString(R.string.dialog_file_not_found_msg), path));
                    } else if (ex instanceof IOException) {
                        builder.setTitle(R.string.dialog_file_not_found);
                    } else {
                        builder.setTitle(R.string.dialog_parsing_error_title);
                    }
                    AlertDialog dialog = builder.show();
                }
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
