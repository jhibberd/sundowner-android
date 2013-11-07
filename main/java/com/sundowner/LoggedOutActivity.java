package com.sundowner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.facebook.Session;
import com.facebook.SessionState;
import com.sundowner.view.FBLoginFragment;

public class LoggedOutActivity extends FragmentActivity implements
        FBLoginFragment.OnSessionOpenListener {

    private final int START_LOGGED_IN_ACTIVITY_REQUEST_CODE = 1;
    private FBLoginFragment fbLoginFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_out);
        fbLoginFragment =
            (FBLoginFragment) getSupportFragmentManager().findFragmentById(R.id.fbLoginFragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SessionState ss = Session.getActiveSession().getState();
        switch (ss) {

            // activity started and user doesn't have an FB session
            case CREATED:
                break;

            // activity started and user does have an FB session
            case CREATED_TOKEN_LOADED:
                getSupportFragmentManager().beginTransaction().hide(fbLoginFragment).commit();
                break;

            // activity resume after the user completed the FB auth flow
            case OPENED:
                getSupportFragmentManager().beginTransaction().hide(fbLoginFragment).commit();
                break;

            // user has logged out of their FB session and returned to the logged out activity
            case CLOSED:
                getSupportFragmentManager().beginTransaction().show(fbLoginFragment).commit();
                break;
        }
    }

    @Override
    public void onSessionOpen() {
        // the Facebook LoginView has notified the class that a Facebook session has been opened
        // so start the logged in activity
        Intent loggedInActivity = new Intent(this, ReadActivity.class);
        startActivityForResult(loggedInActivity, START_LOGGED_IN_ACTIVITY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == START_LOGGED_IN_ACTIVITY_REQUEST_CODE) {
            fbLoginFragment.onLoggedInViewClose();
        }
    }
}
