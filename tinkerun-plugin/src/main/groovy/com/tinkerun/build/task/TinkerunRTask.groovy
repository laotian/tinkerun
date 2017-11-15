package com.tinkerun.build.task

import com.tencent.tinker.build.aapt.AaptResourceCollector
import com.tencent.tinker.build.aapt.AaptUtil
import com.tencent.tinker.build.aapt.PatchUtil
import com.tencent.tinker.build.aapt.RDotTxtEntry
import com.tencent.tinker.build.util.FileOperation
import com.tinkerun.build.TinkerunPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

/**
 * The configuration properties.
 *
 * Copy R.txt
 *
 * @author tianlupan
 */
public class TinkerunRTask extends DefaultTask {

    static boolean  exists( String variantName){
        return new File(getRTxt(variantName)).exists()
    }

    static String getRTxt(String variantName){
        return TinkerunPlugin.TINKER_INTERMEDIATES+variantName+"/R.txt"
    }

    String variantName

    TinkerunRTask() {
        group = 'tinkerun'
    }


    @TaskAction
    def copyRTxt() {

        def destPath=project.file(getRTxt(variantName)).getParent()

        project.copy {
//            def fileNamePrefix = "${project.name}-${variant.baseName}"
//            def newFileNamePrefix = "${fileNamePrefix}"
//
//            def destPath = hasFlavors ? file("${bakPath}/${project.name}-${date}/${variant.flavorName}") : file("${bakPath}/${project.name}-${date}/${variant.name}")
//            from variant.outputs.outputFile
//            into destPath
//            rename { String fileName ->
//                fileName.replace("${fileNamePrefix}.apk", "${newFileNamePrefix}.apk")
//            }

//            def destPath =getRTxt(variantName)
//
//            from "${project.buildDir}/outputs/mapping/${variantName}/mapping.txt"
//            into destPath
//            rename { String fileName ->
//                fileName.replace("mapping.txt", "${newFileNamePrefix}-mapping.txt")
//            }

            from "${project.buildDir}/intermediates/symbols/${variantName}/R.txt"
            into destPath
//            rename { String fileName ->
//                fileName.replace("R.txt", "${newFileNamePrefix}-R.txt")
//            }
        }

    }
}