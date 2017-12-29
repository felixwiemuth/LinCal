/*
 * Copyright (C) 2016 Felix Wiemuth
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

package felixwiemuth.lincal;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import felixwiemuth.lincal.data.CEntry;
import felixwiemuth.lincal.data.LinCal;
import felixwiemuth.lincal.data.LinCalConfig;
import felixwiemuth.lincal.data.LinCalConfigStore;
import felixwiemuth.lincal.parser.LinCalParser;
import felixwiemuth.lincal.parser.UnsupportedUriException;
import felixwiemuth.linearfileparser.ParseException;

import static felixwiemuth.lincal.util.Util.showErrorDialog;

/**
 * This is the single way calendars should be retrieved from, added to and removed from the
 * configuration. It serves as a cache, only parsing calendar files on demand and then keeping them.
 * The instance of this class may be cleared at any time by the runtime system. Calendars can be
 * accessed by the position in the order they have been added or by their unique id.
 *
 * @author Felix Wiemuth
 */
public class Calendars {
    private static Calendars instance;
    private LinCalConfigStore configStore;
    /**
     * NOTE: It would be enough to load every calendar file once but as adding the same calendar
     * file multiple times is not a usual use case we choose the simpler way of loading once per id.
     */
    private final Map<Integer, LinCal> calendarsById = new HashMap<>();
    private final Map<Integer, LinCalConfig> configsById = new HashMap<>();

    private Calendars(Context context) {
        configStore = new LinCalConfigStore(context);
        for (LinCalConfig linCalConfig : configStore.getEntries()) {
            configsById.put(linCalConfig.getId(), linCalConfig);
        }
    }

    /**
     * Obtain an instance to access calendars. Note that subsequent calls to this method may return
     * different instances as the runtime system may unload classes. If you want to save changes
     * made to an instance make sure to call {@link #save(Context)} on the same instance as obtained
     * by calling this method.
     *
     * @param context
     * @return
     */
    public static Calendars getInstance(Context context) {
        if (instance == null) {
            instance = new Calendars(context);
        }
        return instance;
    }

    /**
     * @return
     */
    public int getCalendarCount() {
        return configStore.getEntries().size();
    }

    /**
     * Get the calendar at the given position in adding order. Returns the calendar from cache if
     * already present or loads it otherwise.
     *
     * @param context
     * @param pos
     * @return the calendar from cache or {@code null} if there was an error loading it
     */
    public LinCal getCalendarByPos(Context context, int pos) {
        try {
            return getCalendarById(context, configStore.getEntries().get(pos).getId());
        } catch (IndexOutOfBoundsException ex) {
            throw new RuntimeException("Illegal calendar position used.");
        }
    }

    /**
     * Get the calendar with the given id. Returns the calendar from cache if already present or
     * loads it otherwise, updating the config with values from the calendar.
     *
     * @param context
     * @param id
     * @return the calendar or {@code null} if there was an error loading it (it will be tried to be
     * loaded again on next call of this method)
     */
    public LinCal getCalendarById(Context context, int id) {
        if (calendarsById.get(id) == null) {
            LinCalConfig config = getConfigById(id);
            LinCal calendar = loadCalendar(context, config.getCalendarFile());
            if (calendar == null) {
                config.setNotificationsEnabled(false); // disable notifications to avoid further error notifications when running the service (which can't process the calendar anyway)
                save(context);
            } else {
                calendarsById.put(id, calendar);
                // The following settings are overridden by the calendar's values but should not be saved (in case the calendar removes its settings, the previous values are restored)
                if (calendar.hasForceEntryDisplayModeDate()) {
                    config.setEntryDisplayModeDate(calendar.getEntryDisplayModeDate());
                }
                if (calendar.hasForceEntryDisplayModeDescription()) {
                    config.setEntryDisplayModeDate(calendar.getEntryDisplayModeDescription());
                }
            }
        }
        return calendarsById.get(id);
    }

    /**
     * Get the configuration of the calendar at the given position in adding order.
     *
     * @param pos
     * @return
     */
    public LinCalConfig getConfigByPos(int pos) {
        try {
            return configStore.getEntries().get(pos);
        } catch (IndexOutOfBoundsException ex) {
            throw new RuntimeException("Illegal calendar position used.");
        }
    }

    /**
     * Get the configuration of the calendar with the given id.
     *
     * @param id
     * @return
     */
    public LinCalConfig getConfigById(int id) {
        if (!configsById.containsKey(id)) {
            throw new RuntimeException("Illegal calendar id used.");
        }
        return configsById.get(id);
    }

    /**
     * @param file
     * @return
     */
    public boolean calendarFromFileExists(String file) {
        return configStore.containsCalendarFile(file);
    }

    /**
     * Add a calendar to the configuration and save it. It is added in the last position. Runs
     * {@link NotificationService} for the new calendar.
     *
     * @param context
     * @param config  the configuration for the new calendar (the id will be overwritten).
     * @return the id of the new calendar
     */
    public int addCalendar(Context context, LinCalConfig config) {
        int id = configStore.add(config);
        configsById.put(id, configStore.getEntries().get(configStore.getEntries().size() - 1));
        configStore.save(context);
        NotificationService.runWithCalendar(context, id);
        return id;
    }

