package com.sundowner;

import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.app.Activity;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.Session;
import com.sundowner.api.EndpointContentPOST;
import com.sundowner.util.LocationService;
import com.sundowner.view.ComposeView;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class ComposeActivity extends Activity implements
    EndpointContentPOST.Delegate, ServiceConnection {

    public static final String ACTIVITY_EXTRA_USER = "USER";
    public static final int RESULT_BAD_ACCESS_TOKEN = 1;
    private static final String TAG = "ComposeActivity";
    private boolean isAcceptActionEnabled = true;
    private boolean isLocationServiceBound = false;
    private LocationService.LocationServiceBinder locationService;
    private ComposeView composeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        String user = getIntent().getStringExtra(ACTIVITY_EXTRA_USER);
        composeView = (ComposeView)findViewById(R.id.compose_view);
        composeView.setAuthor(user);

        // hide icon and title from the action bar
        ActionBar ab = getActionBar();
        if (ab == null) {
            Log.e(TAG, "Failed to get action bar.");
            return;
        }
        ab.setDisplayShowHomeEnabled(false);
        ab.setDisplayShowTitleEnabled(false);
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

        String accessToken = Session.getActiveSession().getAccessToken();

        new EndpointContentPOST(
            this, currentLocation.getLongitude(), currentLocation.getLatitude(),
            currentLocation.getAccuracy(), text, parsedText.get("url"), accessToken, this).call();
    }

    @Override
    public void onServerContentPOSTResponse(JSONObject payload) {
        try {
            int statusCode = payload.getJSONObject("meta").getInt("code");
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

    @Override
    public void onServerError(JSONObject payload) {

        int errorCode;
        try {
            errorCode = payload.getJSONObject("meta").getInt("code");
        } catch (JSONException e) {
            Log.e(TAG, "Badly formed error message");
            return;
        }

        switch (errorCode) {

            // if the error response indicates a bad access token then finish the activity with
            // a status code that indicates this
            case 100:
                setResult(RESULT_BAD_ACCESS_TOKEN);
                finish();
                break;

            default:
                Log.e(TAG, "Unknown server error");
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
