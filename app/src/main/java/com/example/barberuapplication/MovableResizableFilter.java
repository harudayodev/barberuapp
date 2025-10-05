package com.example.barberuapplication;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ScaleGestureDetector;

import androidx.annotation.Nullable;

public class MovableResizableFilter extends androidx.appcompat.widget.AppCompatImageView {
    private float scaleFactor = 1.0f;
    private ScaleGestureDetector scaleGestureDetector;

    public MovableResizableFilter(Context context) {
        super(context);
        init(context);
    }

    public MovableResizableFilter(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MovableResizableFilter(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // Initialize ScaleGestureDetector for pinch-to-zoom
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    public boolean onTouchEvent(android.view.MotionEvent event) {
        // Only allow scaling, no moving
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 3.0f));
            setScaleX(scaleFactor);
            setScaleY(scaleFactor);
            return true;
        }
    }
}