    /**
     * Add a calendar to the configuration and save it. First checks whether a calendar with the
     * given file is already present in the configuration and if so shows a confirmation dialog to
     * ask whether to proceed. Also checks whether the title is valid. It is added in the last
     * position. Runs {@link NotificationService} for the new calendar.
     *
     * @param config  the configuration for the new calendar (if the title is "", the calendar's
     *                title will be used; the id will be overwritten).
     * @param context
     * @param finish  action to be performed when the calendar has been added (and not otherwise)
     */
    public static void addCalendarChecked(final LinCalConfig config, final Context context, final Runnable finish) {
        final Calendars instance = getInstance(context);
        // First load calendar to check syntax and get information (title)
        LinCal calendar = loadCalendar(context, config.getCalendarFile());
        if (calendar == null) {
            return;
        }
        if (config.getCalendarTitle().equals("")) {
            config.setCalendarTitle(calendar.getTitle());
        }
        if (config.getCalendarTitle().contains(LinCalConfig.SEPARATOR)) {
            showErrorDialog(R.string.dialog_error_title, String.format(context.getString(R.string.dialog_symbol_not_allowed_message), LinCalConfig.SEPARATOR), true, context);
            return;
        }
        if (config.getEntryDisplayModeDate() == null) {
            config.setEntryDisplayModeDate(calendar.getEntryDisplayModeDate());
        }
        if (config.getEntryDisplayModeDescription() == null) {
            config.setEntryDisplayModeDescription(calendar.getEntryDisplayModeDescription());
        }
        if (instance.configStore.containsCalendarFile(config.getCalendarFile())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.dialog_cal_already_added_title).setMessage(R.string.dialog_cal_already_added_msg).setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            }).setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    instance.addCalendar(context, config); //NOTE keeping context for existence of dialog
                    finish.run();
                }
            });
            builder.show();
        } else {
            instance.addCalendar(context, config);
            finish.run();
        }
    }

    /**
     * Remove a calendar from the configuration and save it.
     *
     * @param context
     * @param pos
     */
    public void removeCalendarByPos(Context context, int pos) {
        int id = configStore.getEntries().get(pos).getId();
        configsById.remove(id);
        calendarsById.remove(id);
        configStore.getEntries().remove(pos);
        save(context);
    }

    /**
     * Write the current configuration to the configuration file. This is automatically called by
     * {@link #addCalendar(Context, LinCalConfig)} and {@link #removeCalendarByPos(Context, int)}
     * but has to be called manually when changing a configuration obtained by {@link
     * #getConfigByPos(int)} or {@link #getConfigById(int)}.
     *
     * @param context
     */
    public void save(Context context) {
        configStore.save(context);
    }

    /**
     * Checks whether a calendar from the given file has already been added.
     *
     * @param file
     * @return
     */
    public boolean calendarWithFilePresent(String file) {
        return configStore.containsCalendarFile(file);
    }

    /**
     * Load a calendar and show an error dialog on failure.
     *
     * @param context
     * @param path    simple path or content URI to the calendar file
     * @return the loaded calendar or {@code null} if there was an error
     */
    public static LinCal loadCalendar(Context context, String path) {
        try {
            return new LinCalParser().parse(path, context);
        } catch (UnsupportedUriException ex) {
            showErrorDialog(R.string.dialog_unsupported_URI_title, String.format(context.getString(R.string.dialog_unsupported_URI_msg), ex.getScheme()), true, context);
        } catch (FileNotFoundException ex) {
            showErrorDialog(R.string.dialog_file_not_found_title, String.format(context.getString(R.string.dialog_file_not_found_msg), path), true, context);
        } catch (IOException ex) {
            showErrorDialog(R.string.dialog_error_title, ex.getMessage(), true, context);
        } catch (ParseException ex) {
            showErrorDialog(R.string.dialog_parsing_error_title, ex.getMessage(), true, context);
        }
        return null;
    }

    //TODO consider default configuration

    /**
     * @param entry
     * @param config
     * @return
     */
    public static Calendar calcNotificationTime(CEntry entry, LinCalConfig config) {
        Calendar notificationTime = Calendar.getInstance();
        notificationTime.setTime(entry.getDate().getTime());
        if (config.isEarliestNotificationTimeEnabled() && config.getEarliestNotificationTime().after(notificationTime)) {
            notificationTime.set(Calendar.HOUR_OF_DAY, config.getEarliestNotificationTime().getHour());
            notificationTime.set(Calendar.MINUTE, config.getEarliestNotificationTime().getMinute());
        }
        return notificationTime;
    }

    /**
     * @param entry
     * @param calendarPos
     * @return
     */
    public Calendar calcNotificationTime(CEntry entry, int calendarPos) {
        return calcNotificationTime(entry, getConfigByPos(calendarPos));
    }

    /**
     * Invalidate the current instance. Should be called when the configuration file is changed in
     * another way than through this class. Note that any changes made to the current instance which
     * have not been written back (by calling {@link #save(Context)}) will be lost.
     */
    public static void invalidate() {
        instance = null;
    }
}
