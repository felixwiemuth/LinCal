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

package felixwiemuth.lincal;

import android.app.Application;
import felixwiemuth.lincal.data.LinCal;
import felixwiemuth.lincal.data.ListChangeListener;
import felixwiemuth.lincal.data.ListChangeListeners;
import java.util.ArrayList;
import java.util.List;
import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(
        mailTo = "felixwiemuth@hotmail.de",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text)
public class Main extends Application {

    private static final Main instance = new Main();

    private final List<LinCal> calendars = new ArrayList<>();
    private final ListChangeListeners listeners = new ListChangeListeners();

    public Main() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
    }

    public List<LinCal> getCalendars() {
        return calendars;
    }

    /**
     * Adds calendar to end of list.
     *
     * @param calendar
     */
    public void addCalendar(final LinCal calendar) {
        final int insert = calendars.size();
        calendars.add(calendar);
        listeners.notify(new ListChangeListeners.Notifier() {
            @Override
            public void notify(ListChangeListener listener) {
                listener.onItemInserted(insert);
            }
        });
    }

    public void addListChangeListener(ListChangeListener listener) {
        listeners.add(listener);
    }

    public String s(int resId) {
        return getApplicationContext().getString(resId);
    }

    public static Main get() {
        return instance;
    }

}
