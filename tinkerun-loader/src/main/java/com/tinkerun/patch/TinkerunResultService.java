package com.tinkerun.patch;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import com.tencent.tinker.lib.service.DefaultTinkerResultService;
import com.tencent.tinker.lib.service.PatchResult;
import com.tencent.tinker.lib.util.TinkerServiceInternals;

/**
 * Created by tianlupan on 2017/11/14.
 */

public class TinkerunResultService extends DefaultTinkerResultService {


    private static final String RESTART_TIPS="安装成功，即将重启应用";

    @Override
    public void onPatchResult(PatchResult result) {
        if(result!=null && result.isSuccess){
            restart();
        }
        super.onPatchResult(result);
    }

    private void restart(){
        TinkerServiceInternals.killAllOtherProcess(this);
        pendingStartActivity(0);
//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(TinkerunResultService.this,RESTART_TIPS,Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    public  void pendingStartActivity(long delayMills) {
        int requestCode=0x1232;
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(getApplicationContext().getApplicationInfo().packageName);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, launchIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + delayMills, pendingIntent);
    }

}
