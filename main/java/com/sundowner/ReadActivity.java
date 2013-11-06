package com.sundowner;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.sundowner.api.EndpointContentGET;
import com.sundowner.api.EndpointVotesPOST;
import com.sundowner.util.ContentArrayAdapter;
import com.sundowner.util.LocalNativeAccountData;
import com.sundowner.util.LocationService;
import com.sundowner.view.ContentView;
import com.sundowner.view.FBLoginFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ReadActivity extends ListActivity implements
        EndpointContentGET.Delegate, ContentView.Delegate, ServiceConnection,
        LocationService.Delegate {

    private static final String TAG = "ReadActivity";
    private ArrayList<JSONObject> objects;
    private ContentArrayAdapter adapter;
    private boolean isLocationServiceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        // hide icon and title from the action bar
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);

        // bind objects array, adapter and list view
        objects = new ArrayList<JSONObject>();
        adapter = new ContentArrayAdapter(this, objects, this);
        setListAdapter(adapter);
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

        // reassign the adapter to clear the view cache, otherwise causes a bug whereby the cell
        // background rectangle disappears temporarily
        setListAdapter(adapter);

        adapter.notifyDataSetChanged();
        Log.i(TAG, "Updated displayed content");
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

            JSONObject content = objects.get(position);
            String contentId = content.getString("id");
            String userId = LocalNativeAccountData.load(this).userId;

            new EndpointVotesPOST(contentId, userId, EndpointVotesPOST.Vote.UP).call();

        } catch (JSONException e) {
            Log.e(TAG, "Failed to up vote content as local content is badly formed");
        }
    }

    public void onContentViewLongTap(int position) {
        // notify the server that the content has been voted down
        try {

            JSONObject content = objects.get(position);
            String contentId = content.getString("id");
            String userId = LocalNativeAccountData.load(this).userId;

            new EndpointVotesPOST(contentId, userId, EndpointVotesPOST.Vote.DOWN).call();

        } catch (JSONException e) {
            Log.e(TAG, "Failed to down vote content as local content is badly formed");
        }
    }

    private void composeObject() {
        Intent intent = new Intent(this, ComposeActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.read, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_compose:
                composeObject();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // called when connected to the LocationService
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        LocationService.LocationServiceBinder locationService =
                (LocationService.LocationServiceBinder) service;
        isLocationServiceBound = true;
        locationService.setDelegate(this);

        // ask the location service to flush through it's current location instead of waiting for
        // the next triggered location update notification
        locationService.flushLocation();
    }

    // called when disconnected from the LocationService
    @Override
    public void onServiceDisconnected(ComponentName name) {
        isLocationServiceBound = false;
    }

    @Override
    public void onLocationUpdate(Location location) {
        Log.i(TAG, "Received location update");
        // use the current location to asynchronously request nearby objects from the server
        new EndpointContentGET(location.getLongitude(), location.getLatitude(), this).call();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        FBLoginFragment.closeSession(this);
        setResult(RESULT_OK);
        finish();
    }
}
