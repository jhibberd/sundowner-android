package com.sundowner.api;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

public class EndpointVotesPOST extends JSONEndpoint {

    public enum Vote {
        DOWN(0),
        UP(1);

        private final int value;

        private Vote(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private static final String TAG = "EndpointVotesPOST";
    private final String contentId;
    private final String accessToken;
    private final Vote vote;

    public EndpointVotesPOST(Context ctx, String contentId, String accessToken, Vote vote) {
        super(ctx, HTTPMethod.POST);
        this.contentId = contentId;
        this.accessToken = accessToken;
        this.vote = vote;
    }

    @Override
    protected void buildURI(Uri.Builder uriBuilder) {
        uriBuilder.path("/votes");
    }

    @Override
    protected JSONObject getRequestBody() {
        try {
            JSONObject object = new JSONObject();
            object.put("content_id", contentId);
            object.put("access_token", accessToken);
            object.put("vote", vote.getValue());
            return object;
        } catch (JSONException e) {
            Log.d(TAG, "Failed to create request body object");
            return null;
        }
    }

    @Override
    protected void onResponseReceived(JSONObject data) {
        // this call is fire and forget, but log if there's an error response
        try {
            int status = data.getJSONObject("meta").getInt("code");
            if (status != HttpStatus.SC_OK && status != HttpStatus.SC_CREATED) {
                Log.e(TAG, "Error response following vote: " + data.toString());
            }
        } catch (JSONException e) {
            Log.e(TAG, "Bad response following vote");
        }
    }
}
