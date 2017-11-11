package com.example.laotian.myapplication;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class MyApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        File file= new File("/sdcard/app-debug.apk");
        if(!file.exists()) return;
        try {
            TinkerResourcePatcher.isResourceCanPatch(this);
            TinkerResourcePatcher.monkeyPatchExistingResources(this,file.getAbsolutePath());
        } catch (Throwable throwable) {
            Log.e("MyApplication","install resource failed...");
            throwable.printStackTrace();
        }

        Log.d("MyApplication","onCreate..");

    }
}
