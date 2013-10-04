package com.example.geotag;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ObjectArrayAdapter extends ArrayAdapter<JSONObject> {

    private static final String TAG = "ObjectArrayAdapter";
    private final Context context;
    private final ArrayList<JSONObject> values;

    public ObjectArrayAdapter(Context context, ArrayList<JSONObject> values) {
        super(context, R.layout.list_item_object, values);
        this.context = context;
        this.values = values;
    }

    public boolean isEnabled(int position) {
        return false;
    }

    /* ViewHolder pattern for smoother scrolling
     * http://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
     */
    private static class ViewHolder {
        TextView title;
        TextView detail;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // reuse views for save memory
        ViewHolder holder;
        View view;
        if (convertView == null) {
            view = new ContentView(context);
            holder = new ViewHolder();
            holder.title = (TextView) view.findViewById(R.id.title);
            holder.detail = (TextView) view.findViewById(R.id.detail);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            view = convertView;
        }

        // populate view values
        JSONObject object;
        try {
            object = values.get(position);

            String username = object.getString("username");
            String titleText = object.getString("text");

            // set UI element values
            holder.title.setText(titleText);
            //holder.detail.setText(username);
            holder.detail.setText("boom 3");

        } catch (JSONException e) {
            Log.d(TAG, "Badly formed JSON from server");
        }

        return view;
    }
}
