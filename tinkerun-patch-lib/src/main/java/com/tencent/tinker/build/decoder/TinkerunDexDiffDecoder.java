/*
 * Tencent is pleased to support the open source community by making Tinker available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.tinker.build.decoder;


import com.tencent.tinker.android.dex.ClassDef;
import com.tencent.tinker.android.dex.Dex;
import com.tencent.tinker.android.dex.DexFormat;
import com.tencent.tinker.build.dexpatcher.DexPatchGenerator;
import com.tencent.tinker.build.dexpatcher.util.ChangedClassesDexClassInfoCollector;
import com.tencent.tinker.build.info.InfoWriter;
import com.tencent.tinker.build.patch.Configuration;
import com.tencent.tinker.build.util.*;
import com.tencent.tinker.build.util.DexClassesComparator.DexClassInfo;
import com.tencent.tinker.build.util.DexClassesComparator.DexGroup;
import com.tencent.tinker.commons.dexpatcher.DexPatchApplier;
import com.tencent.tinker.commons.dexpatcher.DexPatcherLogger.IDexPatcherLogger;
import org.jf.dexlib2.builder.BuilderMutableMethodImplementation;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.writer.builder.BuilderField;
import org.jf.dexlib2.writer.builder.BuilderMethod;
import org.jf.dexlib2.writer.builder.DexBuilder;
import org.jf.dexlib2.writer.io.FileDataStore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Created by zhangshaowen on 2016/3/23.
 */
public class TinkerunDexDiffDecoder extends BaseDecoder {
    private static final String TEST_DEX_NAME = "test.dex";
    private static final String CHANGED_CLASSES_DEX_NAME = "changed_classes.dex";

    private final InfoWriter logWriter;
    private final InfoWriter metaWriter;

    private final ExcludedClassModifiedChecker excludedClassModifiedChecker;

    private final Map<String, String> addedClassDescToDexNameMap;
    private final Map<String, String> deletedClassDescToDexNameMap;

    private final List<AbstractMap.SimpleEntry<File, File>> oldAndNewDexFilePairList;

    private final Map<String, RelatedInfo> dexNameToRelatedInfoMap;
    private boolean hasDexChanged = false;


    public TinkerunDexDiffDecoder(Configuration config, String metaPath, String logPath) throws IOException {
        super(config);

        if (metaPath != null) {
            metaWriter = new InfoWriter(config, config.mTempResultDir + File.separator + metaPath);
        } else {
            metaWriter = null;
        }

        if (logPath != null) {
            logWriter = new InfoWriter(config, config.mOutFolder + File.separator + logPath);
        } else {
            logWriter = null;
        }


        excludedClassModifiedChecker = new ExcludedClassModifiedChecker(config);

        addedClassDescToDexNameMap = new HashMap<>();
        deletedClassDescToDexNameMap = new HashMap<>();

        oldAndNewDexFilePairList = new ArrayList<>();

        dexNameToRelatedInfoMap = new HashMap<>();
    }

    @Override
    public void onAllPatchesStart() throws IOException, TinkerPatchException {

    }

    /**
     * Provide /oldFileRoot/dir/to/oldDex, /newFileRoot/dir/to/newDex,
     * return dir/to/oldDex or dir/to/newDex if any one is not null.
     */
    protected String getRelativeDexName(File oldDexFile, File newDexFile) {
        return oldDexFile != null ? getRelativePathStringToOldFile(oldDexFile) : getRelativePathStringToNewFile(newDexFile);
    }

