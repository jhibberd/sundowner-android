package com.example.geotag;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.geotag.view.ContentView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ObjectArrayAdapter extends ArrayAdapter<JSONObject> {

    private static final String TAG = "ObjectArrayAdapter";
    private final Context context;
    private final ArrayList<JSONObject> data;
    private ContentView.Delegate contentViewDelegate;

    public ObjectArrayAdapter(
            Context context,
            ArrayList<JSONObject> data,
            ContentView.Delegate contentViewDelegate) {

        super(context, R.layout.view_content, data);
        this.context = context;
        this.data = data;
        this.contentViewDelegate = contentViewDelegate;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ContentView view = convertView == null ?
            new ContentView(context, position, contentViewDelegate) : (ContentView)convertView;

        try {
            JSONObject content = data.get(position);
            String author = content.getString("username");
            String text = content.getString("text");
            String url = content.isNull("url") ? null : content.getString("url");
            view.setContent(text, author, url);

        } catch (JSONException e) {
            Log.d(TAG, "Badly formed JSON from server");
        }

        return view;
    }
}
