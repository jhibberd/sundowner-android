package com.sundowner.view;

import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.sundowner.R;

public class ComposeView extends LinearLayout {

    private static final String TAG = "ComposeView";

    public ComposeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.view_compose, this);
    }

    public String getText() {
        EditText editText = (EditText)findViewById(R.id.text);
        Editable editable = editText.getText();
        if (editable == null) {
            Log.e(TAG, "Couldn't get handle to EditText control");
            return null;
        }
        return editable.toString();
    }
}
