package com.sundowner.api;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

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

    public interface Delegate {
        public void onServerVotePOSTResponse(JSONObject payload);
        public void onServerError(JSONObject payload);
    }

    private static final String TAG = "EndpointVotesPOST";
    private final String contentId;
    private final String accessToken;
    private final Vote vote;
    private final Delegate delegate;

    public EndpointVotesPOST(
            Context ctx, String contentId, String accessToken, Vote vote, Delegate delegate) {
        super(ctx, HTTPMethod.POST);
        this.contentId = contentId;
        this.accessToken = accessToken;
        this.vote = vote;
        this.delegate = delegate;
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
    protected void onResponseSuccess(JSONObject payload) {
        delegate.onServerVotePOSTResponse(payload);
    }

    @Override
    protected void onResponseError(JSONObject payload) {
        Log.e(TAG, "Error response");
        delegate.onServerError(payload);
    }
}
