package com.sundowner.api;

import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class EndpointUserIdPOST extends JSONEndpoint {

    public interface Delegate {
        public void onEndpointUserIdPOSTResponse(JSONObject data);
    }

    private static final String TAG = "EndpointUserIdPOST";
    private final String accessToken; // for Facebook Graph API
    private final Delegate delegate;

    public EndpointUserIdPOST(String accessToken, Delegate delegate) {
        super(HTTPMethod.POST);
        this.accessToken = accessToken;
        this.delegate = delegate;
    }

    @Override
    protected void buildURI(Uri.Builder uriBuilder) {
        uriBuilder.path("/users");
    }

    @Override
    protected JSONObject getRequestBody() {
        try {
            JSONObject object = new JSONObject();
            object.put("access_token", accessToken);
            return object;
        } catch (JSONException e) {
            Log.d(TAG, "Failed to create request body object");
            return null;
        }
    }

    @Override
    protected void onResponseReceived(JSONObject data) {
        delegate.onEndpointUserIdPOSTResponse(data);
    }
}
