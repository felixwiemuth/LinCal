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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import felixwiemuth.lincal.R;

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
     * Show an error dialog in the given context. Make sure to call this only with contexts that can
     * display a dialog (or accept crash of application).
     *
     * @param title   resource id for the title text
     * @param message the dialog's main message
     * @param context context where the dialog should be displayed
     */
    public static void showErrorDialog(int title, String message, Context context) {
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
