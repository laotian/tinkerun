package io.laotian.tinkerun.com.tencent.tinker.loader;

import android.content.Context;
import android.util.Log;

import java.io.File;

/**
 * Created by tianlupan on 2017/11/13.
 */

public class TinkerLoader {

    public static void loadResource(Context context){
        File file= new File("/sdcard/app-debug.apk");
        if(!file.exists()) return;
        try {
            TinkerResourcePatcher.isResourceCanPatch(context);
            TinkerResourcePatcher.monkeyPatchExistingResources(context,file.getAbsolutePath());
        } catch (Throwable throwable) {
            Log.e("MyApplication","install resource failed...");
            throwable.printStackTrace();
        }

    }

}
