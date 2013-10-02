package com.example.geotag;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/* Abstracts logic for obtaining nearby objects; based on:
 * http://developer.android.com/training/basics/network-ops/connecting.html
 */
public class ServerGetObjects {

    public interface Delegate {
        public void onObjectsObtainedFromServer(JSONObject data);
    }

    private static final String TAG = "ServerGetObjects";
    private Delegate delegate;

    public void getServerObjects(Location currentLocation, Delegate delegate) {
        this.delegate = delegate;

        double longitude = currentLocation.getLongitude();
        double latitude = currentLocation.getLatitude();
        String urlSpec = String.format(
            "http://199.101.48.101:8051/?longitude=%f&latitude=%f&user_id=aaaaaaaaaaaaaaaaaaaaaaaa", longitude, latitude);

        try {
            URL url = new URL(urlSpec);
            new JSONHTTPRequest().execute(url);

        } catch (MalformedURLException e) {
            // for now just notify the delegate that no objects were obtained
            Log.d(TAG, "Badly formed URL");
            this.delegate.onObjectsObtainedFromServer(null);
        }
    }

    private class JSONHTTPRequest extends AsyncTask<URL, Void, JSONObject> {

        private final int BYTE_ARRAY_BUFFER_SIZE = 50; /* initial buffer capacity */
        private final int BUFFER_SIZE = 512;
        private final String TEXT_ENCODING = "UTF-8";

        @Override
        protected JSONObject doInBackground(URL... urls) {
            try {
                return get(urls[0]);
            } catch (JSONException e) {
                Log.d(TAG, "Server returned badly formed JSON");
                return null;
            } catch (IOException e) {
                Log.d(TAG, "IO Error during request");
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject data) {
            delegate.onObjectsObtainedFromServer(data);
        }

        private JSONObject get(URL url) throws IOException, JSONException {
            InputStream is = null;
            try {

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();

                // TODO check the response code and handle errors
                //int response = conn.getResponseCode();

                // read input stream to string; based on
                // http://stackoverflow.com/questions/2793168/reading-httpurlconnection-inputstream-manual-buffer-or-bufferedinputstream
                is = conn.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                ByteArrayBuffer bab = new ByteArrayBuffer(BYTE_ARRAY_BUFFER_SIZE);
                int bytesRead = 0;
                byte[] buffer = new byte[BUFFER_SIZE];
                while (true) {
                    bytesRead = bis.read(buffer);
                    if (bytesRead == -1)
                        break;
                    bab.append(buffer, 0, bytesRead);
                }
                String content = new String(bab.toByteArray(), TEXT_ENCODING);
                return new JSONObject(content);

            } finally {
                if (is != null) {
                    /* according to the docs any resources associated with the resource are also
                     * released, which would suggest there's no need to explicitly close the
                     * HttpURLConnection object:
                     * http://developer.android.com/reference/java/io/BufferedInputStream.html#close()
                     */
                    is.close();
                }
            }
        }
    }
}
