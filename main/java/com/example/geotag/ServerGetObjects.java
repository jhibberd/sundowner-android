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

        // TODO testing
        longitude = 101.714834785;
        latitude = 3.098744131;

        String urlSpec = String.format(
            "http://199.101.48.101:8051/content?lng=%f&lat=%f&user_id=cccccccccccccccccccccccc", longitude, latitude);

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

        @Override
        protected JSONObject doInBackground(URL... urls) {
            try {
                return get(urls[0]);
            } catch (ServerException e) {
                Log.e(TAG, "Error response from server: " + e.data.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject data) {
            delegate.onObjectsObtainedFromServer(data);
        }

        private JSONObject get(URL url) throws ServerException {
            InputStream is;
            HttpURLConnection conn = null;
            try {

                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();

                is = conn.getInputStream();
                String response = readInputStream(is);
                return jsonDecode(response);

            } catch (IOException input_e) {
                // attempt to read the error response from the server (if there is once)
                JSONObject errorResponse = null;
                if (conn != null) {
                    is = conn.getErrorStream();
                    if (is != null) {
                        try {
                            String data = readInputStream(is);
                            errorResponse = jsonDecode(data);
                        } catch (IOException error_e) {
                            Log.e(TAG, "Failed to read from error stream.");
                        }
                    }
                }
                throw new ServerException(errorResponse);
            }
        }

        private final int BYTE_ARRAY_BUFFER_SIZE = 50; /* initial buffer capacity */
        private final int BUFFER_SIZE = 512;
        private final String TEXT_ENCODING = "UTF-8";

        // Read the contents of an input stream to a String object.
        // Based on:
        // http://stackoverflow.com/questions/2793168/reading-httpurlconnection-inputstream-manual-buffer-or-bufferedinputstream
        private String readInputStream(InputStream is) throws IOException {
            try {
                BufferedInputStream bis = new BufferedInputStream(is);
                ByteArrayBuffer bab = new ByteArrayBuffer(BYTE_ARRAY_BUFFER_SIZE);
                int bytesRead;
                byte[] buffer = new byte[BUFFER_SIZE];
                while (true) {
                    bytesRead = bis.read(buffer);
                    if (bytesRead == -1)
                        break;
                    bab.append(buffer, 0, bytesRead);
                }
                return new String(bab.toByteArray(), TEXT_ENCODING);
            } finally {
                // according to the docs any resources associated with the resource are also
                // released, which would suggest there's no need to explicitly close the
                // HttpURLConnection object:
                // http://developer.android.com/reference/java/io/BufferedInputStream.html#close()
                is.close();
            }
        }

        // All communication to and from the server is encoded as JSON.
        private JSONObject jsonDecode(String data) {
            try {
                return new JSONObject(data);
            } catch (JSONException e) {
                Log.e(TAG, "Server response isn't valid JSON: " + data);
                return null;
            }
        }
    }
}