    @SuppressWarnings("NewApi")
    @Override
    public boolean patch(final File oldFile, final File newFile) throws Exception {
        final String dexName = getRelativeDexName(oldFile, newFile);

        // first of all, we should check input files if excluded classes were modified.
        Logger.d("Check for loader classes in dex: %s", dexName);

        try {
            excludedClassModifiedChecker.checkIfExcludedClassWasModifiedInNewDex(oldFile, newFile);
        } catch (IOException e) {
            throw new TinkerPatchException(e);
        } catch (TinkerPatchException e) {
            if (config.mIgnoreWarning) {
                Logger.e("Warning:ignoreWarning is true, but we found %s", e.getMessage());
            } else {
                Logger.e("Warning:ignoreWarning is false, but we found %s", e.getMessage());
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // If corresponding new dex was completely deleted, just return false.
        // don't process 0 length dex
        if (newFile == null || !newFile.exists() || newFile.length() == 0) {
            return false;
        }

        File dexDiffOut = getOutputPath(newFile).toFile();

        final String newMd5 = getRawOrWrappedDexMD5(newFile);

        //new add file
        if (oldFile == null || !oldFile.exists() || oldFile.length() == 0) {
            hasDexChanged = true;
            copyNewDexAndLogToDexMeta(newFile, newMd5, dexDiffOut);
            return true;
        }

        final String oldMd5 = getRawOrWrappedDexMD5(oldFile);

        if ((oldMd5 != null && !oldMd5.equals(newMd5)) || (oldMd5 == null && newMd5 != null)) {
            hasDexChanged = true;
            if (oldMd5 != null) {
                collectAddedOrDeletedClasses(oldFile, newFile);
            }
        }

        RelatedInfo relatedInfo = new RelatedInfo();
        relatedInfo.oldMd5 = oldMd5;
        relatedInfo.newMd5 = newMd5;

        // collect current old dex file and corresponding new dex file for further processing.
        oldAndNewDexFilePairList.add(new AbstractMap.SimpleEntry<>(oldFile, newFile));

        dexNameToRelatedInfoMap.put(dexName, relatedInfo);

        return true;
    }

    @Override
    public void onAllPatchesEnd() throws Exception {
//        generateChangedClassesDexFile();
        addTestDex();
    }

    @SuppressWarnings("NewApi")
    private void generateChangedClassesDexFile() throws IOException {
        final String dexMode = config.mDexRaw ? "raw" : "jar";
        final File dest = new File(config.mTempResultDir + "/" + CHANGED_CLASSES_DEX_NAME);

        Logger.d("\nBuilding changed classes dex: %s, size: %d\n", dest.getAbsolutePath(), dest.length());


        final String md5 = MD5.getMD5(dest);

        String meta = CHANGED_CLASSES_DEX_NAME + "," + "" + "," + md5 + "," + md5 + "," + 0
                + "," + 0 + "," + 0 + "," + dexMode;

        Logger.d("\nDexDecoder:write changed classes dex meta file data: %s", meta);

        metaWriter.writeLineToInfoFile(meta);
    }

    private void addTestDex() throws IOException {
        //write test dex
        String dexMode = "jar";
        if (config.mDexRaw) {
            dexMode = "raw";
        }

        final InputStream is = TinkerunDexDiffDecoder.class.getResourceAsStream("/" + TEST_DEX_NAME);
        String md5 = MD5.getMD5(is, 1024);
        is.close();

        String meta = TEST_DEX_NAME + "," + "" + "," + md5 + "," + md5 + "," + 0 + "," + 0 + "," + 0 + "," + dexMode;

        File dest = new File(config.mTempResultDir + "/" + TEST_DEX_NAME);
        FileOperation.copyResourceUsingStream(TEST_DEX_NAME, dest);
        Logger.d("\nAdd test install result dex: %s, size:%d", dest.getAbsolutePath(), dest.length());
        Logger.d("DexDecoder:write test dex meta file data: %s", meta);

        metaWriter.writeLineToInfoFile(meta);
    }


    /**
     * Before starting real diff works, we collect added class descriptor
     * and deleted class descriptor for further analysing in {@code checkCrossDexMovingClasses}.
     */
    private void collectAddedOrDeletedClasses(File oldFile, File newFile) throws IOException {
        Dex oldDex = new Dex(oldFile);
        Dex newDex = new Dex(newFile);

        Set<String> oldClassDescs = new HashSet<>();
        for (ClassDef oldClassDef : oldDex.classDefs()) {
            oldClassDescs.add(oldDex.typeNames().get(oldClassDef.typeIndex));
        }

        Set<String> newClassDescs = new HashSet<>();
        for (ClassDef newClassDef : newDex.classDefs()) {
            newClassDescs.add(newDex.typeNames().get(newClassDef.typeIndex));
        }

        Set<String> addedClassDescs = new HashSet<>(newClassDescs);
        addedClassDescs.removeAll(oldClassDescs);

        Set<String> deletedClassDescs = new HashSet<>(oldClassDescs);
        deletedClassDescs.removeAll(newClassDescs);

        for (String addedClassDesc : addedClassDescs) {
            if (addedClassDescToDexNameMap.containsKey(addedClassDesc)) {
                throw new TinkerPatchException(
                        String.format(
                                "Class Duplicate. Class [%s] is added in both new dex: [%s] and [%s]. Please check your newly apk.",
                                addedClassDesc,
                                addedClassDescToDexNameMap.get(addedClassDesc),
                                newFile.toString()
                        )
                );
            } else {
                addedClassDescToDexNameMap.put(addedClassDesc, newFile.toString());
            }
        }

        for (String deletedClassDesc : deletedClassDescs) {
            if (deletedClassDescToDexNameMap.containsKey(deletedClassDesc)) {
                throw new TinkerPatchException(
                        String.format(
                                "Class Duplicate. Class [%s] is deleted in both old dex: [%s] and [%s]. Please check your base apk.",
                                deletedClassDesc,
                                addedClassDescToDexNameMap.get(deletedClassDesc),
                                oldFile.toString()
                        )
                );
            } else {
                deletedClassDescToDexNameMap.put(deletedClassDesc, newFile.toString());
            }
        }
    }

    private boolean isDexNameMatchesClassNPattern(String dexName) {
        return (dexName.matches("^classes[0-9]*\\.dex$"));
    }

    private void copyNewDexAndLogToDexMeta(File newFile, String newMd5, File output) throws IOException {
        FileOperation.copyFileUsingStream(newFile, output);
        logToDexMeta(newFile, null, null, newMd5, newMd5, "0");
    }

    /**
     * Construct dex meta-info and write it to meta file and log.
     *
     * @param newOrFullPatchedFile New dex file or full patched dex file.
     * @param oldFile              Old dex file.
     * @param dexDiffFile          Dex diff file. (Generated by DexPatchGenerator.)
     * @param destMd5InDvm         Md5 of output dex in dvm environment, could be full patched dex md5 or new dex.
     * @param destMd5InArt         Md5 of output dex in dvm environment, could be small patched dex md5 or new dex.
     * @param dexDiffMd5           Md5 of dex patch info file.
     * @throws IOException
     */
    protected void logToDexMeta(File newOrFullPatchedFile, File oldFile, File dexDiffFile, String destMd5InDvm, String destMd5InArt, String dexDiffMd5) throws IOException {
        if (metaWriter == null && logWriter == null) {
            return;
        }
        String parentRelative = getParentRelativePathStringToNewFile(newOrFullPatchedFile);
        String relative = getRelativePathStringToNewFile(newOrFullPatchedFile);

        if (metaWriter != null) {
            String fileName = newOrFullPatchedFile.getName();
            String dexMode = "jar";
            if (config.mDexRaw) {
                dexMode = "raw";
            }

            //new file
            String oldCrc;
            if (oldFile == null) {
                oldCrc = "0";
                Logger.d("DexDecoder:add newly dex file: %s", parentRelative);
            } else {
                oldCrc = FileOperation.getZipEntryCrc(config.mOldApkFile, relative);
                if (oldCrc == null || oldCrc.equals("0")) {
                    throw new TinkerPatchException(
                            String.format("can't find zipEntry %s from old apk file %s", relative, config.mOldApkFile.getPath())
                    );
                }
            }

            String newCrc = FileOperation.getZipEntryCrc(config.mNewApkFile, relative);
            String meta = fileName + "," + parentRelative + "," + destMd5InDvm + ","
                    + destMd5InArt + "," + dexDiffMd5 + "," + oldCrc + "," + newCrc + "," + dexMode;

            Logger.d("DexDecoder:write meta file data: %s", meta);
            metaWriter.writeLineToInfoFile(meta);
        }

        if (logWriter != null) {
            String log = relative + ", oldSize=" + FileOperation.getFileSizes(oldFile) + ", newSize="
                    + FileOperation.getFileSizes(newOrFullPatchedFile) + ", diffSize=" + FileOperation.getFileSizes(dexDiffFile);

            logWriter.writeLineToInfoFile(log);
        }
    }

    @Override
    public void clean() {
        metaWriter.close();
        logWriter.close();
    }

    private String getRawOrWrappedDexMD5(File dexOrJarFile) {
        final String name = dexOrJarFile.getName();
        if (name.endsWith(".dex")) {
            return MD5.getMD5(dexOrJarFile);
        } else {
            JarFile dexJar = null;
            try {
                dexJar = new JarFile(dexOrJarFile);
                ZipEntry classesDex = dexJar.getEntry(DexFormat.DEX_IN_JAR_NAME);
                // no code
                if (classesDex == null) {
                    throw new TinkerPatchException(
                            String.format("Jar file %s do not contain 'classes.dex', it is not a correct dex jar file!", dexOrJarFile.getAbsolutePath())
                    );
                }
                return MD5.getMD5(dexJar.getInputStream(classesDex), 1024 * 100);
            } catch (IOException e) {
                throw new TinkerPatchException(
                        String.format("File %s is not end with '.dex', but it is not a correct dex jar file !", dexOrJarFile.getAbsolutePath()), e
                );
            } finally {
                if (dexJar != null) {
                    try {
                        dexJar.close();
                    } catch (Exception e) {
                        // Ignored.
                    }
                }
            }
        }
    }

    private String getRelativeStringBy(File file, File reference) {
        File actualReference = reference.getParentFile();
        if (actualReference == null) {
            actualReference = reference;
        }
        return actualReference.toPath().relativize(file.toPath()).toString().replace("\\", "/");
    }

    private void ensureDirectoryExist(File dir) {
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new TinkerPatchException("failed to create directory: " + dir);
            }
        }
    }

    private final class RelatedInfo {
        File newOrFullPatchedFile = null;
        /**
         * This field could be null if old dex and new dex
         * are the same.
         */
        File dexDiffFile = null;
        String oldMd5 = "0";
        String newMd5 = "0";
        String dexDiffMd5 = "0";
        /**
         * This field could be one of the following value:
         * fullPatchedDex md5, if old dex and new dex are different;
         * newDex md5, if new dex is marked to be copied directly;
         */
        String newOrFullPatchedMd5 = "0";
    }
}



