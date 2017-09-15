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

package felixwiemuth.lincal.parser;

import android.content.Context;
import android.net.Uri;

import com.google.common.collect.EnumHashBiMap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.ListIterator;
import java.util.regex.Pattern;

import felixwiemuth.lincal.R;
import felixwiemuth.lincal.data.CEntry;
import felixwiemuth.lincal.data.LinCal;
import felixwiemuth.lincal.util.Time;
import felixwiemuth.linearfileparser.ArgKeyProcessor;
import felixwiemuth.linearfileparser.IllegalLineException;
import felixwiemuth.linearfileparser.LinearFileParser;
import felixwiemuth.linearfileparser.ParseException;
import felixwiemuth.linearfileparser.UnknownKeyException;
import felixwiemuth.linearfileparser.UnknownSectionException;

/**
 * Parses calendars specified in a text file.
 *
 * @author Felix Wiemuth
 */
public class LinCalParser extends LinearFileParser {

    private final static Pattern DATE_PATTERN = Pattern.compile("/");

    //TODO use (non-translatable) string resources
    // sections
    private final static String HEADER = "header";
    private final static String MAIN = "main";

    // keys
    private final static String BEGIN_MAIN = "begin";
    private final static String CAL_TITLE = "title";
    private final static String CAL_AUTHOR = "author";
    private final static String CAL_DESCRIPTION = "descr";
    private final static String CAL_VERSION = "version";
    private final static String CAL_DATE = "date";
    private final static String SET_ENTRY_DISPLAY_MODE_DATE = "setDateDisplayMode";
    private final static String SET_ENTRY_DISPLAY_MODE_DESCRIPTION = "setDescrDisplayMode";
    private final static String FORCE_ENTRY_DISPLAY_MODE_DATE = "forceDateDisplayMode";
    private final static String FORCE_ENTRY_DISPLAY_MODE_DESCRIPTION = "forceDescrDisplayMode";

    public static final EnumHashBiMap<LinCal.EntryDisplayMode, String> ENTRY_DISPLAY_MODE_STRING_MAP = EnumHashBiMap.create(LinCal.EntryDisplayMode.class);

    static {
        ENTRY_DISPLAY_MODE_STRING_MAP.put(LinCal.EntryDisplayMode.HIDE_ALL, "hideAll");
        ENTRY_DISPLAY_MODE_STRING_MAP.put(LinCal.EntryDisplayMode.HIDE_FUTURE, "hideFuture");
        ENTRY_DISPLAY_MODE_STRING_MAP.put(LinCal.EntryDisplayMode.SHOW_ALL, "showAll");
    }

    private final static String SWITCH_DATE = "d";
    private final static String SET_TIME = "t";
    private final static String SET_DEFAULT_TIME = "st";
    private final static String ENTRY_DESCRIPTION = "descr";

    private Context context;
    private LinCal.Builder c;
    private CEntry.Builder e;

    // parsing state
    private final Calendar currentDate = Calendar.getInstance();
    private Time defaultTime;
    private Time currentTime; // the time set for the current entry - null if not set
    private boolean firstDateSet; // indicates that in MAIN section a date was set

