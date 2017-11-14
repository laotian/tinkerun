package com.example.laotian.myapplication;

import android.app.Application;
import android.util.Log;
import io.laotian.tinkerun.com.tencent.tinker.loader.TinkerLoader;

public class MyApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        TinkerLoader.loadResource(this);
        Log.d("MyApplication","onCreate..");
    }
}
