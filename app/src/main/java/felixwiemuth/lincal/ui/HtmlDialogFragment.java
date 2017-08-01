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
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import felixwiemuth.lincal.R;

//If you don't support Android 2.x, you should use the non-support version!

/**
 * Created by Adam Speakman on 24/09/13. http://speakman.net.nz
 * <p>
 * Edited by Felix Wiemuth 12/2016, 07/2017.
 */

/**
 * Displays an HTML document in a dialog fragment with the possibility to add special action links
 * that can trigger execution of app code ({@see Action}).
 */
public class HtmlDialogFragment extends DialogFragment {

    /**
     * An action to be performed when a "action:///action-name/arg1/arg2/..." link is clicked.
     */
    public interface Action {

        /**
         * Get the name of the action to be used in the URI.
         *
         * @return
         */
        String getName();

        /**
         * Perform an action.
         *
         * @param args    the path segments after the action name (arg1, arg2, ...), i.e.,
         *                everything between the separators "/" which themselves are not included,
         *                contains only non-empty arguments
         * @param context the current context of the WebView
         */
        void run(List<String> args, Context context);
    }

    private AsyncTask<Void, Void, String> loader;

    private Map<String, Action> actions = new HashMap<>();

    private static final String FRAGMENT_TAG = "nz.net.speakman.androidlicensespage.HtmlDialogFragment";
    private static final String ARG_TITLE = "felixwiemuth.lincal.ARG_TITLE";
    private static final String ARG_RES_HTML_FILE = "felixwiemuth.lincal.ARG_RES_HTML_FILE";
    private static final String ARG_ACTIONS = "felixwiemuth.lincal.ARG_ACTIONS";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        for (String actionName : getArguments().getStringArray(ARG_ACTIONS)) {
            try {
                Class<? extends Action> actionClass = (Class<? extends Action>) Class.forName(actionName);
                Action action = actionClass.newInstance();
                actions.put(action.getName(), action);
            } catch (java.lang.InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Builds and displays a HTML dialog fragment.
     *
     * @param fm          a fragment manager instance used to display this HtmlDialogFragment
     * @param resTitle    the title for the dialog, as string resource
     * @param resHtmlFile the resource of the HTML file to display
     * @param actions     {@link Action}s that should be registered with the WebView to be shown
     */
    public static void displayHtmlDialogFragment(FragmentManager fm, @StringRes int resTitle, @RawRes int resHtmlFile, Class<? extends Action>... actions) {
        Bundle arguments = new Bundle();
        arguments.putInt(ARG_TITLE, resTitle);
        arguments.putInt(ARG_RES_HTML_FILE, resHtmlFile);
        addActionsToBundle(arguments, actions);
        constructFragment(arguments).displayFragment(fm);
    }

    /**
     * Builds and displays a HTML dialog fragment.
     *
     * @param fm          a fragment manager instance used to display this HtmlDialogFragment
     * @param title       the title for the dialog, as string
     * @param resHtmlFile the resource of the HTML file to display
     * @param actions     {@link Action}s that should be registered with the WebView to be shown
     */
    public static void displayHtmlDialogFragment(FragmentManager fm, String title, @RawRes int resHtmlFile, Class<? extends Action>... actions) {
        Bundle arguments = new Bundle();
        arguments.putString(ARG_TITLE, title);
        arguments.putInt(ARG_RES_HTML_FILE, resHtmlFile);
        addActionsToBundle(arguments, actions);
        constructFragment(arguments).displayFragment(fm);
    }

    private static void addActionsToBundle(Bundle bundle, Class<? extends Action>[] actions) {
        String[] actionNames = new String[actions.length];
        for (int i = 0; i < actionNames.length; i++) {
            actionNames[i] = actions[i].getCanonicalName();
        }
        bundle.putStringArray(ARG_ACTIONS, actionNames);
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
        loadPage();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (loader != null) {
            loader.cancel(true);
        }
    }

    private WebView webView;
    private ProgressBar indeterminateProgress;

    private void loadPage() {
        // Load asynchronously in case of a very large file.
        loader = new AsyncTask<Void, Void, String>() {

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
            protected void onPostExecute(String body) {
                super.onPostExecute(body);
                if (getActivity() == null || isCancelled()) {
                    return;
                }
                indeterminateProgress.setVisibility(View.INVISIBLE);
                webView.setVisibility(View.VISIBLE);
                webView.loadDataWithBaseURL(null, body, "text/html", "utf-8", null);
                loader = null;
            }

        }.execute();
    }

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View content = LayoutInflater.from(getActivity()).inflate(R.layout.html_dialog_fragment, null);
        webView = (WebView) content.findViewById(R.id.html_dialog_fragment_web_view);
        // Set the WebViewClient (in API <24 have to parse URI manually)
        if (Build.VERSION.SDK_INT >= 24) {
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
                    Uri uri = webResourceRequest.getUrl(); // @TargetApi(Build.VERSION_CODES.N_MR1)
                    return HtmlDialogFragment.this.loadUrl(webView, uri);
                }
            });
        } else { //TODO test on an API < 24 device
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                    Uri uri = Uri.parse(url);
                    return HtmlDialogFragment.this.loadUrl(webView, uri);
                }
            });
        }

        indeterminateProgress = (ProgressBar) content.findViewById(R.id.html_dialog_fragment_indeterminate_progress);

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

    private boolean loadUrl(WebView webView, Uri uri) {
        if (uri.getScheme().equals("file")) {
            webView.loadUrl(uri.toString());
        } else if (uri.getScheme().equals("action")) {
            List<String> segments = uri.getPathSegments();
            if (segments.isEmpty()) {
                throw new RuntimeException("Error in WebView: No action name provided.");
            } else {
                handleAction(segments.get(0), segments.subList(1, segments.size()));
            }
        } else {
            // If the URI is not pointing to a local file, open with an ACTION_VIEW Intent
            webView.getContext().startActivity(new Intent(Intent.ACTION_VIEW, uri));
        }
        return true; // in both cases we handle the link manually
    }

    private void handleAction(String action, List<String> args) {
        Action a = actions.get(action);
        if (a == null) {
            throw new RuntimeException("Error in WebView: no action \"" + action + "\" registered.");
        } else {
            a.run(args, getContext());
        }
    }
}
