/*
 * Copyright 2013 Adam Speakman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package felixwiemuth.lincal.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import felixwiemuth.lincal.R;

//If you don't support Android 2.x, you should use the non-support version!

/**
 * Created by Adam Speakman on 24/09/13. http://speakman.net.nz
 * <p>
 * Edited by Felix Wiemuth 12/2016.
 */

/**
 * Displays an HTML document in a dialog fragment.
 */
public class HtmlDialogFragment extends DialogFragment {

    private AsyncTask<Void, Void, String> mLicenseLoader;

    private static final String FRAGMENT_TAG = "nz.net.speakman.androidlicensespage.HtmlDialogFragment";
    private static final String ARG_TITLE = "felixwiemuth.lincal.ARG_TITLE";
    private static final String ARG_RES_HTML_FILE = "felixwiemuth.lincal.ARG_RES_HTML_FILE";

    /**
     * Builds and displays a HTML dialog fragment.
     *
     * @param fm          A fragment manager instance used to display this HtmlDialogFragment.
     * @param resTitle    The title for the dialog, as string resource
     * @param resHtmlFile
     */
    public static void displayHtmlDialogFragment(FragmentManager fm, @StringRes int resTitle, @RawRes int resHtmlFile) {
        Bundle arguments = new Bundle();
        arguments.putInt(ARG_TITLE, resTitle);
        arguments.putInt(ARG_RES_HTML_FILE, resHtmlFile);
        constructFragment(arguments).displayFragment(fm);
    }

    /**
     * Builds and displays a HTML dialog fragment.
     *
     * @param fm          A fragment manager instance used to display this HtmlDialogFragment.
     * @param title       The title for the dialog, as string
     * @param resHtmlFile
     */
    public static void displayHtmlDialogFragment(FragmentManager fm, String title, @RawRes int resHtmlFile) {
        Bundle arguments = new Bundle();
        arguments.putString(ARG_TITLE, title);
        arguments.putInt(ARG_RES_HTML_FILE, resHtmlFile);
        constructFragment(arguments).displayFragment(fm);
    }

    private static HtmlDialogFragment constructFragment(Bundle arguments) {
        // Create and show the dialog.
        HtmlDialogFragment newFragment = new HtmlDialogFragment();
        newFragment.setArguments(arguments);
        return newFragment;
    }

    private void displayFragment(FragmentManager fm) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(FRAGMENT_TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        show(ft, FRAGMENT_TAG);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadLicenses();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLicenseLoader != null) {
            mLicenseLoader.cancel(true);
        }
    }

    private WebView mWebView;
    private ProgressBar mIndeterminateProgress;

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View content = LayoutInflater.from(getActivity()).inflate(R.layout.html_dialog_fragment, null);
        mWebView = (WebView) content.findViewById(R.id.html_dialog_fragment_web_view);
        mIndeterminateProgress = (ProgressBar) content.findViewById(R.id.html_dialog_fragment_indeterminate_progress);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle arguments = getArguments();
        // if argument for title is given (string or int referencing a string resource) set the title
        if (arguments.getString(ARG_TITLE) != null) {
            builder.setTitle(arguments.getString(ARG_TITLE));
        } else {
            builder.setTitle(getArguments().getInt(ARG_TITLE)); //TODO error handling
        }
        builder.setView(content);
        return builder.create();
    }

    private void loadLicenses() {
        // Load asynchronously in case of a very large file.
        mLicenseLoader = new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                InputStream rawResource = getActivity().getResources().openRawResource(getArguments().getInt(ARG_RES_HTML_FILE)); //TODO error handling
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(rawResource));

                String line;
                StringBuilder sb = new StringBuilder();

                try {
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                        sb.append("\n");
                    }
                    bufferedReader.close();
                } catch (IOException e) {
                    // TODO You may want to include some logging here.
                }

                return sb.toString();
            }

            @Override
            protected void onPostExecute(String licensesBody) {
                super.onPostExecute(licensesBody);
                if (getActivity() == null || isCancelled()) {
                    return;
                }
                mIndeterminateProgress.setVisibility(View.INVISIBLE);
                mWebView.setVisibility(View.VISIBLE);
                mWebView.loadDataWithBaseURL(null, licensesBody, "text/html", "utf-8", null);
                mLicenseLoader = null;
            }

        }.execute();
    }
}
