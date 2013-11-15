package com.sundowner.api;

import android.content.Context;
import android.net.Uri;

import org.json.JSONObject;

public class EndpointContentGET extends JSONEndpoint {

    public interface Delegate {
        public void onEndpointContentGETResponse(JSONObject data);
    }

    private final double longitude;
    private final double latitude;
    private final String accessToken;
    private final Delegate delegate;

    public EndpointContentGET(
            Context ctx, double longitude, double latitude, String accessToken,
            Delegate delegate) {

        super(ctx, HTTPMethod.GET);
        this.longitude = longitude;
        this.latitude = latitude;
        this.accessToken = accessToken;
        this.delegate = delegate;
    }

    @Override
    protected void buildURI(Uri.Builder uriBuilder) {
        uriBuilder.path("/content");
        uriBuilder.appendQueryParameter("lng", String.valueOf(longitude));
        uriBuilder.appendQueryParameter("lat", String.valueOf(latitude));
        uriBuilder.appendQueryParameter("access_token", accessToken);
    }

    @Override
    protected void onResponseReceived(JSONObject data) {
        delegate.onEndpointContentGETResponse(data);
    }
}
