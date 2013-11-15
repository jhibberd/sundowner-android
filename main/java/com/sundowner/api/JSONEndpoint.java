package com.sundowner.api;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class JSONEndpoint {

    protected enum HTTPMethod {
        GET,
        POST
    }

    private static final String META_DATA_SERVER_HOST = "com.sundowner.ServerHost";
    private static final String META_DATA_SERVER_PORT = "com.sundowner.ServerPort";
    private static final String TAG = "JSONEndpoint";
    private Context ctx;
    private HTTPMethod method;

    public JSONEndpoint(Context ctx, HTTPMethod method) {
        this.ctx = ctx;
        this.method = method;
    }

    public void call() {
        try {
            ApplicationInfo ai = ctx.getPackageManager().getApplicationInfo(
                ctx.getPackageName(), PackageManager.GET_META_DATA);
            String host = ai.metaData.getString(META_DATA_SERVER_HOST);
            String port = ai.metaData.getString(META_DATA_SERVER_PORT);

            // subclass to define the URI
            String uriString = String.format("http://%s:%s", host, port);
            Uri.Builder uriBuilder = Uri.parse(uriString).buildUpon();
            buildURI(uriBuilder);
            Uri uri = uriBuilder.build();

            new AsyncRequest().execute(uri);

        }
        catch (NullPointerException e) {
            Log.e(TAG, "Failed to get package manager.");
        }
        catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to read server host/port from manifest.");
        }
    }

    protected abstract void buildURI(Uri.Builder uriBuilder);
    protected abstract void onResponseReceived(JSONObject data);

    protected JSONObject getRequestBody() {
        // should be implemented by the subclass if the endpoint expects a request body
        return null;
    }

    // abstraction of logic for issuing HTTP request
    // http://developer.android.com/training/basics/network-ops/connecting.html
    protected class AsyncRequest extends AsyncTask<Uri, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Uri... uris) {
            try {
                return request(uris[0]);
            } catch (ServerException e) {
                Log.e(TAG, "Error response from server: " + e.data.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject data) {
            onResponseReceived(data);
        }

        private final int READ_TIMEOUT = 10000; // milliseconds
        private final int WRITE_TIMEOUT = 15000; // milliseconds

        private JSONObject request(Uri uri) throws ServerException {
            InputStream is;
            HttpURLConnection conn = null;
            try {

                URL url = new URL(uri.toString());
                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(WRITE_TIMEOUT);
                conn.setDoInput(true);

                switch (method) {
                    case GET:
                        conn.setRequestMethod("GET");
                        conn.connect();
                        break;

                    case POST:

                        JSONObject body = getRequestBody();
                        conn.setRequestMethod("POST");
                        if (body != null) {
                            conn.setRequestProperty("Content-Type", "application/json");
                            conn.setDoOutput(true);
                        }
                        conn.connect();

                        if (body != null) {
                            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                            os.writeBytes(body.toString());
                            os.flush();
                            os.close();
                        }
                        break;

                    default:

                }

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
