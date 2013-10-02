package com.example.geotag;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.os.Build;
import android.widget.EditText;
import android.widget.Toast;

public class ComposeActivity extends Activity implements
    LocationAgent.Delegate, ServerPostObject.Delegate {

    private boolean isAcceptActionEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
        setupActionBar();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.compose, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_accept:
                submitPost();
                return true;
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem item = menu.findItem(R.id.action_accept);
        if (item != null) {
            item.setEnabled(isAcceptActionEnabled);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void submitPost() {

        /* Disable the accept action bar menu item to prevent the user from pressing it repeatedly
         * while a submission is in progress and submitting multiple copies of the same object.
         */
        isAcceptActionEnabled = false;
        invalidateOptionsMenu();

        /* Asynchronously request the current device location. This begins a chain of asynchronous
         * requests that ends with the listview being updated with new objects.
         */
        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        new LocationAgent().getCurrentLocation(locationManager, this);

    }

    @Override
    public void onObtainedCurrentLocation(Location location) {

        // read the username from the preferences
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultUsername = getResources().getString(R.string.preference_username_default);
        String username = sharedPrefs.getString("username", defaultUsername);

        // get object title entered by the user
        // TODO is this really the best way to handle NullPointerExceptions in Java?
        String title = null;
        EditText editText = (EditText) findViewById(R.id.object_title);
        if (editText != null) {
            Editable text = editText.getText();
            if (text != null) {
                title = text.toString();
            }
        }

        new ServerPostObject().postObjectToServer(location, title, username, this);
    }

    @Override
    public void onObjectPostedToServer(boolean didSucceed) {
        if (didSucceed) {
            Toast.makeText(getApplicationContext(), "Posted", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(getApplicationContext(), "Posting failed", Toast.LENGTH_SHORT).show();
        }
    }
}
