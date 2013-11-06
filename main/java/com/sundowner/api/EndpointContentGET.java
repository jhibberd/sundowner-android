package com.sundowner.api;

import android.net.Uri;

import org.json.JSONObject;

public class EndpointContentGET extends JSONEndpoint {

    public interface Delegate {
        public void onEndpointContentGETResponse(JSONObject data);
    }

    private final double longitude;
    private final double latitude;
    private final Delegate delegate;

    public EndpointContentGET(double longitude, double latitude, Delegate delegate) {

        super(HTTPMethod.GET);

        // TODO for testing instagram
        // longitude = 101.714834785;
        // latitude = 3.098744131;

        // TODO for testing sandbox
        longitude = 1;
        latitude = 1;

        this.longitude = longitude;
        this.latitude = latitude;
        this.delegate = delegate;
    }

    @Override
    protected void buildURI(Uri.Builder uriBuilder) {
        uriBuilder.path("/content");
        uriBuilder.appendQueryParameter("lng", String.valueOf(longitude));
        uriBuilder.appendQueryParameter("lat", String.valueOf(latitude));
    }

    @Override
    protected void onResponseReceived(JSONObject data) {
        delegate.onEndpointContentGETResponse(data);
    }
}
