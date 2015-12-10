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
import felixwiemuth.lincal.R;
import felixwiemuth.lincal.data.CEntry;
import felixwiemuth.lincal.data.LinCal;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.ListIterator;
import java.util.regex.Pattern;
import linearfileparser.ArgKeyProcessor;
import linearfileparser.IllegalLineException;
import linearfileparser.LinearFileParser;
import linearfileparser.ParseException;
import linearfileparser.UnknownKeyException;
import linearfileparser.UnknownSectionException;

/**
 *
 * @author Felix Wiemuth
 */
public class LinCalParser extends LinearFileParser {

    private final static Pattern DATE_PATTERN = Pattern.compile("/");

    //TODO use string resources
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

    private final static String SWITCHDATE = "d";
    private final static String ENTRY_DESCRIPTION = "descr";

    private final Context context;
    private LinCal.Builder c;
    private CEntry.Builder e;

    // parsing state
    private final Calendar currentDate = Calendar.getInstance();
    private boolean firstDate = false; // indicates that in MAIN section a date was set

    public LinCalParser(Context context) {
        super("#", "@", null, HEADER);
        this.context = context;

        addSection(HEADER);
        addSection(MAIN);

        addKeyProcessor(HEADER, new KeyProcessor(BEGIN_MAIN) {
            @Override
            public void process(String arg, ListIterator it) throws ParseException {
                changeSection(MAIN);
            }
        });

        addKeyProcessor(HEADER, new ArgKeyProcessor(CAL_TITLE) {
            @Override
            public void _process(String arg, ListIterator<String> it) throws ParseException {
                c.title(arg);
            }
        });
        addKeyProcessor(HEADER, new ArgKeyProcessor(CAL_AUTHOR) {
            @Override
            public void _process(String arg, ListIterator<String> it) throws ParseException {
                c.author(arg);
            }
        });
        addKeyProcessor(HEADER, new ArgKeyProcessor(CAL_DESCRIPTION) {
            @Override
            public void _process(String arg, ListIterator<String> it) throws ParseException {
                c.description(arg);
            }
        });
        addKeyProcessor(HEADER, new ArgKeyProcessor(CAL_VERSION) {
            @Override
            public void _process(String arg, ListIterator<String> it) throws ParseException {
                c.version(arg);
            }
        });
        addKeyProcessor(HEADER, new ArgKeyProcessor(CAL_DATE) {
            @Override
            public void _process(String arg, ListIterator<String> it) throws ParseException {
                setDate(arg, 3, s(R.string.invalidDateSpecificationException_in_header));
                c.date(currentDate);
            }
        });

        /**
         * A line which starts with neither of comment, key or section prefix is
         * interpreted as a link.
         */
        setDefaultProcessor(new DefaultProcessor() {
            @Override
            public boolean run(String line, ListIterator<String> it) throws IllegalLineException, ParseException {
                if (!firstDate) {
                    throw new DateSpecificationRequiredException(getCurrentLineNumber(), s(R.string.dateSpecificationRequiredException));
                }
                e.date(currentDate).link(line);
                c.addCEntry(e.build());
                e = CEntry.builder();
                currentDate.add(Calendar.DAY_OF_MONTH, 1);
                return true;
            }
        });

        addKeyProcessor(MAIN, new ArgKeyProcessor(SWITCHDATE) {
            @Override
            public void _process(String arg, ListIterator<String> it) throws ParseException {
                if (!firstDate) { // this is the first date specification
                    setDate(arg, 3, s(R.string.invalidDateSpecificationException_first));
                    firstDate = true;
                } else {
                    setDate(arg);
                }
            }
        });

        addKeyProcessor(MAIN, new ArgKeyProcessor(ENTRY_DESCRIPTION) {
            @Override
            public void _process(String arg, ListIterator<String> it) throws ParseException {
                e.description(arg);
            }
        });
    }

    private void setDate(String changeSpec, int min, String minError) throws InvalidDateSpecificationException {
        int changed;
        try {
            changed = setDate(changeSpec);
        } catch (NumberFormatException ex) {
            throw new InvalidDateSpecificationException(getCurrentLineNumber(), s(R.string.invalidDateSpecificationException_base) + " " + ex.getMessage());
        }

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
     * Change the current date according to one of the following patterns. "d",
     * "d/m", "d/m/y" where d is {@link Calendar#DAY_OF_MONTH}, m is
     * {@link Calendar#MONTH} and y is {@link Calendar#YEAR}.
     *
     * @param changeSpec
     * @return 0 if due to a wrong specification nothing was changed or 1,2,3 if
     * d,d+m,d+m+y was changed
     * @throws NumberFormatException if the date specification matches the basic
     * format (d/m/y) but one is not a number (fields may have been changed
     * before)
     */
    private int setDate(String changeSpec) throws NumberFormatException {
        int changed = 0;
        String[] split = DATE_PATTERN.split(changeSpec);
        if (split.length == 0 || split.length > 3) {
            return 0;
        }
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
        return changed;
    }

    /**
     *
     * @param file
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     * @throws UnknownKeyException
     * @throws UnknownSectionException
     * @thorws MissingArgumentException
     * @throws ParseException
     */
    public LinCal parse(File file) throws IOException, FileNotFoundException, UnknownKeyException, UnknownSectionException, ParseException {
        c = LinCal.builder();
        e = CEntry.builder();
        _parse(file);
        return c.build();

    }

    private String s(int i) {
        return context.getString(i);
    }
}
