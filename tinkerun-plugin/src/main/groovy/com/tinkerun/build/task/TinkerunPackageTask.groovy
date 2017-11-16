package com.tinkerun.build.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 *
 *TinkerunPatch
 *
 * //TODO 添加TINKER_ID  loader会校验ShareTinkerInternals#checkSignatureAndTinkerID
 ShareSecurityCheck#getPackagePropertiesIfPresent 要求在包内放assets/package_meta.txt 并且和应用采用同样的签名
 *
 *
 *
 res_meta.txt 示例

resources_out.zip,3128467786,88e0f077ac7ce71cab0f844ed49c3425
pattern:3
resources.arsc
res/*
assets/*
modify:1
resources.arsc
delete:1
assets/xxxx.txt
add:2
res/layout/layout_path.xml
assets/only_use_to_test_tinker_resource.txt

 * @author tianlupan
 */
public class TinkerunPackageTask extends DefaultTask {


    public static final String INSTALL_POSITION="/sdcard/tinkerun/"
    File resourceApk
    File apk
    def packageName

    TinkerunPackageTask() {
        group = 'tinkerun'
    }

    @TaskAction
    def packageAll() {
        project.logger.info("package")

    }

}