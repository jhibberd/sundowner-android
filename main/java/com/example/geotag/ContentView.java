package com.example.geotag;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

public class ContentView extends RelativeLayout {

    private GestureDetector gestureDetector;

    public ContentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        View.inflate(context, R.layout.list_item_object, this);

        gestureDetector = new GestureDetector(context, new GestureListener());
        setLongClickable(true);
    }

    public ContentView(Context context) {
        this(context, null, 0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final String DEBUG_TAG = "Gestures";

        @Override
        public void onLongPress(MotionEvent event) {
            Log.d(DEBUG_TAG, "onLongPress: " + event.toString());
        }

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            Log.d(DEBUG_TAG, "onDoubleTap: " + event.toString());

            // TODO proof on concept but this object should accept a delegate which is notified
            // of these gesture events
            View v = (View)findViewById(R.id.likeContent);
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.content_like);
            v.startAnimation(animation);

            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            Log.d(DEBUG_TAG, "onSingleTapConfirmed: " + event.toString());
            return true;
        }
    }

}
