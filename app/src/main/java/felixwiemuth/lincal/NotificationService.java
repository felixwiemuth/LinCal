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

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import java.util.Calendar;

import felixwiemuth.lincal.data.CEntry;
import felixwiemuth.lincal.data.LinCal;
import felixwiemuth.lincal.data.LinCalConfig;

/**
 * A one-shot service taking care of sending the notifications as specified by the calendars and the
 * user's settings. On activation it checks all calendars registered in the configuration file for
 * due entries and then uses {@link android.app.AlarmManager} to schedule its next execution.
 */
public class NotificationService extends IntentService {
    //TODO for now ignores calendar parameter and processes all calendars, due to the problem that setting multiple alarms seems to cancel previous alarms (even if intents are different). This problem needs further investigation.

    /**
     * The id of the calendar to be processed by the service.
     */
    public static final String EXTRA_CALENDAR_ID = "felixwiemuth.lincal.NotificationService.CALENDAR_ID";

    public NotificationService() {
        super("LinCalNotificationService");
    }

    private Calendars calendars;


    @Override
    protected void onHandleIntent(Intent intent) {
        calendars = Calendars.getInstance(this);
        Calendar now = Calendar.getInstance();
        Calendar nextAlarm = null;
        for (int i = 0; i < calendars.getCalendarCount(); i++) {
            LinCalConfig config = calendars.getConfigByPos(i);
            if (config.isNotificationsEnabled()) { // only load calendar if notifications are enabled
                LinCal cal = calendars.getCalendarByPos(this, i);
                if (cal != null) { // if the calendar could not be loaded, skip it (this will also skip scheduling of next notifications for this calendar)
                    Calendar nextTime = processCalendar(cal, config, now);
                    if (nextAlarm == null || (nextTime != null && nextTime.before(nextAlarm))) {
                        nextAlarm = nextTime;
                    }
                }
            }
        }
        calendars.save(this);
        // Schedule next processing if there are further entries
        if (nextAlarm != null) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent processIntent = new Intent(this, NotificationService.class);
            PendingIntent alarmIntent = PendingIntent.getService(this, 0, processIntent, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextAlarm.getTimeInMillis(), alarmIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, nextAlarm.getTimeInMillis(), alarmIntent);
            }
        }
        stopSelf();
    }

    /**
     * Send notifications due for a calendar (regardless whether it has {@code
     * cal.isNotificationsEnabled() == true}.
     *
     * @param cal
     * @param config
     * @param now
     * @return
     */
    private Calendar processCalendar(LinCal cal, LinCalConfig config, Calendar now) {
        int pos = config.getPos();
        while (pos < cal.size() && (!Calendars.calcNotificationTime(cal.get(pos), config).after(now))) {
            sendNotification(cal.get(pos), pos, config);
            pos++;
        }
        config.setPos(pos);
        if (pos < cal.size()) {
            return Calendars.calcNotificationTime(cal.get(pos), config);
        } else {
            return null;
        }
    }

    private void sendNotification(CEntry entry, int entryPos, LinCalConfig config) {
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(entry.getLink()));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT); //TODO check whether this replaces notifications
        NotificationCompat.Builder nb = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.ic_menu_today)
                .setContentTitle(config.getCalendarTitle())
                .setContentText(entry.getDescription())
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(config.getId() * 1000 + entryPos, nb.build()); //TODO manage ID
    }

    public static void runWithCalendar(Context context, int calendarId) {
        Intent intent = new Intent(context, NotificationService.class);
        intent.putExtra(EXTRA_CALENDAR_ID, calendarId);
        context.startService(intent);
    }
}
