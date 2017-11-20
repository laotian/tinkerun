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
    String buildApkPath
    String outputFolder
    def targetDir
    def signConfig
    def tinkerId
    def oldApk

    public TinkerunPatchSchemaTask() {
        description = 'Assemble Tinker Patch'
        group = 'tinkerun'
        outputs.upToDateWhen { false }
    }


    @TaskAction
    def tinkerPatch() {

        //生成patch.zip
        Map<String, String> configFields=new HashMap<>(project.tinkerun.getFields())
        configFields.put("TINKER_ID",tinkerId)
        configFields.put("NEW_TINKER_ID",tinkerId)

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

        builder.setOldApk(oldApk)
               .setNewApk(buildApkPath)
               .setOutBuilder(outputFolder)
               .setIgnoreWarning(true)
               .setDexFilePattern(new ArrayList<String>(configuration.pattern))
               .setIsProtectedApp(true)
//               .setIsComponentHotplugSupported(configuration.supportHotplugComponent)
               .setDexLoaderPattern(new ArrayList<String>(configuration.loader))
               .setDexIgnoreWarningLoaderPattern(new ArrayList<String>())
               .setDexMode(configuration.dexMode)
               .setSoFilePattern(new ArrayList<String>())
               .setResourceFilePattern(new ArrayList<String>(configuration.sourcePattern))
               .setResourceIgnoreChangePattern(new ArrayList<String>())
               .setResourceLargeModSize(100)
               .setUseApplyResource(true)
               .setConfigFields(configFields)
//               .setSevenZipPath(configuration.sevenZip.path)
               .setUseSign(configuration.useSign)

        InputParam inputParam = builder.create()
        TinkerunRunner.gradleRun(inputParam);
    }
}