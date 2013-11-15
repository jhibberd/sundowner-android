package com.sundowner.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.sundowner.R;

// implementation of Facebook login flow as per doc:
// https://developers.facebook.com/docs/android/login-with-facebook/
public class FBLoginFragment extends Fragment {

    public static interface OnSessionOpenListener {
        public abstract void onSessionOpen(String user);
    }

    private static final String TAG = "FBLoginFragment";

    public static void closeSession() {
        Session.getActiveSession().closeAndClearTokenInformation();
    }

    private UiLifecycleHelper uiHelper;
    private OnSessionOpenListener listener;
    private boolean loggedInViewVisible = false;

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_fb_login, container, false);
        if (view == null) {
            Log.e(TAG, "Failed to inflate view");
            return null;
        }
        LoginButton authButton = (LoginButton) view.findViewById(R.id.loginButton);
        authButton.setFragment(this);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnSessionOpenListener)activity;
        } catch (final ClassCastException e) {
            Log.e(TAG, "Attached to activity that doesn't implement required interface");
        }
    }

    public void onLoggedInViewClose() {
        loggedInViewVisible = false;
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {

        if (state.isOpened()) {

            // If a Facebook session opens but the logged in activity is already visible then
            // start the activity again. A session can appear to be opened twice as a result of
            // transitioning between this app and the Facebook app during the auth process.
            if (loggedInViewVisible) {
                return;
            }
            loggedInViewVisible = true;

            // Using the Facebook access token, call the Facebook Graph API to get the name of
            // the user. Store the name in memory until the Facebook session is closed.
            Request.newMeRequest(session, new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    listener.onSessionOpen(user.getName());
                }
            }).executeAsync();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // For scenarios where the main activity is launched and user
        // session is not null, the session state change notification
        // may not be triggered. Trigger it if it's open/closed.
        Session session = Session.getActiveSession();
        if (session != null &&
                (session.isOpened() || session.isClosed()) ) {
            onSessionStateChange(session, session.getState(), null);
        }

        uiHelper.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }
}
