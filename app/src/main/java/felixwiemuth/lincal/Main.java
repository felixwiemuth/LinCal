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
import android.content.Context;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import java.io.File;
import java.text.DateFormat;

import felixwiemuth.lincal.data.LinCalConfigStore;

@ReportsCrashes(
        mailTo = "felixwiemuth@hotmail.de",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text)
public class Main extends Application {

    public static final DateFormat dfDay = DateFormat.getDateInstance();
    public static final DateFormat dfDayTime = DateFormat.getDateTimeInstance();


    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG) {
            ACRA.init(this);
        }
        update(this);
    }

    /**
     * Checks whether the configuration has to be updated to a new format and performs updates if
     * necessary. Also initializes the configuration on first launch.
     */
    public static void update(Context context) {
        File dir = context.getFilesDir();
        File configDir = new File(dir, "config-0");
        if (!configDir.exists()) {
            if (!configDir.mkdir()) {
                throw new RuntimeException("Could not create configuration directory.");
            }
            LinCalConfigStore.createInitialConfigurationFile(context);
        }
    }
}
