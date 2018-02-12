package com.tinkerun.loader;

import android.content.Context;
import com.tencent.tinker.lib.reporter.DefaultLoadReporter;
import com.tencent.tinker.lib.tinker.TinkerUtil;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.tinkerun.debug.NotificationUtil;
import com.tinkerun.debug.TinkerunConstants;
import java.io.File;

/**
 * Created by tianlupan on 2018/2/12.
 */

public class TinkerunLoadReporter extends DefaultLoadReporter {

    private static final String TAG="TinkerunLoadReporter";

    public TinkerunLoadReporter(Context context) {
        super(context);
    }

    @Override
    public void onLoadPatchListenerReceiveFail(File patchFile, int errorCode) {
        super.onLoadPatchListenerReceiveFail(patchFile, errorCode);
        String error=TinkerunConstants.getError(errorCode,"ERROR_PATCH_");
        sendNotification(errorCode== ShareConstants.ERROR_LOAD_OK,",onLoadPatchListenerReceiveFail，errorCode="+errorCode+"->"+error);
    }

    @Override
    public void onLoadException(Throwable e, int errorCode) {
        super.onLoadException(e, errorCode);
        //发生异常时会在父类中禁用Tinker,导致后续补丁无法安装，这里要清除状态
        TinkerUtil.enableTinker(context);
        TinkerLog.e(TAG,"onLoadException:"+errorCode+",Tinker被禁用后，被Tinkerun重新开启");
        String errorCodeDesc=TinkerunConstants.getError(errorCode,"ERROR_LOAD_EXCEPTION_");
        sendNotification(errorCode== ShareConstants.ERROR_LOAD_OK, "loadException errorCode:"+errorCode+" ->"+ errorCodeDesc);
    }

    @Override
    public void onLoadResult(File patchDirectory, int loadCode, long cost) {
        super.onLoadResult(patchDirectory, loadCode, cost);
        if(loadCode==ShareConstants.ERROR_LOAD_OK){
            sendNotification(true,null);
        }else if(loadCode==ShareConstants.ERROR_LOAD_DISABLE){
            sendNotification(true,"Tinker被禁用了!!");
        }
        else{
            String errorCodeDesc=TinkerunConstants.getLoadErrorCode(loadCode);
            sendNotification(false, "loadCodeFail:"+loadCode+" ->"+ errorCodeDesc);
        }
    }

    @Override
    public void onLoadPackageCheckFail(File patchFile, int errorCode) {
        super.onLoadPackageCheckFail(patchFile, errorCode);
        String errorCodeDesc=TinkerunConstants.getPackageCheckErrorCode(errorCode);
        sendNotification(errorCode== ShareConstants.ERROR_PACKAGE_CHECK_OK, "packageCheckFail:"+errorCode+" ->"+ errorCodeDesc);
    }

    private void sendNotification(boolean success, String msg){
        String content=success ? "加载补丁成功" : msg;
        TinkerLog.w(TAG,"发送Notification:"+msg);
        NotificationUtil.showNotification(context,content);
    }




}
