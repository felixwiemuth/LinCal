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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

import felixwiemuth.lincal.Calendars;
import felixwiemuth.lincal.Main;
import felixwiemuth.lincal.NotificationService;
import felixwiemuth.lincal.R;
import felixwiemuth.lincal.data.CEntry;
import felixwiemuth.lincal.data.LinCal;
import felixwiemuth.lincal.data.LinCalConfig;
import felixwiemuth.lincal.util.ImplementationError;
import felixwiemuth.lincal.util.Time;

/**
 * A fragment representing a single Calendar screen with a list of its entries. This fragment is
 * either contained in a {@link CalendarListActivity} in two-pane mode (on tablets) or a {@link
 * CalendarViewActivity} on handsets.
 */
public class CalendarViewFragment extends Fragment {

    /**
     * The fragment argument representing the item ID that this fragment represents.
     */
    public static final String ARG_CALENDAR_POS = "felixwiemuth.lincal.CalendarListActivity.EXTRA_ARG_CALENDAR_POS";

    private LinCal calendar;
    private int calendarPos;
    private CheckBox notificationsEnabled;
    private Time earliestNotificationTime;
    private TextView textViewEarliestNotificationTime;
    private CheckBox earliestNotificationTimeEnabled;
    //private CheckBox onScreenOnEnabled; //TODO implement
    private Spinner entryDisplayModeDate;
    private Spinner entryDisplayModeDescription;
    private Button buttonRemoveCalendar;
    private RecyclerView entryList;

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
        private int calendarPos;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            int hour = getArguments().getInt("hour");
            int minute = getArguments().getInt("minute");
            calendarPos = getArguments().getInt("calendarPos");

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute, true);
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendars calendars = Calendars.getInstance(getContext());
            LinCalConfig config = calendars.getConfigByPos(calendarPos);
            Time time = new Time(hourOfDay, minute);
            config.setEarliestNotificationTime(time);
            TextView textViewEarliestNotificationTime = (TextView) getActivity().findViewById(R.id.setting_earliest_notification_time);
            textViewEarliestNotificationTime.setText(time.toString());
            calendars.save(getContext());
            // have to update the displayed notification times (times might have changed)
            RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.entry_list_recycler_view);
            recyclerView.getAdapter().notifyDataSetChanged();
            NotificationService.runWithCalendar(getContext(), config.getId()); //TODO reconsider when to call
        }
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon
     * screen orientation changes).
     */
    public CalendarViewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getArguments().containsKey(ARG_CALENDAR_POS)) {
            throw new RuntimeException("Missing argument: CalendarViewFragment.ARG_CALENDAR_POS");
        }
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        calendarPos = getArguments().getInt(ARG_CALENDAR_POS);
        Calendars calendars = Calendars.getInstance(getContext()); // loads calendar and makes sure all values in config are set (e.g. forceEntryDisplayMode values)
        calendar = calendars.getCalendarByPos(getContext(), calendarPos);
        // Set ActionBar title to calendar title (as set by user) to show which calendar is selected (in narrow as well as wide)
        activity.getSupportActionBar().setTitle(calendars.getConfigByPos(calendarPos).getCalendarTitle());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.calendar_view, container, false);
        TextView titleView = (TextView) rootView.findViewById(R.id.cal_title);
        TextView authorView = (TextView) rootView.findViewById(R.id.cal_author);

        notificationsEnabled = (CheckBox) rootView.findViewById(R.id.notifications_enabled);
        textViewEarliestNotificationTime = (TextView) rootView.findViewById(R.id.setting_earliest_notification_time);
        earliestNotificationTimeEnabled = (CheckBox) rootView.findViewById(R.id.setting_earliest_notification_time_enabled);
        entryDisplayModeDate = (Spinner) rootView.findViewById(R.id.setting_entry_display_mode_date);
        entryDisplayModeDescription = (Spinner) rootView.findViewById(R.id.setting_entry_display_mode_description);
        buttonRemoveCalendar = (Button) rootView.findViewById(R.id.button_remove_cal);

        if (calendar == null) {
            titleView.setText(R.string.cal_title_error_loading);
            notificationsEnabled.setEnabled(false);
            earliestNotificationTimeEnabled.setEnabled(false);
            entryDisplayModeDate.setEnabled(false);
            entryDisplayModeDescription.setEnabled(false);
        } else {
            titleView.setText(calendar.getTitle());
            authorView.setText(calendar.getAuthor());
            entryList = (RecyclerView) rootView.findViewById(R.id.entry_list_recycler_view);
            SimpleItemRecyclerViewAdapter adapter = new SimpleItemRecyclerViewAdapter();
            entryList.setAdapter(adapter);
            ((TextView) rootView.findViewById(R.id.cal_descr)).setText(calendar.getDescription());
            ((TextView) rootView.findViewById(R.id.cal_version)).setText(calendar.getVersion());
            ((TextView) rootView.findViewById(R.id.cal_date)).setText(calendar.getDateStr());
            if (calendar.hasForceEntryDisplayModeDate()) {
                entryDisplayModeDate.setEnabled(false);
            }
            if (calendar.hasForceEntryDisplayModeDescription()) {
                entryDisplayModeDescription.setEnabled(false);
            }
        }

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getContext(), R.array.setting_entry_display_mode_options, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        entryDisplayModeDate.setAdapter(spinnerAdapter);
        entryDisplayModeDescription.setAdapter(spinnerAdapter);

        loadSettings(); // loading settings before adding listeners prevents them from firing due to initialization (e.g. Spinner)

        if (calendar != null) {
            // Set listeners
            final View.OnClickListener saveSettingsListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveSettings();
                    NotificationService.runWithCalendar(getContext(), Calendars.getInstance(getContext()).getConfigByPos(calendarPos).getId()); //TODO reconsider when to call
                }
            };

            notificationsEnabled.setOnClickListener(saveSettingsListener);
            textViewEarliestNotificationTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadSettings(); // update earliestNotificationTime
                    DialogFragment dialogFragment = new TimePickerFragment();
                    Bundle arguments = new Bundle();
                    arguments.putInt("hour", earliestNotificationTime.getHour());
                    arguments.putInt("minute", earliestNotificationTime.getMinute());
                    arguments.putInt("calendarPos", calendarPos);
                    dialogFragment.setArguments(arguments);
                    dialogFragment.show(getFragmentManager(), "timePicker");
                }
            });
            earliestNotificationTimeEnabled.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveSettingsListener.onClick(v);
                    // have to update the displayed notification times (only show when enabled)
                    entryList.getAdapter().notifyDataSetChanged();
                }
            });
            //TODO implement
            //        onScreenOnEnabled = (CheckBox) rootView.findViewById(R.id.setting_show_notification_on_screen_on);
            //        onScreenOnEnabled.setOnClickListener(saveSettingsListener);
            entryDisplayModeDate.setOnItemSelectedListener(new AdapterViewOnItemSelectedListener(entryDisplayModeDate.getSelectedItemPosition(), saveSettingsListener));
            entryDisplayModeDescription.setOnItemSelectedListener(new AdapterViewOnItemSelectedListener(entryDisplayModeDescription.getSelectedItemPosition(), saveSettingsListener));
        }

        buttonRemoveCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.dialog_remove_cal_title)
                        .setMessage(R.string.dialog_remove_cal_msg)
                        .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // First remove this fragment, so that it cannot be used anymore
                        getActivity().getSupportFragmentManager().beginTransaction().remove(CalendarViewFragment.this).commit();
                        Calendars calendars = Calendars.getInstance(getContext());
                        calendars.removeCalendarByPos(getContext(), calendarPos);
                        // Notify calendar list that the calendar with the given position was removed and return to calendar list in case of CalendarViewActivity
                        Activity hostActivity = getActivity();
                        if (hostActivity instanceof CalendarListActivity) {
                            ((CalendarListActivity) hostActivity).notifyCalendarRemoved(calendarPos); // The host activity should be running while the fragment is running
                        } else if (hostActivity instanceof CalendarViewActivity) {
                            Intent resultData = new Intent();
                            resultData.putExtra(CalendarListActivity.EXTRA_RESULT_CAL_REMOVED, calendarPos);
                            hostActivity.setResult(Activity.RESULT_OK, resultData);
                            hostActivity.finish();
                        } else {
                            throw new ImplementationError("CalendarViewFragment may only be contained in either CalendarListActivity or CalendarViewActivity.");
                        }
                    }
                });
                builder.show();
            }
        });

        return rootView;
    }

    // listener used for both {@link Spinner}s
    private class AdapterViewOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        int selected;
        View.OnClickListener saveSettingsListener;

        public AdapterViewOnItemSelectedListener(int selected, View.OnClickListener saveSettingsListener) {
            this.selected = selected;
            this.saveSettingsListener = saveSettingsListener;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (selected != position) {
                saveSettingsListener.onClick(view);
                // have to update the displayed entries
                entryList.getAdapter().notifyDataSetChanged();
                selected = position;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    public class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        public SimpleItemRecyclerViewAdapter() {

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.entry_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final CEntry entry = calendar.get(position);
            String dateStr = entry.getDateTimeStr(); //NOTE if changing expected length, adapt TextView size
            Calendars calendars = Calendars.getInstance(getContext());
            LinCalConfig config = calendars.getConfigByPos(calendarPos);
            Calendar notificationTime = Calendars.calcNotificationTime(entry, config);
            String notificationTimeStr = Main.dfTime.format(notificationTime.getTime());
            if (config.isEarliestNotificationTimeEnabled() && notificationTime.after(entry.getDate())) {
                dateStr += " (" + notificationTimeStr + ")";
            }
            final String descr = entry.getDescription();
            boolean isEntryDue = entry.getDate().getTime().getTime() <= System.currentTimeMillis();
            if (config.getEntryDisplayModeDate() == LinCal.EntryDisplayMode.SHOW_ALL
                    || config.getEntryDisplayModeDate() == LinCal.EntryDisplayMode.HIDE_FUTURE && isEntryDue) {
                holder.dateView.setText(dateStr);
            } else {
                holder.dateView.setText(R.string.entry_hide_date_text);
            }
            if (config.getEntryDisplayModeDescription() == LinCal.EntryDisplayMode.SHOW_ALL
                    || config.getEntryDisplayModeDescription() == LinCal.EntryDisplayMode.HIDE_FUTURE && isEntryDue) {
                holder.descriptionView.setText(descr);
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEntryDialog(entry, false);
                    }
                });
            } else {
                holder.descriptionView.setText(getString(R.string.entry_hide_description_text));
            }
        }

        private void showEntryDialog(final CEntry entry, boolean showLink) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CalendarViewFragment.this.getActivity());
            StringBuilder msg = new StringBuilder();
            if (entry.getDescription() != null) {
                msg.append(entry.getDescription());
            }
            if (showLink) {
                if (msg.length() > 0) {
                    msg.append("\n\n");
                }
                msg.append(entry.getLink());
            }
            builder.setTitle(entry.getDateStr()).setMessage(msg.toString());
            final AlertDialog dialog = builder.create();
            if (!showLink) {
                dialog.setButton(AlertDialog.BUTTON_NEUTRAL, s(R.string.dialog_show_link), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        dialog.dismiss();
                        showEntryDialog(entry, true);
                    }
                });
            }
            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, s(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    dialogInterface.dismiss();
                }
            });
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, s(R.string.dialog_open_link), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    dialogInterface.dismiss();
                    entry.open(getContext());
                }
            });
            dialog.show();
        }


        @Override
        public int getItemCount() {
            return calendar.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public final View view;
            public final TextView dateView;
            public final TextView descriptionView;

            public ViewHolder(View view) {
                super(view);
                this.view = view;
                dateView = (TextView) view.findViewById(R.id.date);
                descriptionView = (TextView) view.findViewById(R.id.description);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + descriptionView.getText() + "'";
            }
        }
    }

    //NOTE it does not matter to save everything at each single change as the whole file is rewritten anyway
    private void saveSettings() {
        Calendars calendars = Calendars.getInstance(getContext());
        LinCalConfig config = calendars.getConfigByPos(calendarPos);
        config.setNotificationsEnabled(notificationsEnabled.isChecked());
        config.setEarliestNotificationTimeEnabled(earliestNotificationTimeEnabled.isChecked());
        //config.setOnScreenOn(onScreenOnEnabled.isChecked()); //TODO implement
        config.setEntryDisplayModeDate(LinCal.EntryDisplayMode.values()[entryDisplayModeDate.getSelectedItemPosition()]);
        config.setEntryDisplayModeDescription(LinCal.EntryDisplayMode.values()[entryDisplayModeDescription.getSelectedItemPosition()]);
        calendars.save(getContext());
    }

    private void loadSettings() {
        Calendars calendars = Calendars.getInstance(getContext());
        LinCalConfig config = calendars.getConfigByPos(calendarPos);
        notificationsEnabled.setChecked(config.isNotificationsEnabled());
        earliestNotificationTimeEnabled.setChecked(config.isEarliestNotificationTimeEnabled());
        earliestNotificationTime = config.getEarliestNotificationTime();
        textViewEarliestNotificationTime.setText(earliestNotificationTime.toString());
        //onScreenOnEnabled.setChecked(config.isOnScreenOn()); //TODO implement
        entryDisplayModeDate.setSelection(config.getEntryDisplayModeDate().ordinal());
        entryDisplayModeDescription.setSelection(config.getEntryDisplayModeDescription().ordinal());
    }

    private String s(int resId) {
        return getContext().getString(resId);
    }
}
