package com.test.jalalsouky.stickrtest;

import android.app.Activity;
import android.os.Bundle;

import com.test.jalalsouky.stickrtest.view.TransformView;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TransformView view = (TransformView)findViewById(R.id.view);
        view.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));
    }
}
