package com.sundowner.api;

import org.json.JSONObject;

// An exception that is raised by the server in response to a request. The data field contains the
// JSON response from the server detailing the error (or null if non is available).
public class ServerException extends Exception {

    public final JSONObject data;

    public ServerException(JSONObject data) {
        this.data = data;
    }

}
