package com.tencent.tinker.lib.tinker;

import android.content.Context;
import android.content.SharedPreferences;

import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.lib.util.TinkerServiceInternals;
import com.tencent.tinker.loader.shareutil.ShareConstants;

/**
 * Created by tianlupan on 2018/2/12.
 */

public class TinkerUtil {

    /**
     * 启用Tinker
     * @param context
     */
    public static void enableTinker(Context context){
        if(Tinker.isTinkerInstalled()) {
            Tinker tinker=Tinker.with(context);
            if(!tinker.isTinkerEnabled()) {
                Tinker.with(context).tinkerFlags = ShareConstants.TINKER_ENABLE_ALL;
            }
            if(!TinkerServiceInternals.isTinkerEnableWithSharedPreferences(context)){
                SharedPreferences sp = context.getSharedPreferences(ShareConstants.TINKER_SHARE_PREFERENCE_CONFIG, Context.MODE_MULTI_PROCESS);
                sp.edit().remove(getTinkerSharedPreferencesName()).commit();
            }
        }
    }

    private static String getTinkerSharedPreferencesName() {
        return ShareConstants.TINKER_ENABLE_CONFIG + ShareConstants.TINKER_VERSION;
    }

}
