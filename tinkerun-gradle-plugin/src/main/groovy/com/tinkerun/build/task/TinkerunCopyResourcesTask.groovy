package com.tinkerun.build.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import com.tinkerun.build.TinkerunAppPlugin

/**
 *
 *TinkerunPatch
 * @author tianlupan
 */
public class TinkerunCopyResourcesTask extends DefaultTask {
    TinkerunCopyResourcesTask(){
        group = 'tinkerun'
    }

    File fromFile;
    File toFile;

    @InputFile
    public File getFromFile(){
        return fromFile
    }

    @OutputFile
    public File getToFile(){
        return toFile
    }

    @TaskAction
    def copyResource(){
        project.copy{
            from getFromFile()
            rename { String fileName ->
                TinkerunAppPlugin.RESOURCES_FILE_NAME
            }
            into getToFile().getParent()
        }
    }


}