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

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import felixwiemuth.lincal.data.LinCal;
import felixwiemuth.lincal.data.LinCalConfig;
import felixwiemuth.lincal.data.LinCalConfigStore;
import felixwiemuth.lincal.parser.LinCalParser;
import linearfileparser.ParseException;

import static felixwiemuth.lincal.Util.showErrorDialog;

/**
 * This is the single way calendars should be retrieved from, added to and removed from the
 * configuration. It serves as a cache, only parsing calendar files on demand and then keeping them.
 * The instance of this class may be cleared at any time by the runtime system. Calendars can be
 * accessed by the position in the order they have been added or by their unique id.
 *
 * @author Felix Wiemuth
 */
public class Calendars {
    private static final Calendars instance = new Calendars();
    private Context context; //NOTE: a context has to be provided when obtaining the instance
    private LinCalConfigStore configStore;
    /**
     * NOTE: It would be enough to load every calendar file once but as adding the same calendar
     * file multiple times is not a usual use case we chose the simpler way of loading once per id.
     */
    private final Map<Integer, LinCal> calendarsById = new HashMap<>();
    private final Map<Integer, LinCalConfig> configsById = new HashMap<>();

    private Calendars() {
    }

    /**
     * Obtain an instance to access calendars. Note that subsequent calls to this method may return
     * different instances as the runtime system may unload classed. If you want to save changes
     * made to an instance make sure to call {@link #save()} on the same instance as obtained by
     * calling this method.
     *
     * @return
     */
    public static Calendars getInstance(Context context) {
        instance.context = context;
        instance.loadConfig();
        return instance;
    }

    private void loadConfig() {
        configStore = new LinCalConfigStore(context);
        for (LinCalConfig linCalConfig : configStore.getEntries()) {
            configsById.put(linCalConfig.getId(), linCalConfig);
        }
    }

    public int getCalendarCount() {
        return configStore.getEntries().size();
    }

    /**
     * Get the calendar at the given position in adding order.
     *
     * @param pos
     * @return
     */
    public LinCal getCalendarByPos(int pos) {
        try {
            return getCalendarById(configStore.getEntries().get(pos).getId());
        } catch (IndexOutOfBoundsException ex) {
            throw new RuntimeException("Illegal calendar position used.");
        }
    }

    /**
     * Get the calendar with the given id.
     *
     * @param id
     * @return the calendar or {@code null} if there was an error loading the calendar
     */
    public LinCal getCalendarById(int id) {
        if (calendarsById.get(id) == null) {
            LinCalConfig config = configsById.get(id);
            if (config == null) {
                throw new RuntimeException("Illegal calendar id used.");
            }
            // load (parse) the calendar
            calendarsById.put(id, loadCalendar(config.getCalendarFile(), context)); //NOTE if the returned calendar is null it will be loaded again on next request
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
     * Add a calendar to the configuration and save it. It is added in the last position. Runs
     * {@link NotificationService} for the new calendar.
     *
     * @param calendarFile
     * @param calendarTitle
     * @param notificationMode
     * @param notificationTime
     * @return the id of the new calendar
     */
    public int addCalendar(String calendarFile, String calendarTitle, LinCalConfig.NotificationMode notificationMode, Date notificationTime) {
        int id = configStore.add(calendarFile, calendarTitle, notificationMode, notificationTime);
        configStore.save();
        NotificationService.runWithCalendar(context, id);
        return id;
    }

    public void removeCalendarByPos(int pos) {
        configsById.remove(configStore.getEntries().get(pos).getId());
        configStore.getEntries().remove(pos);
    }

    /**
     * Write the current configuration to the configuration file. This is automatically called by
     * {@link #addCalendar(String, String, LinCalConfig.NotificationMode, Date)} and {@link
     * #removeCalendarByPos(int)} but has to be called manually when changing a configuration
     * obtained by {@link #getConfigByPos(int)} or {@link #getConfigById(int)}.
     */
    public void save() {
        configStore.save();
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
     * @param file
     * @return the loaded calendar or {@code null} if there was an error
     */
    public static LinCal loadCalendar(String file, Context context) {
        try {
            return new LinCalParser(context).parse(new File(file));
        } catch (FileNotFoundException ex) {
            showErrorDialog(R.string.dialog_file_not_found, String.format(context.getString(R.string.dialog_file_not_found_msg), file), context);
        } catch (IOException ex) {
            showErrorDialog(R.string.dialog_error_title, ex.getMessage(), context);
        } catch (ParseException ex) {
            showErrorDialog(R.string.dialog_parsing_error_title, ex.getMessage(), context);
        }
        return null;
    }
}
