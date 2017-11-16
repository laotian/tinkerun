package com.tinkerun.loader;

import android.content.Intent;
import android.util.Log;
import com.tencent.tinker.loader.TinkerLoader;
import com.tencent.tinker.loader.TinkerResourceLoader;
import com.tencent.tinker.loader.app.TinkerApplication;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.tencent.tinker.loader.shareutil.ShareIntentUtil;
import com.tencent.tinker.loader.shareutil.SharePatchFileUtil;
import com.tencent.tinker.loader.shareutil.SharePatchInfo;
import com.tencent.tinker.loader.shareutil.ShareSecurityCheck;
import com.tencent.tinker.loader.shareutil.ShareTinkerInternals;
import java.io.File;

/**
 * Created by tianlupan on 2017/11/16.
 */

public class TinkerunLoader extends TinkerLoader {

    private static final String TAG="TinkerunLoader";


    public static String getPatchVersion(TinkerApplication app){

        File patchDirectoryFile = SharePatchFileUtil.getPatchDirectory(app);
        String patchDirectoryPath = patchDirectoryFile.getAbsolutePath();
        //tinker/patch.info
        File patchInfoFile = SharePatchFileUtil.getPatchInfoFile(patchDirectoryPath);

        //check patch info file whether exist
        if (!patchInfoFile.exists()) {
            return null;
        }
        //old = 641e634c5b8f1649c75caf73794acbdf
        //new = 2c150d8560334966952678930ba67fa8
        File patchInfoLockFile = SharePatchFileUtil.getPatchInfoLockFile(patchDirectoryPath);

        SharePatchInfo patchInfo = SharePatchInfo.readAndCheckPropertyWithLock(patchInfoFile, patchInfoLockFile);
        if(patchInfo!=null){
            return patchInfo.newVersion;
        }
        return null;
    }

    public static String getPatchVersionDirectory(TinkerApplication app){
        String version=getPatchVersion(app);
        return SharePatchFileUtil.getPatchVersionDirectory(version);
    }


    @Override
    public Intent tryLoad(TinkerApplication app) {
        //利用TinkerLoader加载Dex与LocalLib
        Intent resultIntent =  super.tryLoad(app);
        int resultCode=  ShareIntentUtil.getIntentReturnCode(resultIntent);
        //利用TinkerunLoader加载资源，资源必须是完整的，这里需要优化成增量的
        if(resultCode== ShareConstants.ERROR_LOAD_OK){
            //补丁加载成功／加载资源
            final int tinkerFlag = app.getTinkerFlags();
            //check resource
            final boolean isEnabledForResource = ShareTinkerInternals.isTinkerEnabledForResource(tinkerFlag);
            if (isEnabledForResource) {
                //tinker
                File patchDirectoryFile = SharePatchFileUtil.getPatchDirectory(app);
                String patchDirectoryPath = patchDirectoryFile.getAbsolutePath();
                //patch-641e634c
                String patchName = getPatchVersionDirectory(app);
                //tinker/patch-641e634c
                String patchVersionDirectory = patchDirectoryPath + "/" + patchName;
                loadResource(app,patchVersionDirectory,resultIntent);
            }
        }

        loadResource(app,"/sdcard/tinkerun/"+app.getPackageName(),resultIntent);

        return resultIntent;
    }

    private void loadResource(TinkerApplication app,String patchVersionDirectory,Intent resultIntent){

        Log.e(TAG,"loadResource "+patchVersionDirectory+"begin");
        ShareSecurityCheck securityCheck=new ShareSecurityCheck(app);

        //MD5是随便写的，因为我们不效验
        securityCheck.getMetaContentMap().put(ShareConstants.RES_META_FILE,"resources_out.zip,3128467786,88e0f077ac7ce71cab0f844ed49c3425");

        boolean resourceCheck = TinkerResourceLoader.checkComplete(app, patchVersionDirectory, securityCheck, resultIntent);
        if (!resourceCheck) {
            //file not found, do not load patch
            Log.w(TAG, "tryLoadPatchFiles:resource check fail");
            return;
        }

        boolean loadTinkerResources = TinkerResourceLoader.loadTinkerResources(app, patchVersionDirectory, resultIntent);
        if (!loadTinkerResources) {
            Log.w(TAG, "tryLoadPatchFiles:onPatchLoadResourcesFail");
            return;
        }
    }

}
