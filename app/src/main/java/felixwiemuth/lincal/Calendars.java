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
import java.util.HashMap;
import java.util.Map;

import felixwiemuth.lincal.data.LinCal;
import felixwiemuth.lincal.data.LinCalConfig;
import felixwiemuth.lincal.parser.LinCalParser;
import linearfileparser.ParseException;

import static felixwiemuth.lincal.Util.showErrorDialog;

/**
 * This singleton provides access to the calendars specified in the configuration file. Parses
 * calendars on demand. Cache has to be invalidated each time the configuration file is changed.
 *
 * @author Felix Wiemuth
 */
public class Calendars {
    private static final Calendars instance = new Calendars();
    private LinCalConfig config;
    private final Map<Integer, LinCal> calendars = new HashMap<>();

    private Calendars() {
    }

    /**
     * Get the calendar at a given position in the configuration file.
     *
     * @param pos
     * @param context
     * @return the calendar or {@code null} if there was an error loading the calendar
     */
    public static LinCal getCalendar(int pos, Context context) {
        instance.loadConfig(context);
        if (instance.calendars.get(pos) == null) {
            instance.calendars.put(pos, loadCalendar(instance.config.getEntries().get(pos).getCalendarFile(), context)); //NOTE if the returned calendar is null it will be loaded again on next request
        }
        return instance.calendars.get(pos);
    }

    public static int getCalendarCount(Context context) {
        instance.loadConfig(context);
        return instance.config.getEntries().size();
    }

    public static LinCalConfig.Entry getConfig(int pos, Context context) {
        instance.loadConfig(context);
        return instance.config.getEntries().get(pos);
    }

    private void loadConfig(Context context) {
        config = new LinCalConfig(context);
        //TODO have to check whether loaded calendars still correspond to new config and invalidate if not => if the file name matches, that is enough! Could even index calendars by filename!
    }

    public static void invalidate() {
        instance.config = null;
    }

    /**
     * Load a calendar and show an error dialog on failure.
     *
     * @param file
     * @param context
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