    /**
     *
     */
    public LinCalParser() {
        super("#", "@", null, HEADER);

        addSection(HEADER);
        addSection(MAIN);

        addKeyProcessor(HEADER, new KeyProcessor(BEGIN_MAIN, true) {
            @Override
            public void process(String arg, ListIterator it) throws ParseException {
                changeSection(MAIN);
            }
        });

        addKeyProcessor(HEADER, new ArgKeyProcessor(CAL_TITLE, true) {
            @Override
            public void _process(String arg, ListIterator<String> it) throws ParseException {
                c.title(arg);
            }
        });
        addKeyProcessor(HEADER, new ArgKeyProcessor(CAL_AUTHOR, true) {
            @Override
            public void _process(String arg, ListIterator<String> it) throws ParseException {
                c.author(arg);
            }
        });
        addKeyProcessor(HEADER, new ArgKeyProcessor(CAL_DESCRIPTION, true) {
            @Override
            public void _process(String arg, ListIterator<String> it) throws ParseException {
                c.description(arg);
            }
        });
        addKeyProcessor(HEADER, new ArgKeyProcessor(CAL_VERSION, true) {
            @Override
            public void _process(String arg, ListIterator<String> it) throws ParseException {
                c.version(arg);
            }
        });
        addKeyProcessor(HEADER, new ArgKeyProcessor(CAL_DATE, true) {
            @Override
            public void _process(String arg, ListIterator<String> it) throws ParseException {
                setDate(arg, 3, s(R.string.invalidDateSpecificationException_in_header));
                c.date(currentDate);
            }
        });
        addKeyProcessor(HEADER, new ArgKeyProcessor(SET_ENTRY_DISPLAY_MODE_DATE, true) {
            @Override
            public void _process(String arg, ListIterator<String> it) throws ParseException {
                c.entryDisplayModeDate(parseEntryDisplayMode(arg));
            }
        });
        addKeyProcessor(HEADER, new ArgKeyProcessor(SET_ENTRY_DISPLAY_MODE_DESCRIPTION, true) {
            @Override
            public void _process(String arg, ListIterator<String> it) throws ParseException {
                c.entryDisplayModeDescription(parseEntryDisplayMode(arg));
            }
        });
        addKeyProcessor(HEADER, new ArgKeyProcessor(FORCE_ENTRY_DISPLAY_MODE_DATE, true) {
            @Override
            public void _process(String arg, ListIterator<String> it) throws ParseException {
                c.entryDisplayModeDate(parseEntryDisplayMode(arg));
                c.forceEntryDisplayModeDate(true);
            }
        });
        addKeyProcessor(HEADER, new ArgKeyProcessor(FORCE_ENTRY_DISPLAY_MODE_DESCRIPTION, true) {
            @Override
            public void _process(String arg, ListIterator<String> it) throws ParseException {
                c.entryDisplayModeDescription(parseEntryDisplayMode(arg));
                c.forceEntryDisplayModeDescription(true);
            }
        });

        /**
         * A line which starts with neither a comment, key or section prefix is
         * interpreted as a link.
         */
        setDefaultProcessor(new DefaultProcessor() {
            @Override
            public boolean run(String line, ListIterator<String> it) throws IllegalLineException, ParseException {
                if (!firstDateSet) {
                    throw new DateSpecificationRequiredException(getCurrentLineNumber(), s(R.string.dateSpecificationRequiredException));
                }
                if (currentTime != null) {
                    currentTime.setAtCalendar(currentDate);
                } else {
                    defaultTime.setAtCalendar(currentDate);
                }
                currentTime = null;
                e.date(currentDate).link(line);
                c.addCEntry(e.build());
                e = CEntry.builder(); //reset builder for next entry
                currentDate.add(Calendar.DAY_OF_MONTH, 1);
                return true;
            }
        });

        addKeyProcessor(MAIN, new ArgKeyProcessor(SWITCH_DATE) {
            @Override
            public void _process(String arg, ListIterator<String> it) throws ParseException {
                if (!firstDateSet) { // this is the first date specification
                    setDate(arg, 3, s(R.string.invalidDateSpecificationException_first));
                    firstDateSet = true;
                } else {
                    setDate(arg);
                }
            }
        });

        addKeyProcessor(MAIN, new ArgKeyProcessor(SET_TIME) {
            @Override
            public void _process(String arg, ListIterator<String> it) throws ParseException {
                currentTime = new Time(0, 0);
                setTime(arg, currentTime);
            }
        });

        addKeyProcessor(MAIN, new ArgKeyProcessor(SET_DEFAULT_TIME) {
            @Override
            public void _process(String arg, ListIterator<String> it) throws ParseException {
                setTime(arg, defaultTime);
            }
        });

        addKeyProcessor(MAIN, new ArgKeyProcessor(ENTRY_DESCRIPTION) {
            @Override
            public void _process(String arg, ListIterator<String> it) throws ParseException {
                e.description(arg);
            }
        });
    }

    private LinCal.EntryDisplayMode parseEntryDisplayMode(String arg) throws InvalidDisplayModeSpecificationException {
        LinCal.EntryDisplayMode mode = ENTRY_DISPLAY_MODE_STRING_MAP.inverse().get(arg);
        if (mode == null) {
            StringBuilder displayModes = new StringBuilder()
                    .append(ENTRY_DISPLAY_MODE_STRING_MAP.get(LinCal.EntryDisplayMode.HIDE_ALL)).append(", ")
                    .append(ENTRY_DISPLAY_MODE_STRING_MAP.get(LinCal.EntryDisplayMode.HIDE_FUTURE)).append(", ")
                    .append(ENTRY_DISPLAY_MODE_STRING_MAP.get(LinCal.EntryDisplayMode.SHOW_ALL));
            throw new InvalidDisplayModeSpecificationException(getCurrentLineNumber(), sf(R.string.invalidDisplayModeSpecificationException, displayModes.toString()));
        }
        return mode;
    }

