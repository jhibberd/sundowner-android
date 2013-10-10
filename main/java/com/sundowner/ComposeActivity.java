package com.sundowner;

import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.internal.ca;
import com.sundowner.api.EndpointContentPOST;
import com.sundowner.view.ComposeView;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ComposeActivity extends Activity implements
    LocationAgent.Delegate, EndpointContentPOST.Delegate {

    private static final String TAG = "ComposeActivity";
    private boolean isAcceptActionEnabled = true;

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

        // Disable the accept action bar menu item to prevent the user from pressing it repeatedly
        // while a submission is in progress and submitting multiple copies of the same object.
        isAcceptActionEnabled = false;
        invalidateOptionsMenu();

        // Asynchronously request the current device location. This begins a chain of asynchronous
        // requests that ends with the listview being updated with new objects.
        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        new LocationAgent().getCurrentLocation(locationManager, this);

    }

    @Override
    public void onObtainedCurrentLocation(Location location) {

        // read the user ID from the preferences
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultUsername = getResources().getString(R.string.preference_username_default);
        String username = sharedPrefs.getString("username", defaultUsername);

        ComposeView composeView = (ComposeView)findViewById(R.id.compose_view);
        String text = composeView.getText();
        if (text == null) {
            Log.e(TAG, "ComposeView returned null text");
            return;
        }

        Map<String, String> parsedText = parseText(text);

        new EndpointContentPOST(
            location.getLongitude(), location.getLatitude(), location.getAccuracy(),
            parsedText.get("text"), parsedText.get("url"), username, this).call();
    }

    // attempt to extract a URL from the content text
    // http://stackoverflow.com/questions/285619/how-to-detect-the-presence-of-url-in-a-string
    private Map<String, String> parseText(String originalText) {

        StringBuilder text = new StringBuilder();
        String url = null;

        String[] words = originalText.split("\\s");
        for (String word : words) {

            boolean wordIsURL = false;
            if (url == null) {
                try {
                    new URL(word);
                    url = word;
                    wordIsURL = true;
                } catch (MalformedURLException e) {
                    // word is not URL
                }
            }

            if (!wordIsURL) {
                if (text.length() > 0) {
                    text.append(" ");
                }
                text.append(word);
            }
        }

        HashMap<String, String> result = new HashMap<String, String>();
        result.put("text", text.toString());
        result.put("url", url);
        return result;
    }

    @Override
    public void onEndpointContentPOSTResponse(JSONObject data) {
        Log.i(TAG, data.toString());
        // TODO check response
        if (true) {
            Toast.makeText(getApplicationContext(), "Posted", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(getApplicationContext(), "Posting failed", Toast.LENGTH_SHORT).show();
        }
    }
}
