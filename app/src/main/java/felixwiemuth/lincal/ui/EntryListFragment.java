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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import felixwiemuth.lincal.Main;
import felixwiemuth.lincal.R;
import felixwiemuth.lincal.data.CEntry;
import felixwiemuth.lincal.data.LinCal;
import java.text.DateFormat;

/**
 * A fragment representing a single Calendar screen with a list of its entries.
 * This fragment is either contained in a {@link CalendarListActivity} in
 * two-pane mode (on tablets) or a {@link EntryListActivity} on handsets.
 */
public class EntryListFragment extends Fragment {

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    private LinCal calendar;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EntryListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle("Toolbar title");
            }
            int calendarIndex = getArguments().getInt(ARG_ITEM_ID);
            calendar = Main.get().getCalendars().get(calendarIndex);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.entry_list, container, false);
        //TODO maybe save and load data from savedInstanceState?

        setupRecyclerView((RecyclerView) rootView.findViewById(R.id.entry_list));
        TextView titleView = (TextView) rootView.findViewById(R.id.cal_title);
        titleView.setText(calendar.getTitle());
        TextView authorView = (TextView) rootView.findViewById(R.id.cal_author);
        authorView.setText(calendar.getAuthor());
        ((TextView) rootView.findViewById(R.id.cal_descr)).setText(calendar.getDescription());

        return rootView;
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        SimpleItemRecyclerViewAdapter adapter = new SimpleItemRecyclerViewAdapter();
        recyclerView.setAdapter(adapter);
    }

    public class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final DateFormat dfDay = DateFormat.getDateInstance();

        public SimpleItemRecyclerViewAdapter() {

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.entry_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final CEntry entry = calendar.get(position);
            final String dateStr = dfDay.format(entry.getDate().getTime());
            final String descr = entry.getDescription();
            holder.dateView.setText(dateStr);
            holder.descriptionView.setText(descr);

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEntryDialog(entry, false);
                }
            });
        }

        private void showEntryDialog(final CEntry entry, boolean showLink) {
            AlertDialog.Builder builder = new AlertDialog.Builder(EntryListFragment.this.getActivity());
            StringBuilder msg = new StringBuilder();
            if (entry.getDescription() != null) {
                msg.append(entry.getDescription());
            }
            if (showLink) {
                if (msg.length() > 0) {
                    msg.append("\n\n");

                }
                msg.append(entry.getLink());
            }
            builder.setTitle(dfDay.format(entry.getDate().getTime())).setMessage(msg.toString());
            final AlertDialog dialog = builder.create();
            if (!showLink) {
                dialog.setButton(AlertDialog.BUTTON_NEUTRAL, s(R.string.dialog_show_link), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        dialog.dismiss();
                        showEntryDialog(entry, true);
                    }
                });
            }
            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, s(R.string.dialog_cancle), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    dialogInterface.dismiss();
                }
            });
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, s(R.string.dialog_open_link), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    dialogInterface.dismiss();
                    Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(entry.getLink()));
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException ex) { // there is no activity to handle the link - show a dialog with the string
                        showNonLinkEntryAsDialog(entry);
                    }
                }
            });
            dialog.show();
        }

        private void showNonLinkEntryAsDialog(CEntry entry) {
            AlertDialog.Builder builder = new AlertDialog.Builder(EntryListFragment.this.getActivity());
            builder.setTitle(dfDay.format(entry.getDate().getTime()))
                    .setMessage(entry.getLink())
                    .setPositiveButton(R.string.dialog_dismiss, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }

        @Override
        public int getItemCount() {
            return calendar.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public final View view;
            public final TextView dateView;
            public final TextView descriptionView;

            public ViewHolder(View view) {
                super(view);
                this.view = view;
                dateView = (TextView) view.findViewById(R.id.date);
                descriptionView = (TextView) view.findViewById(R.id.description);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + descriptionView.getText() + "'";
            }
        }
    }

    private String s(int resId) {
        return getContext().getString(resId);
    }
}
