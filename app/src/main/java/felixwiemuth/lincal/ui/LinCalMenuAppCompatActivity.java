/*
 * Copyright (C) 2017 Felix Wiemuth
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

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import felixwiemuth.lincal.R;
import felixwiemuth.lincal.ui.actions.DisplayChangeLog;

/**
 * {@link AppCompatActivity} with the LinCal main menu in the action bar.
 *
 * @author Felix Wiemuth
 */
public class LinCalMenuAppCompatActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_help:
                HtmlDialogFragment.displayHtmlDialogFragment(getSupportFragmentManager(), R.string.menu_help, R.raw.help);
                return true;
            case R.id.menu_about:
                try {
                    PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    String title = getString(R.string.app_name) + " " + packageInfo.versionName;
                    HtmlDialogFragment.displayHtmlDialogFragment(getSupportFragmentManager(), title, R.raw.about, DisplayChangeLog.class);
                    return true;
                } catch (PackageManager.NameNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
