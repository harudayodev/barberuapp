package com.example.barberuapplication;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.widget.AppCompatImageView;

public class ZoomableImageView extends AppCompatImageView {

    private Matrix matrix = new Matrix();
    private Matrix baseMatrix = new Matrix();
    private float scale = 1f;
    private float minZoom = 1f;
    private float maxZoom = 3f;

    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;

    // For dragging
    private PointF lastTouch = new PointF();
    private int mode = NONE;

    // Modes
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;

    // Double-tap target zoom
    private static final float DOUBLE_TAP_ZOOM = 1.8f;

    private boolean isInitialized = false;



    public ZoomableImageView(Context context) {
        super(context);
        init(context);
    }

    public void resetView() {
        if (getDrawable() == null) return;

        float viewWidth = getWidth();
        float viewHeight = getHeight();
        float drawableWidth = getDrawable().getIntrinsicWidth();
        float drawableHeight = getDrawable().getIntrinsicHeight();

        if (drawableWidth <= 0 || drawableHeight <= 0 || viewWidth <= 0 || viewHeight <= 0) return;

        // Compute the target "fit to screen" scale
        float scaleX = viewWidth / drawableWidth;
        float scaleY = viewHeight / drawableHeight;
        float targetScale = Math.min(scaleX, scaleY);

        float dx = (viewWidth - drawableWidth * targetScale) / 2f;
        float dy = (viewHeight - drawableHeight * targetScale) / 2f;

        // Create the target matrix
        Matrix targetMatrix = new Matrix();
        targetMatrix.setScale(targetScale, targetScale);
        targetMatrix.postTranslate(dx, dy);

        // Animate to this matrix, not baseMatrix
        animateToMatrix(targetMatrix, targetScale);
    }

