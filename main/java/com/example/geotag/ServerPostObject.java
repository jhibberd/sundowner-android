package com.example.geotag;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerPostObject {

    public interface Delegate {
        public void onObjectPostedToServer(boolean didSucceed);
    }

    private static final String TAG = "ServerPostObject";
    private Delegate delegate;

    public void postObjectToServer(
            Location currentLocation, String title, String username, Delegate delegate) {

        this.delegate = delegate;

        /* Construct the payload (the object definition).
         *
         * According to the documentation all Location objects returned by LocationManager contain
         * an accuracy.
         */
        JSONObject object = new JSONObject();
        try {
            object.put("longitude", currentLocation.getLongitude());
            object.put("latitude", currentLocation.getLatitude());
            object.put("accuracy", currentLocation.getAccuracy());
            object.put("title", title);
            object.put("username", username);
        } catch (JSONException e) {
            Log.d(TAG, "Failed to create object definition");
            return;
        }

        new JSONHTTPRequest().execute(object);
    }

    private class JSONHTTPRequest extends AsyncTask<JSONObject, Void, Boolean> {

        private static final String ENDPOINT = "http://199.101.48.101:8050";

        @Override
        protected Boolean doInBackground(JSONObject... objects) {
            try {
                return get(objects[0]);
            } catch (JSONException e) {
                Log.d(TAG, "Server returned badly formed JSON");
            } catch (IOException e) {
                Log.d(TAG, "IO Error during request");
            }
            return Boolean.FALSE;
        }

        @Override
        protected void onPostExecute(Boolean didSucceed) {
            delegate.onObjectPostedToServer(didSucceed);
        }

        private Boolean get(JSONObject object) throws IOException, JSONException {

            // TODO this could do with some extra work
            URL url = new URL(ENDPOINT);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.connect();

            // write the request body (the object definition)
            String body = object.toString();
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(body);
            os.flush();
            os.close();

            int response = conn.getResponseCode();
            return response == HttpStatus.SC_OK;

        }
    }
}
