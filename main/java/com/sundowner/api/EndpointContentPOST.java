package com.sundowner.api;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class EndpointContentPOST extends JSONEndpoint {

    public interface Delegate {
        public void onServerContentPOSTResponse(JSONObject payload);
        public void onServerError(JSONObject payload);
    }

    private static final String TAG = "EndpointContentPOST";
    private final double longitude;
    private final double latitude;
    private final float accuracy;
    private final String text;
    private final String url;
    private final String accessToken;
    private final Delegate delegate;

    public EndpointContentPOST(
            Context ctx, double longitude, double latitude, float accuracy, String text,
            String url, String accessToken, Delegate delegate) {

        super(ctx, HTTPMethod.POST);
        this.longitude = longitude;
        this.latitude = latitude;
        this.accuracy = accuracy;
        this.text = text;
        this.url = url;
        this.accessToken = accessToken;
        this.delegate = delegate;
    }

    @Override
    protected void buildURI(Uri.Builder uriBuilder) {
        uriBuilder.path("/content");
    }

    @Override
    protected JSONObject getRequestBody() {
        try {
            JSONObject object = new JSONObject();
            object.put("lng", longitude);
            object.put("lat", latitude);
            object.put("accuracy", accuracy);
            object.put("text", text);
            if (url != null) {
                object.put("url", url);
            }
            object.put("access_token", accessToken);
            return object;
        } catch (JSONException e) {
            Log.d(TAG, "Failed to create request body object");
            return null;
        }
    }

    @Override
    protected void onResponseSuccess(JSONObject payload) {
        delegate.onServerContentPOSTResponse(payload);
    }

    @Override
    protected void onResponseError(JSONObject payload) {
        Log.e(TAG, "Error response");
        delegate.onServerError(payload);
    }
}
