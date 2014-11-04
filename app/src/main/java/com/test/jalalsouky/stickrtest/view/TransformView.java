package com.test.jalalsouky.stickrtest.view;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

/**
 * Custom ImageView that responds to translation, scaling,
 * x axis rotation and 2D rotation gestures.
 */
public class TransformView extends ImageView {

    private static final int INVALID_POINTER_ID = -1;

    private static final float MAX_SCALE_FACTOR = 5.f;
    private static final float MIN_SCALE_FACTOR = .25f;

    private static final int MAX_X_AXIS_ROTATION = 180;
    private static final int MIN_X_AXIS_ROTATION = -180;

    private static final int ROTATE_SLOW_FACTOR = 3;

    private static final int POINTER_SPAN_THRESHOLD = 300;

    private int mFirstPointerId = INVALID_POINTER_ID;
    private int mSecondPointerId = INVALID_POINTER_ID;

    private Drawable mDrawable;

    private ScaleGestureDetector mScaleDetector;

    private Camera mCamera;
    private Matrix mMatrix;

    private float mLastTouchX, mLastTouchY;
    private float mLastTouchX_, mLastTouchY_;

    private float mTransX, mTransY;
    private float mRotationDegrees;
    private float mXAxisRotation;
    private float mYAxisRotation;

    private float mScaleFactor = 1.f;

    private boolean isInitRotation = true;
    private float mInitDegrees;

    public TransformView(Context context) {
        this(context, null, 0);
    }

    public TransformView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TransformView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public void setImageDrawable(Drawable drawable) {
        mDrawable = drawable;
        mDrawable.setBounds(0, 0, mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mCamera = new Camera();
        mMatrix = new Matrix();

        float centerX = mDrawable.getBounds().centerX();
        float centerY = mDrawable.getBounds().centerY();

        mCamera.save();
        mCamera.rotateX(mXAxisRotation);
        mCamera.rotateY(mYAxisRotation);
        mCamera.getMatrix(mMatrix);
        mCamera.restore();

        canvas.save();
        canvas.translate(mTransX, mTransY);
        canvas.scale(mScaleFactor, mScaleFactor, centerX, centerY);
        canvas.rotate(mRotationDegrees, centerX, centerY);
        canvas.concat(mMatrix);

        mDrawable.draw(canvas);

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mScaleDetector.onTouchEvent(ev);

        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN: {

                final float x = ev.getX();
                final float y = ev.getY();

                mLastTouchX = x;
                mLastTouchY = y;

                mFirstPointerId = ev.getPointerId(0);

                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int firstIndex = ev.findPointerIndex(mFirstPointerId);
                final float x = ev.getX(firstIndex);
                final float y = ev.getY(firstIndex);

                final float dx = x - mLastTouchX;
                final float dy = y - mLastTouchY;

                mLastTouchX = x;
                mLastTouchY = y;

                if(ev.getPointerCount() == 1) {
                    translate(dx, dy);
                }

                final int secondIndex = ev.findPointerIndex(mSecondPointerId);
                if(secondIndex == INVALID_POINTER_ID) {
                    break;
                }

                if(mScaleDetector.getCurrentSpanY() <= POINTER_SPAN_THRESHOLD) {
                    if(Math.abs(dy) > Math.abs(dx)) {
                        rotateXAxis(dy);
                    }
                    else {
                        rotateYAxis(dx);
                    }
                }
                else {

                    rotate(ev);
                }

                break;
            }

            case MotionEvent.ACTION_UP: {
                mFirstPointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mFirstPointerId = INVALID_POINTER_ID;
                mSecondPointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mFirstPointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                    mFirstPointerId = ev.getPointerId(newPointerIndex);
                }
                else if(pointerId == mSecondPointerId) {
                    mSecondPointerId = INVALID_POINTER_ID;
                }
                break;
            }

            case MotionEvent.ACTION_POINTER_DOWN:

                isInitRotation = true;
                mSecondPointerId = ev.getPointerId(1);

                final float x = ev.getX(1);
                final float y = ev.getY(1);

                mLastTouchX = x;
                mLastTouchY = y;

                break;
        }

        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            if(detector.getCurrentSpanY() > POINTER_SPAN_THRESHOLD) {
                mScaleFactor *= detector.getScaleFactor();
                mScaleFactor = Math.max(MIN_SCALE_FACTOR, Math.min(mScaleFactor, MAX_SCALE_FACTOR));
                invalidate();
            }

            return true;
        }
    }

    private void rotate(MotionEvent ev) {

        if(ev.getPointerCount() < 2) {
            return;
        }

        double delta_x = (ev.getX(0) - ev.getX(1));
        double delta_y = (ev.getY(0) - ev.getY(1));
        double radians = Math.atan2(delta_y, delta_x);

        if(isInitRotation) {
            isInitRotation = false;
            mInitDegrees = (float) Math.toDegrees(radians);
            return;
        }

        mRotationDegrees = (float) Math.toDegrees(radians) - mInitDegrees;
        invalidate();
    }

    private void translate(float dx, float dy) {
        mTransX += dx;
        mTransY += dy;
        invalidate();
    }

    private void rotateXAxis(float dy) {
        mXAxisRotation -= dy / ROTATE_SLOW_FACTOR;
        mXAxisRotation = Math.max(MIN_X_AXIS_ROTATION,
                Math.min(mXAxisRotation, MAX_X_AXIS_ROTATION));
        invalidate();
    }

    private void rotateYAxis(float dx) {
        mYAxisRotation += dx / ROTATE_SLOW_FACTOR;
        mYAxisRotation = Math.max(MIN_X_AXIS_ROTATION,
                Math.min(mYAxisRotation, MAX_X_AXIS_ROTATION));
        invalidate();
    }
}