package com.tinkerun.patch;

import android.content.Context;
import android.util.Log;

import com.tencent.tinker.lib.patch.*;
import com.tencent.tinker.lib.service.PatchResult;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.loader.shareutil.SharePatchFileUtil;
import com.tencent.tinker.loader.shareutil.ShareSecurityCheck;
import com.tencent.tinker.loader.shareutil.ShareTinkerInternals;

import java.io.File;

/**
 * Created by tianlupan on 2017/11/14.
 */

public class TinkerunUpgradePatch extends UpgradePatch {

    private static final String TAG="TinkerunUpgradePatch";

    @Override
    public boolean tryPatch(Context context, String tempPatchPath, PatchResult patchResult) {
        Log.d(TAG,"tryPatch...called..");
        boolean result= super.tryPatch(context, tempPatchPath, patchResult);
        if(result){
            Tinker manager = Tinker.with(context);
            final File patchFile = new File(tempPatchPath);
            //check ok, we can real recover a new patch
            final String patchDirectory = manager.getPatchDirectory().getAbsolutePath();

            //it is a new patch, we first delete if there is any files
            //don't delete dir for faster retry
//        SharePatchFileUtil.deleteDir(patchVersionDirectory);
            String patchMd5=patchResult.patchVersion;

            final String patchName = SharePatchFileUtil.getPatchVersionDirectory(patchMd5);

            final String patchVersionDirectory = patchDirectory + "/" + patchName;

            //check the signature, we should create a new checker
            ShareSecurityCheck signatureCheck = new ShareSecurityCheck(context);

            int returnCode = ShareTinkerInternals.checkTinkerPackage(context, manager.getTinkerFlags(), patchFile, signatureCheck);

            //copy file
            File destPatchFile = new File(patchVersionDirectory + "/" + SharePatchFileUtil.getPatchVersionFile(patchMd5));
            if (!ResDiffPatchInternal.tryRecoverResourceFiles(manager, signatureCheck, context, patchVersionDirectory, destPatchFile)) {
                TinkerLog.e(TAG, "UpgradePatch tryPatch:new patch recover, try patch resource failed");
                patchResult.isSuccess=false;
                return false;
            }
        }
        return result;
    }
}
