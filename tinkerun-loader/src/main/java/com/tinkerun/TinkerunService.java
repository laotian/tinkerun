package com.tinkerun;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.tencent.tinker.lib.tinker.TinkerInstaller;

import java.io.File;

/**
 * Created by tianlupan on 2017/11/14.
 */

public class TinkerunService extends IntentService {


    public TinkerunService(){
        super("Tinkerun-Daemon");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        String patchLocation= Environment.getExternalStorageDirectory()+"/"+getApplicationContext().getApplicationInfo().packageName+"-patch.apk";
        File patch=new File(patchLocation);
        if(patch.exists() && patch.canRead()){
            TinkerInstaller.onReceiveUpgradePatch(getApplicationContext(),patchLocation);
        }else{
            Log.e("Tinkerun","can't install file="+patch.getAbsolutePath());
        }


    }
}
