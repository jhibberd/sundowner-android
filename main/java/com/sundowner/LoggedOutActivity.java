package com.sundowner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

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
