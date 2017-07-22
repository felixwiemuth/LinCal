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

package felixwiemuth.lincal.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import felixwiemuth.lincal.R;
import felixwiemuth.lincal.ui.CalendarListActivity;

/**
 * Utility methods.
 *
 * @author Felix Wiemuth
 */
public class Util {

    public static void showMessageDialog(int resTitle, String message, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setTitle(resTitle).setMessage(message);
        builder.show();
    }

    public static void showMessageDialog(int resTitle, int resMessage, Context context) {
        showMessageDialog(resTitle, context.getString(resMessage), context);
    }

    /**
     * Show an error dialog in the given context (or optionally a notification if a dialog cannot be
     * shown).
     *
     * @param title        resource id for the title text
     * @param message      the dialog's main message
     * @param notification whether to show a notification when a dialog cannot be shown in the given
     *                     context (clicking the notification will start {@link
     *                     CalendarListActivity}) - if {@code false} and the dialog cannot be shown,
     *                     the application will crash
     * @param context      context where the dialog should be displayed
     */
    public static void showErrorDialog(int title, String message, boolean notification, Context context) {
        if (notification && !(context instanceof Activity)) {
            Intent intent = new Intent(context, CalendarListActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            NotificationCompat.Builder nb = new NotificationCompat.Builder(context)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(message))
                    .setSmallIcon(android.R.drawable.stat_sys_warning)
                    .setContentTitle(context.getString(title))
                    .setContentText(message)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify((int) System.currentTimeMillis(), nb.build());
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setPositiveButton(R.string.dialog_error_dismiss, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).setTitle(title).setMessage(message);
            builder.show();
        }
    }
}
