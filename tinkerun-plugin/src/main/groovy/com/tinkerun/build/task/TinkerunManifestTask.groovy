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

import com.tinkerun.build.TinkerunPlugin
import groovy.xml.Namespace
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

/**
 *  把Manifest中的application替换成 com.tinkerun.TinkerunApplication
 * 并且通过添加<meta-data TINKERUN_APP="旧application name" /> 保存旧的applicationName,以便委拖
 * @author zhangshaowen
 * @author tianlupan
 */
public class TinkerunManifestTask extends DefaultTask {
    static final String MANIFEST_XML = TinkerunPlugin.TINKER_INTERMEDIATES + "AndroidManifest.xml"
    static final String TINKER_ID = "TINKERUN_APP"
    static final String TINKERUN_APPLICATION="com.tinkerun.TinkerunApplication"
    String manifestPath

    TinkerunManifestTask() {
        group = 'tinkerun'
    }

    @TaskAction
    def updateManifest() {
        project.logger.error("tinkerun want to update ${manifestPath}")
            writeManifestMeta()
//        String applicationName = readManifestApplicationName(manifestPath)
//        if(applicationName==null){
//            applicationName=""
//        }
//        project.logger.error("tinker add ${tinkerValue} to your AndroidManifest.xml ${manifestPath}")
//
//        writeManifestMeta(manifestPath, TINKER_ID, applicationName)
//        addApplicationToLoaderPattern()
//        File manifestFile = new File(manifestPath)
//        if (manifestFile.exists()) {
//            FileOperation.copyFileUsingStream(manifestFile, project.file(MANIFEST_XML))
//            project.logger.error("tinker gen AndroidManifest.xml in ${MANIFEST_XML}")
//        }

    }

    void writeManifestMeta() {
        def ns = new Namespace("http://schemas.android.com/apk/res/android", "android")

        def xml = new XmlParser().parse(new InputStreamReader(new FileInputStream(manifestPath), "utf-8"))

        def application = xml.application[0]
        if (application) {

            def applicationName = application.attributes()[ns.name]
            application.attributes()[ns.name]=TINKERUN_APPLICATION

            def metaDataTags = application['meta-data']

            // remove any old TINKER_ID elements
            def tinkerId = metaDataTags.findAll {
                it.attributes()[ns.name].equals(TINKER_ID)
            }.each {
                it.parent().remove(it)
            }

            if (applicationName != null) {
            // Add the new TINKER_ID element
            application.appendNode('meta-data', [(ns.name): TINKER_ID, (ns.value): applicationName])
        }


            // Write the manifest file
            def printer = new XmlNodePrinter(new PrintWriter(manifestPath, "utf-8"))
            printer.preserveWhitespace = true
            printer.print(xml)
        }
    }

    void addApplicationToLoaderPattern() {
        Iterable<String> loader = project.extensions.tinkerPatch.dex.loader
        String applicationName = readManifestApplicationName(manifestPath)

        if (applicationName != null && !loader.contains(applicationName)) {
            loader.add(applicationName)
            project.logger.error("tinker add ${applicationName} to dex loader pattern")
        }
        String loaderClass = "com.tencent.tinker.loader.*"
        if (!loader.contains(loaderClass)) {
            loader.add(loaderClass)
            project.logger.error("tinker add ${loaderClass} to dex loader pattern")
        }

    }

    String readManifestApplicationName(String path) {
        def xml = new XmlParser().parse(new InputStreamReader(new FileInputStream(path), "utf-8"))
        def ns = new Namespace("http://schemas.android.com/apk/res/android", "android")

        def application = xml.application[0]
        if (application) {
            return application.attributes()[ns.name]
        }
        return null
    }
}