    public ZoomableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void animateToMatrix(Matrix targetMatrix, float targetScale) {
        float[] startValues = new float[9];
        float[] endValues = new float[9];
        matrix.getValues(startValues);
        targetMatrix.getValues(endValues);

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(400);
        animator.setInterpolator(new DecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            float[] currentValues = new float[9];

            for (int i = 0; i < 9; i++) {
                currentValues[i] = startValues[i] + fraction * (endValues[i] - startValues[i]);
            }

            Matrix animatedMatrix = new Matrix();
            animatedMatrix.setValues(currentValues);
            setImageMatrix(animatedMatrix);

            scale = currentValues[Matrix.MSCALE_X];
        });

        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                // ✅ lock the final state into matrix
                matrix.set(targetMatrix);
                setImageMatrix(matrix);
                scale = targetScale;
            }
        });

        animator.start();
    }

    private void init(Context context) {
        setScaleType(ScaleType.MATRIX);
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        // Gesture detector for double-tap
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                float targetScale;

                if (scale < (minZoom * 1.6f)) {
                    targetScale = Math.min(scale * 2.1f, maxZoom);
                } else {
                    targetScale = minZoom;
                }

                float focusX = e.getX();
                float focusY = e.getY();

                animateZoom(scale, targetScale, focusX, focusY);
                return true;
            }
        });
    }



    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        fitImageToView();
    }


    private void fitImageToView() {
        if (getDrawable() == null || isInitialized) return;

        float viewWidth = getWidth();
        float viewHeight = getHeight();
        float drawableWidth = getDrawable().getIntrinsicWidth();
        float drawableHeight = getDrawable().getIntrinsicHeight();

        if (drawableWidth <= 0 || drawableHeight <= 0 || viewWidth <= 0 || viewHeight <= 0) return;

        // Compute fit scale
        float scaleX = viewWidth / drawableWidth;
        float scaleY = viewHeight / drawableHeight;
        minZoom = Math.min(scaleX, scaleY);

        // Center the image
        float dx = (viewWidth - drawableWidth * minZoom) / 2f;
        float dy = (viewHeight - drawableHeight * minZoom) / 2f;

        matrix.setScale(minZoom, minZoom);
        matrix.postTranslate(dx, dy);

        baseMatrix.set(matrix);   // ✅ save base state
        setImageMatrix(matrix);

        scale = minZoom;
        isInitialized = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);

        gestureDetector.onTouchEvent(event);
        scaleDetector.onTouchEvent(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastTouch.set(event.getX(), event.getY());
                mode = DRAG;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    float dx = event.getX() - lastTouch.x;
                    float dy = event.getY() - lastTouch.y;
                    matrix.postTranslate(dx, dy);
                    fixTranslation();
                    setImageMatrix(matrix);
                    lastTouch.set(event.getX(), event.getY());
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mode = ZOOM;
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
        }

        return true;
    }



    private void fixTranslation() {
        RectF bounds = getMatrixRect();
        float deltaX = 0, deltaY = 0;

        if (bounds.width() > getWidth()) {
            if (bounds.left > 0) deltaX = -bounds.left;
            if (bounds.right < getWidth()) deltaX = getWidth() - bounds.right;
        } else {
            deltaX = getWidth() / 2f - (bounds.left + bounds.width() / 2f);
        }

        if (bounds.height() > getHeight()) {
            if (bounds.top > 0) deltaY = -bounds.top;
            if (bounds.bottom < getHeight()) deltaY = getHeight() - bounds.bottom;
        } else {
            deltaY = getHeight() / 2f - (bounds.top + bounds.height() / 2f);
        }

        matrix.postTranslate(deltaX, deltaY);
    }

    private RectF getMatrixRect() {
        RectF rect = new RectF();
        if (getDrawable() != null) {
            rect.set(0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
            matrix.mapRect(rect);
        }
        return rect;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            float newScale = scale * scaleFactor;

            // Clamp zoom
            if (newScale < minZoom) scaleFactor = minZoom / scale;
            if (newScale > maxZoom) scaleFactor = maxZoom / scale;

            matrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            fixTranslation();
            setImageMatrix(matrix);

            scale *= scaleFactor;
            return true;
        }
    }

    private void animateZoom(float startScale, float endScale, float focusX, float focusY) {
        ValueAnimator animator = ValueAnimator.ofFloat(startScale, endScale);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            float currentScale = (float) animation.getAnimatedValue();
            float scaleFactor = currentScale / scale;
            matrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
            fixTranslation();
            setImageMatrix(matrix);
            scale = currentScale;
        });

        animator.start();
    }

    /**
     * Smoothly pans and zooms the image to a specific point on the image.
     * Keeps the image locked in place (no reset/recenter).
     */
    public void panTo(PointF targetPoint) {
        if (getDrawable() == null || !isInitialized) return;

        float[] startValues = new float[9];
        matrix.getValues(startValues);

        float viewWidth = getWidth();
        float viewHeight = getHeight();

        // Target zoom (slightly zoomed in but not max)
        float targetZoom = Math.min(maxZoom * 0.1f + minZoom, maxZoom);

        // Compute target scale and translation
        Matrix targetMatrix = new Matrix();
        targetMatrix.setScale(targetZoom, targetZoom);

        float scaledX = targetPoint.x * targetZoom;
        float scaledY = targetPoint.y * targetZoom;
        float dx = viewWidth / 2f - scaledX;
        float dy = viewHeight / 2f - scaledY;
        targetMatrix.postTranslate(dx, dy);

        float[] endValues = new float[9];
        targetMatrix.getValues(endValues);

        // Animate smoothly from current matrix -> target matrix
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(600);
        animator.setInterpolator(new DecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            float[] currentValues = new float[9];
            for (int i = 0; i < 9; i++) {
                currentValues[i] = startValues[i] + fraction * (endValues[i] - startValues[i]);
            }

            Matrix animatedMatrix = new Matrix();
            animatedMatrix.setValues(currentValues);
            setImageMatrix(animatedMatrix);

            // Only update scale here (not the base matrix yet)
            scale = currentValues[Matrix.MSCALE_X];
        });

        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                // When animation finishes, set final matrix
                matrix.set(targetMatrix);
                setImageMatrix(matrix);
                scale = targetZoom;
            }
        });

        animator.start();
    }
}