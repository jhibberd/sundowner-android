package com.sundowner.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.util.AttributeSet;
import android.view.View;

import com.sundowner.R;


public class LikeContentView extends View {

    private ShapeDrawable drawable = null;

    public LikeContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = this.getWidth();
        int h = this.getHeight();

        if (drawable == null) {

            // heart shape
            Path path = new Path();
            path.moveTo(0.5f*w, 0.158f*h);
            path.cubicTo(0.5f*w,    0.126f*h,   0.455f*w,   0,          0.273f*w,   0);
            path.cubicTo(0,         0,          0,          0.395f*h,   0,          0.395f*h);
            path.cubicTo(0,         0.579f*h,   0.182f*w,   0.811f*h,   0.5f*w,     h);
            path.cubicTo(0.818f*w,  0.811f*h,   w,          0.579f*h,   w,          0.395f*h);
            path.cubicTo(w,         0.395f*h,   w,          0,          0.727f*w,   0);
            path.cubicTo(0.591f*w,  0,          0.5f*w,     0.126f*h,   0.5f*w,     0.158f*h);

            PathShape shape = new PathShape(path, w, h);
            drawable = new ShapeDrawable(shape);
            drawable.getPaint().setColor(getResources().getColor(R.color.like));
            drawable.setBounds(0, 0, w, h);
        }

        drawable.draw(canvas);
    }

}
