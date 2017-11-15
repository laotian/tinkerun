package com.tinkerun.build.task

import com.tinkerun.build.TinkerunPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 *
 *TinkerunPatch
 * @author tianlupan
 */
public class TinkerunCopyResourcesTask extends DefaultTask {


    def variantName
    def resourcesFile;

    TinkerunCopyResourcesTask() {
        group = 'tinkerun'
    }


    @TaskAction
    def copyResource() {
        //TODO 改成增量任务
        project.copy {
            from resourcesFile
            rename{String fileName ->
                TinkerunPlugin.RESOURCES_FILE_NAME
            }
            into TinkerunPlugin.TINKER_INTERMEDIATES+variantName+"/"
        }
    }
}