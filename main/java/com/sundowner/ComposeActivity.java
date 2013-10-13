package com.sundowner;

import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.app.Activity;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.sundowner.api.EndpointContentPOST;
import com.sundowner.util.LocationService;
import com.sundowner.view.ComposeView;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class ComposeActivity extends Activity implements
    EndpointContentPOST.Delegate, ServiceConnection {

    private static final String TAG = "ComposeActivity";
    private boolean isAcceptActionEnabled = true;
    private boolean isLocationServiceBound = false;
    private LocationService.LocationServiceBinder locationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        // hide icon and title from the action bar
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // bind to the LocationService
        Intent intent = new Intent(this, LocationService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // unbind from the LocationService is it's bound
        if (isLocationServiceBound) {
            unbindService(this);
            isLocationServiceBound = false;
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

        setAcceptActionEnabled(false);

        // get the current device location
        Location currentLocation = locationService.getCurrentLocation();
        if (currentLocation == null) {
            Context ctx = getApplication();
            if (ctx == null) {
                Log.e(TAG, "Failed to get application context");
                return;
            }
            Toast.makeText(
                ctx,
                "Oops, can't get your current location. Try again in a few seconds.",
                Toast.LENGTH_SHORT).show();
            setAcceptActionEnabled(true);
            return;
        }

        // read the user ID from the preferences
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultUsername = getResources().getString(R.string.preference_username_default);
        String username = sharedPrefs.getString("username", defaultUsername);

        ComposeView composeView = (ComposeView)findViewById(R.id.compose_view);
        Map<String, String> parsedText = composeView.getParsedText();
        if (parsedText == null) {
            Log.e(TAG, "ComposeView returned null text");
            return;
        }

        // prevent the user from making a post with no text
        String text = parsedText.get("text");
        if (text.length() == 0) {
            Log.i(TAG, "Prevented empty content from being posted");
            setAcceptActionEnabled(true);
            return;
        }

        new EndpointContentPOST(
            currentLocation.getLongitude(), currentLocation.getLatitude(),
            currentLocation.getAccuracy(), text, parsedText.get("url"), username, this).call();
    }

    @Override
    public void onEndpointContentPOSTResponse(JSONObject data) {
        try {
            int statusCode = data.getJSONObject("meta").getInt("code");
            if (statusCode != HttpStatus.SC_CREATED) {
                throw new PostFailedException();
            }
            Context ctx = getApplication();
            if (ctx == null) {
                Log.e(TAG, "Failed to get application context");
                return;
            }
            Toast.makeText(ctx, "Posted", Toast.LENGTH_SHORT).show();
            finish();

        } catch (PostFailedException e) {
            Log.e(TAG, "Server returned failed status code");
            onPostFailed();
        } catch (JSONException e) {
            Log.e(TAG, "Server returned bad meta field");
            onPostFailed();
        }
    }

    private void onPostFailed() {
        Context ctx = getApplication();
        if (ctx == null) {
            Log.e(TAG, "Failed to get application context");
            return;
        }
        Toast.makeText(ctx, "Oops, posting failed", Toast.LENGTH_SHORT).show();
        setAcceptActionEnabled(true); // so the user can try to submit again
    }

    // disable the accept action bar menu item to prevent the user from pressing it repeatedly
    // while a submission is in progress and submitting multiple copies of the same object
    private void setAcceptActionEnabled(boolean enabled) {
        isAcceptActionEnabled = enabled;
        invalidateOptionsMenu();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        locationService = (LocationService.LocationServiceBinder)service;
        isLocationServiceBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        isLocationServiceBound = false;
    }

    private class PostFailedException extends Exception {}
}
