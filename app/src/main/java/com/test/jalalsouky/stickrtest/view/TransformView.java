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
 * Created by jalalsouky on 01/11/14.
 */
public class TransformView extends ImageView {

    private Drawable mDrawable;

    private Camera mCamera;
    private Matrix mMatrix;

    private static final int INVALID_POINTER_ID = -1;
    private int mActivePointerId = INVALID_POINTER_ID;

    private float mLastTouchX, mLastTouchY;
    private float mPosX, mPosY;

    private float mRotationDegrees;

    private float mRotationXAxis;

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

    private static final float MAX_SCALE_FACTOR = 5.f;
    private static final float MIN_SCALE_FACTOR = .25f;

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
        mCamera.rotateX(95);
        mCamera.getMatrix(mMatrix);

        canvas.save();
        canvas.translate(mPosX, mPosY);
        canvas.scale(mScaleFactor, mScaleFactor, centerX, centerY);
        canvas.rotate(mRotationDegrees, centerX, centerY);
        canvas.concat(mMatrix);
        mDrawable.draw(canvas);
        canvas.restore();

        mCamera.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mScaleDetector.onTouchEvent(ev);

        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN: {

                mActivePointerId = ev.getPointerId(0);

                final float x = ev.getX(mActivePointerId);
                final float y = ev.getY(mActivePointerId);

                mLastTouchX = x;
                mLastTouchY = y;

                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);

                if (!mScaleDetector.isInProgress()) {

                    final float dx = x - mLastTouchX;
                    final float dy = y - mLastTouchY;

                    mPosX += dx;
                    mPosY += dy;

                    invalidate();
                }

                rotate(ev);

                mLastTouchX = x;
                mLastTouchY = y;

                break;
            }

            case MotionEvent.ACTION_UP: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
            }
        }

        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(MIN_SCALE_FACTOR, Math.min(mScaleFactor, MAX_SCALE_FACTOR));

            invalidate();
            return true;
        }
    }

    private void rotate(MotionEvent ev) {

        if(ev.getPointerCount() > 1) {
            double delta_x = (ev.getX(0) - ev.getX(1));
            double delta_y = (ev.getY(0) - ev.getY(1));
            double radians = Math.atan2(delta_y, delta_x);

            mRotationDegrees = (float) Math.toDegrees(radians);

            invalidate();
        }
    }
}