package com.sundowner.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sundowner.R;

public class ContentView extends RelativeLayout {

    public interface Delegate {
        public void onContentViewSingleTap(int position);
        public void onContentViewDoubleTap(int position);
        public void onContentViewLongTap(int position);
    }

    private static final String TAG = "ContentView";
    private static final int STRIKETHROUGH_DELAY = 2000;
    private GestureDetector gestureDetector;
    private Delegate delegate;
    private int position; // index of content data in list
    private boolean hasURL;
    private TextView text;
    private TextView author;
    private View urlIndicatorTab;

    public ContentView(Context context, int position, Delegate delegate) {

        super(context);
        this.position = position;
        this.delegate = delegate;
        View.inflate(context, R.layout.view_content, this);
        gestureDetector = new GestureDetector(context, new GestureListener());
        setLongClickable(true);

        // more efficient when reusing class for new content
        text = (TextView)findViewById(R.id.text);
        author = (TextView)findViewById(R.id.author);
        urlIndicatorTab = findViewById(R.id.urlIndicatorTab);
    }

    public void setContent(String text, String author, String url) {

        hasURL = url != null;
        Resources res = getResources();
        if (res == null) {
            Log.e(TAG, "Failed to get resources.");
            return;
        }

        this.text.setText(text);

        // the author is only show if the tag was created by a friend of the user or the user
        // themselves.
        if (author == null) {
            this.author.setVisibility(View.GONE);
        } else {
            this.author.setText(author);
            this.author.setVisibility(View.VISIBLE);
        }

        // if the tag is linked to a URL then visually indicate this in the view
        urlIndicatorTab.setVisibility(url == null ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public void onLongPress(MotionEvent event) {
            beginVoteDownAnimation();
            delegate.onContentViewLongTap(position);
        }

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            beginVoteUpAnimation();
            delegate.onContentViewDoubleTap(position);
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            if (hasURL) {
                delegate.onContentViewSingleTap(position);
            }
            return true;
        }

        private void beginVoteDownAnimation() {
            Resources res = getResources();
            if (res == null) {
                Log.e(TAG, "Failed to get resources.");
                return;
            }
            TextView v = (TextView)findViewById(R.id.text);
            v.setPaintFlags(v.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            v.setTextColor(res.getColor(R.color.background_text));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Resources res = getResources();
                    TextView v = (TextView)findViewById(R.id.text);
                    v.setTextColor(res.getColor(R.color.text));
                    v.setPaintFlags(v.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                }
            }, STRIKETHROUGH_DELAY);
        }

        private void beginVoteUpAnimation() {
            Context ctx = getContext();
            if (ctx == null) {
                Log.e(TAG, "Failed to get context.");
                return;
            }
            View v = findViewById(R.id.likeContent);
            Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.content_like);
            if (animation == null) {
                Log.e(TAG, "Failed to get animation resource.");
                return;
            }
            v.startAnimation(animation);
        }
    }

}
