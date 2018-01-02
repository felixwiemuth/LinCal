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

package felixwiemuth.lincal.parser;

import android.content.Context;

import felixwiemuth.lincal.R;
import felixwiemuth.lincal.util.ImplementationError;
import felixwiemuth.linearfileparser.localization.ResourceProvider;

/**
 * Adapter to make Android resources available to {@link LinCalParser}.
 *
 * @author Felix Wiemuth
 */
public class AndroidResourceProvider implements ResourceProvider {

    private Context context;

    /**
     * Create an instance of this class. Attention: make sure to keep the lifetime of this instance
     * as short as possible as it keeps the {@link Context} passed.
     *
     * @param context
     */
    public AndroidResourceProvider(Context context) {
        this.context = context;
    }

    @Override
    public String getString(felixwiemuth.linearfileparser.localization.R key) {
        switch (key) {
            case ERROR_AT_LINE:
                return s(R.string.linearfileparser_error_at_line);
            case ILLEGAL_LINE:
                return s(R.string.linearfileparser_illegal_line);
            case UNKNOWN_SECTION:
                return s(R.string.linearfileparser_unknown_section);
            case UNKNOWN_KEY:
                return s(R.string.linearfileparser_unknown_key);
            case REPEATED_KEY:
                return s(R.string.linearfileparser_repeated_key);
            case MISSING_ARGUMENT:
                return s(R.string.linearfileparser_missing_argument);
            default:
                throw new ImplementationError("No resource provided for key " + key);
        }
    }

    private String s(int id) {
        return context.getString(id);
    }
}
