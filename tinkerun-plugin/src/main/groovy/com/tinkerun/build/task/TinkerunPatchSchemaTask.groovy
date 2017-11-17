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

package com.tinkerun.build.task
import com.tencent.tinker.build.patch.InputParam
import com.tencent.tinker.build.patch.Runner
import com.tencent.tinker.build.patch.TinkerunRunner
import com.tinkerun.build.TinkerunPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
/**
 * The configuration properties.
 *
 * @author zhangshaowen
 */
public class TinkerunPatchSchemaTask extends DefaultTask {
    def configuration
    def android
    String buildApkPath
    String outputFolder
    def signConfig
    def resourcesFile //resources.ap_
    def tinkerId
    def patchApk

    public TinkerunPatchSchemaTask() {
        description = 'Assemble Tinker Patch'
        group = 'tinkerun'
        outputs.upToDateWhen { false }
        android = project.extensions.android
    }


    @TaskAction
    def tinkerPatch() {

        //复制resources.ap_
        project.copy {
            from resourcesFile
            rename{String fileName ->
                TinkerunPlugin.RESOURCES_FILE_NAME
            }
            into outputFolder
        }

        //FIXME
        if(1<2) return ;

        //生成patch.zip
        configuration.checkParameter()
        configuration.buildConfig.checkParameter()
        configuration.res.checkParameter()
        configuration.dex.checkDexMode()
        configuration.sevenZip.resolveZipFinalPath()

        InputParam.Builder builder = new InputParam.Builder()
        if (configuration.useSign) {
            if (signConfig == null) {
                throw new GradleException("can't the get signConfig for this build")
            }
            builder.setSignFile(signConfig.storeFile)
                    .setKeypass(signConfig.keyPassword)
                    .setStorealias(signConfig.keyAlias)
                    .setStorepass(signConfig.storePassword)

        }

        builder.setOldApk(configuration.oldApk)
               .setNewApk(buildApkPath)
               .setOutBuilder(outputFolder)
               .setIgnoreWarning(configuration.ignoreWarning)
               .setDexFilePattern(new ArrayList<String>(configuration.dex.pattern))
               .setIsProtectedApp(configuration.buildConfig.isProtectedApp)
               .setIsComponentHotplugSupported(configuration.buildConfig.supportHotplugComponent)
               .setDexLoaderPattern(new ArrayList<String>(configuration.dex.loader))
               .setDexIgnoreWarningLoaderPattern(new ArrayList<String>(configuration.dex.ignoreWarningLoader))
               .setDexMode(configuration.dex.dexMode)
               .setSoFilePattern(new ArrayList<String>(configuration.lib.pattern))
               .setResourceFilePattern(new ArrayList<String>(configuration.res.pattern))
               .setResourceIgnoreChangePattern(new ArrayList<String>(configuration.res.ignoreChange))
               .setResourceLargeModSize(configuration.res.largeModSize)
               .setUseApplyResource(configuration.buildConfig.usingResourceMapping)
               .setConfigFields(new HashMap<String, String>(configuration.packageConfig.getFields()))
               .setSevenZipPath(configuration.sevenZip.path)
               .setUseSign(configuration.useSign)

        InputParam inputParam = builder.create()
        TinkerunRunner.gradleRun(inputParam);
    }
}