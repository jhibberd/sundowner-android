package com.example.geotag;

import android.content.Context;
import android.graphics.Paint;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ContentView extends RelativeLayout {

    public interface Delegate {
        public void onContentViewSingleTap(int position);
        public void onContentViewDoubleTap(int position);
        public void onContentViewLongTap(int position);
    }

    private static final int STRIKETHROUGH_DELAY = 2000;
    private GestureDetector gestureDetector;
    private Delegate delegate;
    private int position; // index of content data in list

    public ContentView(Context context, int position, Delegate delegate) {
        super(context, null, 0);
        this.position = position;
        this.delegate = delegate;
        View.inflate(context, R.layout.list_item_object, this);
        gestureDetector = new GestureDetector(context, new GestureListener());
        setLongClickable(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public void onLongPress(MotionEvent event) {

            // being vote down animation
            TextView v = (TextView)findViewById(R.id.text);
            v.setPaintFlags(v.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            v.setTextColor(getResources().getColor(R.color.background_text));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    TextView v = (TextView)findViewById(R.id.text);
                    v.setTextColor(getResources().getColor(R.color.text));
                    v.setPaintFlags(v.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                }
            }, STRIKETHROUGH_DELAY);

            delegate.onContentViewLongTap(position);
        }

        @Override
        public boolean onDoubleTap(MotionEvent event) {

            // begin vote up animation
            View v = findViewById(R.id.likeContent);
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.content_like);
            v.startAnimation(animation);

            delegate.onContentViewDoubleTap(position);
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            delegate.onContentViewSingleTap(position);
            return true;
        }
    }

}
