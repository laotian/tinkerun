package com.tinkerun.loader;

import android.util.Log;

import com.tencent.tinker.loader.TinkerLoader;
import com.tencent.tinker.loader.app.TinkerApplication;

import  com.tencent.tinker.loader.shareutil.ShareConstants;
import com.tinkerun.debug.NotificationUtil;

/**
 *
 * 加载补丁用的Application
 * Created by tianlupan on 2017/11/14.
 */

public class TinkerunApplication extends TinkerApplication {

    public static TinkerunApplication application;

    public TinkerunApplication(){
        //加载所有产，但资源必须在补丁安装成功后复制出来 TODO
        super(ShareConstants.TINKER_ENABLE_ALL, ApplicationDelegate.class.getName(), TinkerLoader.class.getName(), false);
    }

    @Override
    public void onCreate() {
        application=this;
        Log.d("TinkerunApplication","try to add notification!!");
        NotificationUtil.showNotification(this,"点击查看Tinkerun日志");
        super.onCreate();
    }
}
