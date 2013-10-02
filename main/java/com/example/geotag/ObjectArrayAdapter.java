package com.example.geotag;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ObjectArrayAdapter extends ArrayAdapter<JSONObject> {

    private static final String TAG = "ObjectArrayAdapter";
    private final Context context;
    private final ArrayList<JSONObject> values;
    private final DetailFormatter detailFormatter;

    public ObjectArrayAdapter(Context context, ArrayList<JSONObject> values) {
        super(context, R.layout.list_item_object, values);
        this.context = context;
        this.values = values;
        detailFormatter = new DetailFormatter();
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
            LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_object, parent, false);
            holder = new ViewHolder();
            holder.title = (TextView) view.findViewById(R.id.title);
            holder.detail = (TextView) view.findViewById(R.id.detail);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            view = convertView;
        }

        // populate view values
        JSONObject object = null;
        try {
            object = values.get(position);

            double distance = object.getDouble("distance");
            String username = object.getString("username");
            String titleText = object.getString("title");
            long created = object.getLong("created");

            String detailText = detailFormatter.formatDetail(distance, created, username);

            // set UI element values
            holder.title.setText(titleText);
            holder.detail.setText(detailText);

        } catch (JSONException e) {
            Log.d(TAG, "Badly formed JSON from server");
        }

        return view;
    }
}
