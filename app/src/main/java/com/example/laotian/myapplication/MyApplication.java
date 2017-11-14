package com.example.laotian.myapplication;

import android.app.Application;
import android.support.multidex.MultiDex;
import android.util.Log;

public class MyApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
        Log.d("MyApplication","onCreate2..");
    }
}
