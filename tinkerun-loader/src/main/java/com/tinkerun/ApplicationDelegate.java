package com.tinkerun;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.text.TextUtils;

import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.loader.app.DefaultApplicationLike;

/**
 * Created by tianlupan on 2017/11/14.
 */

public class ApplicationDelegate extends DefaultApplicationLike {

    private final Application userApplication;
    public ApplicationDelegate(Application application, int tinkerFlags, boolean tinkerLoadVerifyFlag, long applicationStartElapsedTime, long applicationStartMillisTime, Intent tinkerResultIntent) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent);
        userApplication=createApplication();
    }

    //Create User_defined Application
    private Application createApplication(){
        //TODO 读取Manifest 中的Application设置
        String appClass=null;
        if(!TextUtils.isEmpty(appClass)){
            try {
              return    (Application) Class.forName(appClass).newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void onCreate() {
        if(userApplication!=null){
            userApplication.onCreate();
        }
        installTinker();
        //TODO 监听补丁
    }

    private void installTinker(){
        //TODO  清除Tinker，安装自己的
        Tinker tinker = new Tinker.Builder(getApplication()).build();
        Tinker.create(tinker);
        tinker.install(getTinkerResultIntent(),TinkerunResultService.class,new TinkerunUpgradePatch());
    }

    @Override
    public void onLowMemory() {
        if(userApplication!=null){
            userApplication.onLowMemory();;
        }
    }

    @Override
    public void onTrimMemory(int level) {
        if(userApplication!=null){
            userApplication.onTrimMemory(level);
        }
    }

    @Override
    public void onTerminate() {
        if(userApplication!=null){
            userApplication.onTerminate();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if(userApplication!=null){
            userApplication.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onBaseContextAttached(Context base) {
        if(userApplication!=null){
            userApplication.onCreate();
        }
    }



}
