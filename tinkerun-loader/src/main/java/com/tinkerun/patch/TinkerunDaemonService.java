package com.tinkerun.patch;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;

import java.io.File;

/**
 * Created by tianlupan on 2017/11/14.
 */

public class TinkerunDaemonService extends IntentService {


    public TinkerunDaemonService(){
        super("Tinkerun-Daemon");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        //TODO 检查SD卡权限
        String patchLocation= Environment.getExternalStorageDirectory()+"/tinkerun/"+getApplicationContext().getApplicationInfo().packageName+".apk";
        File patch=new File(patchLocation);
        if(patch.exists() && patch.canRead()){
            TinkerInstaller.onReceiveUpgradePatch(this,patch.getAbsolutePath());
//            Tinker.with(this).
            Log.e("Tinkerun","found resource file="+patch.getAbsolutePath());
//            TinkerInstaller.onReceiveUpgradePatch(getApplicationContext(),patchLocation);
        }else{
            Log.e("Tinkerun","can't install file="+patch.getAbsolutePath());
        }


    }
}
