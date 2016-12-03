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

package felixwiemuth.lincal.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import felixwiemuth.lincal.Calendars;
import felixwiemuth.lincal.R;

/**
 * An activity representing a list of calendars. Note that this activity does not update the
 * calendar list while running but each time {@link #onCreate} is called. This activity has
 * different presentations for handset and tablet-size devices. On handsets, the activity presents a
 * list of items, which when touched, lead to a {@link EntryListActivity} representing item details.
 * On tablets, the activity presents the list of items and item details side-by-side using two
 * vertical panes.
 */
public class CalendarListActivity extends AppCompatActivity {

    /**
     * If this extra is given when starting the activity, all calendars are reloaded. If the value
     * is "true", the last calendar in the list is selected.
     */
    public static final String EXTRA_ARG_CONFIG_CHANGED = "felixwiemuth.lincal.CalendarListActivity.EXTRA_ARG_CONFIG_CHANGED"; //TODO update this to "calendar added"

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CalendarListActivity.this, AddCalendarActivity.class));
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.calendar_list);
        assert recyclerView != null;
        setupRecyclerView(recyclerView);

        if (findViewById(R.id.entry_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        if (getIntent().hasExtra(EXTRA_ARG_CONFIG_CHANGED)) {
            if (getIntent().getBooleanExtra(EXTRA_ARG_CONFIG_CHANGED, false)) { // this means a calendar was added, so select the last entry
                recyclerView.scrollToPosition(recyclerView.getChildCount()); //TODO this just scrolls, actually selecting the item is more tricky: http://stackoverflow.com/questions/27377830/what-is-the-equivalent-listview-setselection-in-case-of-recycler-view
            }
        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        SimpleItemRecyclerViewAdapter adapter = new SimpleItemRecyclerViewAdapter();
        recyclerView.setAdapter(adapter);
        //addListChangeListener(adapter);
    }

    /**
     * Adapter to represent calendar titles in a list.
     */
    public class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        public SimpleItemRecyclerViewAdapter() {
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.calendar_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.titleView.setText(Calendars.getInstance(CalendarListActivity.this).getConfigByPos(position).getCalendarTitle());
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putInt(EntryListFragment.ARG_CALENDAR_POS, position);
                        EntryListFragment fragment = new EntryListFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.entry_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, EntryListActivity.class);
                        intent.putExtra(EntryListFragment.ARG_CALENDAR_POS, position);
                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return Calendars.getInstance(CalendarListActivity.this).getCalendarCount();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public final View view;
            public final TextView titleView;

            public ViewHolder(View view) {
                super(view);
                this.view = view;
                titleView = (TextView) view.findViewById(R.id.title);
            }
        }
    }
}
