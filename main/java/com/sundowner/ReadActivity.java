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

import com.facebook.Session;
import com.sundowner.api.EndpointContentGET;
import com.sundowner.api.EndpointVotesPOST;
import com.sundowner.util.ContentArrayAdapter;
import com.sundowner.util.LocationService;
import com.sundowner.view.ContentView;
import com.sundowner.view.FBLoginFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ReadActivity extends ListActivity implements
        EndpointContentGET.Delegate, EndpointVotesPOST.Delegate, ContentView.Delegate,
        ServiceConnection, LocationService.Delegate {

    public static final String ACTIVITY_EXTRA_USER = "USER";
    public static final int RESULT_BAD_ACCESS_TOKEN = 1;
    private static final String TAG = "ReadActivity";
    private static final int REQUEST_CODE_DEFAULT = 1;
    private ArrayList<JSONObject> objects;
    private ContentArrayAdapter adapter;
    private String user;
    private boolean isLocationServiceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        user = getIntent().getStringExtra(ACTIVITY_EXTRA_USER);

        // hide icon and title from the action bar
        ActionBar ab = getActionBar();
        if (ab == null) {
            Log.e(TAG, "Failed to get action bar.");
            return;
        }
        ab.setDisplayShowHomeEnabled(false);
        ab.setDisplayShowTitleEnabled(false);

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

    @Override
    public void onServerContentGETResponse(JSONObject payload) {

        // extract objects from the response data and convert to ArrayList object for compatibility
        // with the adapter
        objects.clear();
        try {
            JSONArray values = payload.getJSONArray("data");
            int numObjects = values.length();
            for (int i = 0; i < numObjects; i++)
                objects.add(values.getJSONObject(i));
        } catch (JSONException e) {
            Log.d(TAG, "Badly formed JSON in server response: " + payload.toString());
            return;
        }

        // reassign the adapter to clear the view cache, otherwise causes a bug whereby the cell
        // background rectangle disappears temporarily
        setListAdapter(adapter);

        adapter.notifyDataSetChanged();
        Log.i(TAG, "Updated displayed content");
    }

    @Override
    public void onServerVotePOSTResponse(JSONObject payload) {
        // no need to take any action following a successful vote
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
                onBadAccessToken();
                break;

            default:
                Log.e(TAG, "Unknown server error");
        }
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
            String accessToken = Session.getActiveSession().getAccessToken();
            new EndpointVotesPOST(
                this, contentId, accessToken, EndpointVotesPOST.Vote.UP, this).call();

        } catch (JSONException e) {
            Log.e(TAG, "Failed to up vote content as local content is badly formed");
        }
    }

    public void onContentViewLongTap(int position) {
        // notify the server that the content has been voted down
        try {
            JSONObject content = objects.get(position);
            String contentId = content.getString("id");
            String accessToken = Session.getActiveSession().getAccessToken();
            new EndpointVotesPOST(
                this, contentId, accessToken, EndpointVotesPOST.Vote.DOWN, this).call();

        } catch (JSONException e) {
            Log.e(TAG, "Failed to down vote content as local content is badly formed");
        }
    }

    private void composeObject() {
        Intent intent = new Intent(this, ComposeActivity.class);
        intent.putExtra(ComposeActivity.ACTIVITY_EXTRA_USER, user);
        startActivityForResult(intent, REQUEST_CODE_DEFAULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // if the compose activity closed because of a bad access token, close this activity too
        // and propagate the error message up the activity stack
        if (requestCode == REQUEST_CODE_DEFAULT) {
            if (resultCode == ComposeActivity.RESULT_BAD_ACCESS_TOKEN) {
                onBadAccessToken();
            }
        }
    }

    private void onBadAccessToken() {
        FBLoginFragment.closeSession();
        setResult(RESULT_BAD_ACCESS_TOKEN);
        finish();
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

        String accessToken = Session.getActiveSession().getAccessToken();

        new EndpointContentGET(
            this, location.getLongitude(), location.getLatitude(), accessToken, this).call();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        FBLoginFragment.closeSession();
        setResult(RESULT_OK);
        finish();
    }
}