    private void setDate(String changeSpec, int min, String minError) throws InvalidDateSpecificationException {
        int changed = setDate(changeSpec);
        if (changed == 0 || changed < min) {
            StringBuilder sb = new StringBuilder();
            sb.append(s(R.string.invalidDateSpecificationException_base)).append(" ");
            if (changed == 0) {
                sb.append(s(R.string.invalidDateSpecificationException_format));
            } else {
                sb.append(s(R.string.invalidDateSpecificationException_expected_full)).append(" ").append(minError);
            }
            sb.append(".");
            throw new InvalidDateSpecificationException(getCurrentLineNumber(), sb.toString());
        }
    }

    /**
     * Change the current date according to one of the following patterns. "d", "d/m", "d/m/y" where
     * d is {@link Calendar#DAY_OF_MONTH}, m is {@link Calendar#MONTH} and y is {@link
     * Calendar#YEAR}.
     *
     * @param changeSpec
     * @return 0 if due to a wrong specification nothing was changed or 1,2,3 if d,d+m,d+m+y was
     * changed
     * @throws InvalidDateSpecificationException if the date specification matches the basic format
     *                                           (d/m/y) but one of the strings for d,m,y is not a
     *                                           number (fields may have been changed before)
     */
    private int setDate(String changeSpec) throws InvalidDateSpecificationException {
        int changed = 0;
        String[] split = DATE_PATTERN.split(changeSpec);
        if (split.length == 0 || split.length > 3) {
            return 0;
        }
        try {
            currentDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(split[0]));
            changed++;
            if (split.length > 1) {
                currentDate.set(Calendar.MONTH, Integer.parseInt(split[1]) - 1);
                changed++;
                if (split.length > 2) {
                    currentDate.set(Calendar.YEAR, Integer.parseInt(split[2]));
                    changed++;
                }
            }
        } catch (NumberFormatException ex) {
            throw new InvalidDateSpecificationException(getCurrentLineNumber(), s(R.string.invalidDateSpecificationException_base) + " " + s(R.string.invalidDateSpecificationException_format) + ".");
        }
        return changed;
    }

    private void setTime(String timeSpec, Time time) throws InvalidTimeSpecificationException {
        if (!time.set(timeSpec)) {
            throw new InvalidTimeSpecificationException(getCurrentLineNumber(), s(R.string.invalidTimeSpecificationException_base) + " " + timeSpec + " " + s(R.string.invalidTimeSpecificationException_format) + ".");
        }
    }

    /**
     * @param path    simple path or content URI to the calendar file
     * @param context application context needed to provide String resources
     * @return
     * @throws UnsupportedUriException if the URI given has a scheme which is not supported
     * @throws IOException
     * @throws FileNotFoundException
     * @throws UnknownKeyException
     * @throws UnknownSectionException
     * @throws ParseException
     */
    public LinCal parse(String path, Context context) throws UnsupportedUriException, IOException, FileNotFoundException, UnknownKeyException, UnknownSectionException, ParseException {
        this.context = context;
        setResourceProvider(new AndroidResourceProvider(context));
        // Initialize parser
        currentDate.setTimeInMillis(0);
        defaultTime = new Time(0, 0);
        currentTime = null;
        firstDateSet = false;
        c = LinCal.builder();
        e = CEntry.builder();
        // Check whether the file is a content URI or a simple file path, run parser
        Uri uri = Uri.parse(path);
        String scheme = uri.getScheme();
        if (scheme != null) {
            if (scheme.equals("content")) {
                _parse(context.getContentResolver().openInputStream(uri));
            } else {
                throw new UnsupportedUriException(scheme);
            }
        } else {
            _parse(new FileInputStream(path));
        }
        try {
            return c.build();
        } catch (LinCal.Builder.MissingFieldException ex) {
            //NOTE: could do the check already with a "leave action" of the header section but that would require much more code
            throw newParseException(getCurrentLineNumber(), sf(R.string.parseException_missing_field, ex.getField()));
        } finally {
            context = null; // possibly free resources
            setResourceProvider(null);
        }
    }

    private String s(int i) {
        return context.getString(i);
    }

    private String sf(int i, Object... args) {
        return String.format(s(i), args);
    }
}
