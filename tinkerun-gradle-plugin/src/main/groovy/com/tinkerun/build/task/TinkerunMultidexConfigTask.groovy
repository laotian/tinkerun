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

import com.tinkerun.build.TinkerunAppPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * The configuration properties.
 *
 * @author zhangshaowen
 */
public class TinkerunMultidexConfigTask extends DefaultTask {
    static final String MULTIDEX_CONFIG_PATH = TinkerunAppPlugin.TINKER_INTERMEDIATES + "tinkerun_multidexkeep.pro"
    static final String MULTIDEX_CONFIG_SETTINGS =
            "-keep public class * implements com.tencent.tinker.loader.app.ApplicationLifeCycle {\n" +
                    "    <init>(...);\n" +
                    "    void onBaseContextAttached(android.content.Context);\n" +
                    "}\n" +
                    "\n" +
                    "-keep public class * extends com.tencent.tinker.loader.TinkerLoader {\n" +
                    "    <init>(...);\n" +
                    "}\n" +
                    "\n" +
                    "-keep public class * extends android.app.Application {\n" +
                    "     <init>();\n" +
                    "     void attachBaseContext(android.content.Context);\n" +
                    "}\n"


    def applicationVariant

    public TinkerunMultidexConfigTask() {
        group = 'tinkerun'
    }

    @TaskAction
    def updateTinkerProguardConfig() {

        StringBuffer lines = new StringBuffer()
        lines.append("\n")
             .append("#tinker multidex keep patterns:\n")
             .append(MULTIDEX_CONFIG_SETTINGS)
             .append("\n")

        // This class must be placed in main dex so that we can use it to check if new pathList
        // in AndroidNClassLoader is fine when under the protected app (whose main dex is always encrypted).
        lines.append("-keep class com.tencent.tinker.loader.TinkerTestAndroidNClassLoader {\n" +
                "    <init>(...);\n" +
                "}\n")
             .append("\n")

        lines.append("#your dex.loader patterns here\n")

        Iterable<String> loader = project.extensions.tinkerun.loader
        for (String pattern : loader) {
            if (pattern.endsWith("*")) {
                if (!pattern.endsWith("**")) {
                    pattern += "*"
                }
            }
            lines.append("-keep class " + pattern + " {\n" +
                    "    <init>(...);\n" +
                    "}\n")
                    .append("\n")
        }


        File multiDexKeepProguard = null
        try {
            multiDexKeepProguard = applicationVariant.getVariantData().getScope().getManifestKeepListProguardFile()
        } catch (Throwable ignore) {
            try {
                multiDexKeepProguard = applicationVariant.getVariantData().getScope().getManifestKeepListFile()
            } catch (Throwable e) {
                project.logger.error("can't find getManifestKeepListFile method, exception:${e}")
            }
        }
        if (multiDexKeepProguard == null) {
            saveTemp(lines)
            return
        }
        FileWriter manifestWriter = new FileWriter(multiDexKeepProguard, true)
        try {
            for (String line : lines) {
                manifestWriter.write(line)
            }
        } finally {
            manifestWriter.close()
        }
    }

    private void saveTemp(Iterable<String> lines){
        File file = project.file(MULTIDEX_CONFIG_PATH)
        project.logger.error("auto add multidex keep pattern fail, you can only copy ${file} to your own multiDex keep proguard file yourself.")

        // Create the directory if it doesn't exist already
        file.getParentFile().mkdirs()

        // Write our recommended proguard settings to this file
        FileWriter fr = new FileWriter(file.path)
        try {
            for (String line : lines) {
                fr.write(line)
            }
        } finally {
            fr.close()
        }
    }


}