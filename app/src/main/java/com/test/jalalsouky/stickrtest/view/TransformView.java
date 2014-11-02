package com.test.jalalsouky.stickrtest.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jalalsouky on 01/11/14.
 */
public class TransformView extends ImageView implements TransformationPointView.onTransformationPointListener {

    private static final int INVALID_POINTER_ID = -1;

    private Bitmap mBitmap;
    private Drawable mDrawable;

    private int mActivePointerId;
    private float mLastTouchX, mLastTouchY;

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

    //Translation coordinates
    private float mX, mY;

    //Transformation coordinates
    private float mX1, mY1; //Top left
    private float mX2, mY2; //Top right
    private float mX3, mY3; //Bottom left
    private float mX4, mY4; //Bottom right

    //Initial coordinates
    private float mInitX1, mInitY1,
                  mInitX2, mInitY2,
                  mInitX3, mInitY3,
                  mInitX4, mInitY4;

    private int mState = STATE.UNKNOWN;

    private List<TransformationPointView> mPoints = new ArrayList<TransformationPointView>();

    public class STATE {
        public static final int UNKNOWN = 0;
        public static final int MOVE = 1;
        public static final int SCALE = 2;
        public static final int ROTATE = 3;
    }

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
        mBitmap = ((BitmapDrawable)drawable).getBitmap();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(mBitmap == null) {
            return;
        }

        canvas.save();

        int bmW = mBitmap.getWidth() * 3; //Bitmap is too small. Just making it three times bigger
        int bmH = mBitmap.getHeight() * 3; //Same
        int cW = getWidth();
        int cH = getHeight();

        mInitX1 = (cW - bmW) / 2;
        mInitY1 = (cH - bmH) / 2;
        mInitX2 = (cW + bmW) / 2;
        mInitY2 = (cH - bmH) / 2;
        mInitX3 = (cW - bmW) / 2;
        mInitY3 = (cH + bmH) / 2;
        mInitX4 = (cW + bmW) / 2;
        mInitY4 = (cH + bmH) / 2;

        float[] vertices = {
                (mInitX1 + mX1 + mX), (mInitY1 + mY1 + mY),
                (mInitX2 + mX2 + mX), (mInitY2 + mY2 + mY),
                (mInitX3 + mX3 + mX), (mInitY3 + mY3 + mY),
                (mInitX4 + mX4 + mX), (mInitY4 + mY4 + mY)
        };

        canvas.drawBitmapMesh(mBitmap, 1, 1, vertices, 0, null, 0, null);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        mScaleDetector.onTouchEvent(ev);

        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {

                mState = STATE.UNKNOWN;

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

                if (!mScaleDetector.isInProgress()) {

                    mState = STATE.MOVE;

                    final float dx = x - mLastTouchX;
                    final float dy = y - mLastTouchY;

                    mX += dx;
                    mY += dy;

                    invalidate();
                }

                mLastTouchX = x;
                mLastTouchY = y;

                break;
            }

            case MotionEvent.ACTION_UP: {
                mState = STATE.UNKNOWN;
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mState = STATE.UNKNOWN;
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                mState = STATE.UNKNOWN;
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

            mState = STATE.SCALE;

            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

            invalidate();
            return true;
        }
    }

    @Override
    public void onMove(TransformationPointView point) {

        float dX = point.dX;
        float dY = point.dY;

        switch(point.getType()) {
            case TransformationPointView.PointType.TOP_LEFT:
                mX1 += dX;
                mY1 += dY;
                break;
            case TransformationPointView.PointType.TOP_RIGHT:
                mX2 += dX;
                mY2 += dY;
                break;
            case TransformationPointView.PointType.BOTTOM_LEFT:
                mX3 += dX;
                mY3 += dY;
                break;
            case TransformationPointView.PointType.BOTTOM_RIGHT:
                mX4 += dX;
                mY4 += dY;
                break;
            default:
                return;
        }

        invalidate();
    }

    public void addPoint(TransformationPointView point) {
        mPoints.add(point);
    }
}