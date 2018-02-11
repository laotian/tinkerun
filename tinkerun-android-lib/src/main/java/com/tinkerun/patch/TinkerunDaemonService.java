package com.tinkerun.patch;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.tencent.tinker.lib.patch.TinkerunUpgradePatch;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.tinkerun.debug.NotificationUtil;
import com.tinkerun.loader.ApplicationDelegate;

import java.io.File;

/**
 * Created by tianlupan on 2017/11/14.
 */

public class TinkerunDaemonService extends IntentService {

    private static final String PATH_FILE="/data/local/tmp/tinkerun/%s/patch.apk";

    public TinkerunDaemonService(){
        super("Tinkerun-Daemon");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        NotificationUtil.showNotification(this,"收到补丁，正在安装");
        String patchLocation=String.format(PATH_FILE,getApplicationContext().getApplicationInfo().packageName);
        File patch=new File(patchLocation);
        if(patch.exists() && patch.canRead()){
            TinkerInstaller.onReceiveUpgradePatch(this,patch.getAbsolutePath());
        }else{
            Log.e("Tinkerun","can't install file="+patch.getAbsolutePath()+",patch file not exists or you don't have permission");
        }


    }


}
