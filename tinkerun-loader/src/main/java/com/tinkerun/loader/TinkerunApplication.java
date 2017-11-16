package com.tinkerun.loader;

import com.tencent.tinker.loader.TinkerLoader;
import com.tencent.tinker.loader.app.TinkerApplication;

import  com.tencent.tinker.loader.shareutil.ShareConstants;

/**
 *
 * 加载补丁用的Application
 * Created by tianlupan on 2017/11/14.
 */

public class TinkerunApplication extends TinkerApplication {

    public TinkerunApplication(){
        //加载所有产，但资源必须在补丁安装成功后复制出来 TODO
        super(ShareConstants.TINKER_ENABLE_ALL, ApplicationDelegate.class.getName(), TinkerLoader.class.getName(), false);
    }


}
