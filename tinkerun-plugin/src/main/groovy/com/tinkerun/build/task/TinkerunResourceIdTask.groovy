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

import com.tencent.tinker.build.aapt.AaptResourceCollector
import com.tencent.tinker.build.aapt.AaptUtil
import com.tencent.tinker.build.aapt.PatchUtil
import com.tencent.tinker.build.aapt.RDotTxtEntry
import com.tencent.tinker.build.util.FileOperation
import com.tinkerun.build.TinkerunPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

/**
 * The configuration properties.
 *
 * @author zhangshaowen
 */
public class TinkerunResourceIdTask extends DefaultTask {
    static final String RESOURCE_PUBLIC_XML = TinkerunPlugin.TINKER_INTERMEDIATES + "public.xml"
    static final String RESOURCE_IDX_XML = TinkerunPlugin.TINKER_INTERMEDIATES + "idx.xml"

    String resDir
    String rTxtFile

    //如果是cleanMode清除，否则生成id.xml和public.xml
    boolean  cleanMode=false


    TinkerunResourceIdTask() {
        group = 'tinkerun'
    }

    @TaskAction
    def applyResourceId() {
//        String resourceMappingFile = project.extensions.tinkerun.applyResourceMapping

        String idsXml = resDir + "/values/ids.xml";
        String publicXml = resDir + "/values/public.xml";

        if(cleanMode){
            FileOperation.deleteFile(idsXml);
            FileOperation.deleteFile(publicXml);
            return
        }

        //不重复生成
        if(FileOperation.fileExists(publicXml) && FileOperation.fileExists(idsXml)){
            return
        }

        def resourceMappingFile=project.file(rTxtFile).getAbsolutePath()
        // Parse the public.xml and ids.xml
        if (!FileOperation.isLegalFile(resourceMappingFile)) {
            throw new GradleException("apply resource mapping file ${resourceMappingFile} is illegal, just ignore, path resource failed!")
//            project.logger.error()
            return
        }


        List<String> resourceDirectoryList = new ArrayList<String>()
        resourceDirectoryList.add(resDir)

        project.logger.info("we build ${project.getName()} apk with apply resource mapping file ${resourceMappingFile}")
//        project.extensions.tinkerun.usingResourceMapping = true
        Map<RDotTxtEntry.RType, Set<RDotTxtEntry>> rTypeResourceMap = PatchUtil.readRTxt(resourceMappingFile)

        //TODO 不扫描resourceDirectoryList下的文件
        AaptResourceCollector aaptResourceCollector = AaptUtil.collectResource(resourceDirectoryList, rTypeResourceMap)
        PatchUtil.generatePublicResourceXml(aaptResourceCollector, idsXml, publicXml)
        //暂时不支持相对上一次的增量资源，只针对原始资源增量
//        File publicFile = new File(publicXml)
//        if (publicFile.exists()) {
//            FileOperation.copyFileUsingStream(publicFile, project.file(RESOURCE_PUBLIC_XML))
//            project.logger.error("tinker gen resource public.xml in ${RESOURCE_PUBLIC_XML}")
//        }
//        File idxFile = new File(idsXml)
//        if (idxFile.exists()) {
//            FileOperation.copyFileUsingStream(idxFile, project.file(RESOURCE_IDX_XML))
//            project.logger.error("tinker gen resource idx.xml in ${RESOURCE_IDX_XML}")
//        }
    }
}

