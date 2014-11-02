package com.test.jalalsouky.stickrtest.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by jalalsouky on 01/11/14.
 */
public class TransformationPointView extends ImageView {

    private final static int INVALID_POINTER_ID = -1;

    private int mActivePointerId;

    private float mLastTouchX, mLastTouchY;

    private onTransformationPointListener mPointListener;

    public float dX, dY;
    private float mInitX, mInitY;

    private int mType = -1;

    public static class PointType {
        public static final int TOP_LEFT = 0;
        public static final int TOP_RIGHT = 1;
        public static final int BOTTOM_LEFT = 2;
        public static final int BOTTOM_RIGHT = 3;
    }

    public TransformationPointView(Context context) {
        this(context, null, 0);
    }

    public TransformationPointView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TransformationPointView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.setX(getX() + dX);
        this.setY(getY() + dY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();

                mLastTouchX = x;
                mLastTouchY = y;
                mActivePointerId = ev.getPointerId(0);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);

                final float dx = x - mLastTouchX;
                final float dy = y - mLastTouchY;

                this.dX += dx;
                this.dY += dy;

                mPointListener.onMove(this);

                invalidate();

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

    public void setType(int type) {
        mType = type;
    }

    public int getType() {
        return mType;
    }

    public void setTransformationView(TransformView view) {
        mPointListener = (onTransformationPointListener)view;
        view.addPoint(this);
    }

    public void setInitPosition(float x, float y) {
        mInitX = x;
        mInitY = y;

        invalidate();
    }

    public interface onTransformationPointListener {
        public void onMove(TransformationPointView point);
    }
}