package com.example.geotag.api;

import android.net.Uri;

import org.json.JSONObject;

public class EndpointContentGET extends JSONEndpoint {

    public interface Delegate {
        public void onEndpointContentGETResponse(JSONObject data);
    }

    private final double longitude;
    private final double latitude;
    private final String userId;
    private final Delegate delegate;

    public EndpointContentGET(
            double longitude, double latitude, String userId, Delegate delegate) {

        super(HTTPMethod.GET);

        // TODO for testing instagram
        // longitude = 101.714834785;
        // latitude = 3.098744131;

        // TODO for testing sandbox
        longitude = 101.6868549;
        latitude = 3.139003;

        userId = "cccccccccccccccccccccccc";

        this.longitude = longitude;
        this.latitude = latitude;
        this.userId = userId;
        this.delegate = delegate;
    }

    @Override
    protected void buildURI(Uri.Builder uriBuilder) {
        uriBuilder.path("/content");
        uriBuilder.appendQueryParameter("lng", String.valueOf(longitude));
        uriBuilder.appendQueryParameter("lat", String.valueOf(latitude));
        uriBuilder.appendQueryParameter("user_id", userId);
    }

    @Override
    protected void onResponseReceived(JSONObject data) {
        delegate.onEndpointContentGETResponse(data);
    }
}
