package com.sundowner;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.sundowner.api.EndpointContentGET;
import com.sundowner.api.EndpointVotesPOST;
import com.sundowner.view.ContentView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends ListActivity implements
        LocationAgent.Delegate, EndpointContentGET.Delegate, ContentView.Delegate {

    private static final String TAG = "MainActivity";
    ArrayList<JSONObject> objects;
    ObjectArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // hide icon and title from the action bar
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);

        // set default preferences (will not override user preferences)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // bind objects array, adapter and list view
        objects = new ArrayList<JSONObject>();
        adapter = new ObjectArrayAdapter(this, objects, this);
        setListAdapter(adapter);

        refreshObjects();
    }

    @Override
    public void onObtainedCurrentLocation(Location location) {

        // use the current location to asynchronously request nearby objects from the server
        new EndpointContentGET(location.getLongitude(), location.getLatitude(), this).call();
    }

    public void onEndpointContentGETResponse(JSONObject data) {

        // extract objects from the response data and convert to ArrayList object for compatibility
        // with the adapter
        objects.clear();
        try {
            JSONArray values = data.getJSONArray("data");
            int numObjects = values.length();
            for (int i = 0; i < numObjects; i++)
                objects.add(values.getJSONObject(i));
        } catch (JSONException e) {
            Log.d(TAG, "Badly formed JSON in server response: " + data.toString());
            return;
        }

        adapter.notifyDataSetChanged();

        // debug notification
        Toast.makeText(getApplicationContext(), "Refreshed", Toast.LENGTH_SHORT).show();
    }

    private void refreshObjects() {

        // Asynchronously request the current device location. This begins a chain of asynchronous
        // requests that ends with the listview being updated with new objects.
        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        new LocationAgent().getCurrentLocation(locationManager, this);
    }

    public void onContentViewSingleTap(int position) {
        // open the URL associated with the content in the browser
        try {

            JSONObject content = objects.get(position);
            String url = content.getString("url");
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));

        } catch (JSONException e) {
            Log.e(TAG, "Failed to view content as local content is badly formed");
        }
    }

    public void onContentViewDoubleTap(int position) {
        // notify the server that the content has been voted up
        try {

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            String defaultUsername = getResources().getString(R.string.preference_username_default);
            String userId = sharedPrefs.getString("username", defaultUsername);

            JSONObject content = objects.get(position);
            String contentId = content.getString("id");

            new EndpointVotesPOST(contentId, userId, EndpointVotesPOST.Vote.UP).call();

        } catch (JSONException e) {
            Log.e(TAG, "Failed to up vote content as local content is badly formed");
        }
    }

    public void onContentViewLongTap(int position) {
        // notify the server that the content has been voted down
        try {

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            String defaultUsername = getResources().getString(R.string.preference_username_default);
            String userId = sharedPrefs.getString("username", defaultUsername);

            JSONObject content = objects.get(position);
            String contentId = content.getString("id");

            new EndpointVotesPOST(contentId, userId, EndpointVotesPOST.Vote.DOWN).call();

        } catch (JSONException e) {
            Log.e(TAG, "Failed to down vote content as local content is badly formed");
        }
    }

    private void composeObject() {
        Intent intent = new Intent(this, ComposeActivity.class);
        startActivity(intent);
    }

    private void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refreshObjects();
                return true;
            case R.id.action_compose:
                composeObject();
                return true;
            case R.id.action_settings:
                showSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
