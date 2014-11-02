package com.test.jalalsouky.stickrtest;

import android.app.Activity;
import android.os.Bundle;
import android.widget.RelativeLayout;

import com.test.jalalsouky.stickrtest.view.TransformView;
import com.test.jalalsouky.stickrtest.view.TransformationPointView;


public class MainActivity extends Activity {

    private RelativeLayout mRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRoot = (RelativeLayout) findViewById(R.id.root);

        TransformView view = (TransformView)findViewById(R.id.view);
        view.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));

        addCircle(view, TransformationPointView.PointType.TOP_LEFT);
        addCircle(view, TransformationPointView.PointType.TOP_RIGHT);
        addCircle(view, TransformationPointView.PointType.BOTTOM_LEFT);
        addCircle(view, TransformationPointView.PointType.BOTTOM_RIGHT);
    }

    /**
     * Adds a vertix control circle and relates it to view.
     * @param view The view to transform and anchor the point to.
     * @param type The point type, indicating which view vertix to transform. Must belong to the
     *             {@link com.test.jalalsouky.stickrtest.view.TransformationPointView.PointType} class
     */
    private void addCircle(TransformView view, int type) {

        TransformationPointView point = new TransformationPointView(this);

        point.setTransformationView(view);
        point.setImageDrawable(getResources().getDrawable(R.drawable.circle));
        point.setType(type);

        int size = getResources().getDimensionPixelSize(R.dimen.trans_point_circle_size);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);

        switch(type) {
            case TransformationPointView.PointType.TOP_LEFT:
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                break;
            case TransformationPointView.PointType.TOP_RIGHT:
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                break;
            case TransformationPointView.PointType.BOTTOM_LEFT:
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                break;
            case TransformationPointView.PointType.BOTTOM_RIGHT:
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                break;
        }
        point.setLayoutParams(params);

        mRoot.addView(point);
    }
}
