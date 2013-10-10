package com.sundowner.api;

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
    private final String userId;
    private final Vote vote;

    public EndpointVotesPOST(String contentId, String userId, Vote vote) {
        super(HTTPMethod.POST);

        // TODO for testing
        userId = "cccccccccccccccccccccccc";

        this.contentId = contentId;
        this.userId = userId;
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
            object.put("user_id", userId);
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
