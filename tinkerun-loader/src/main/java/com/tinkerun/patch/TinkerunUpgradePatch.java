package com.tinkerun.patch;

import android.content.Context;
import android.util.Log;

import com.tencent.tinker.lib.patch.UpgradePatch;
import com.tencent.tinker.lib.service.PatchResult;

/**
 * Created by tianlupan on 2017/11/14.
 */

public class TinkerunUpgradePatch extends UpgradePatch {
    @Override
    public boolean tryPatch(Context context, String tempPatchPath, PatchResult patchResult) {
        Log.d("Tinkerun","tryPatch...called..");
        return super.tryPatch(context, tempPatchPath, patchResult);
    }
}
