package com.tinkerun.build.task

import com.tinkerun.build.TinkerunAppPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 *
 *TinkerunCopyAssets Incremental
 * @author tianlupan
 */
public class TinkerunCopyAssetsTask extends DefaultTask {
    TinkerunCopyAssetsTask(){
        group = 'tinkerun'
    }

    String LAST_BUILD

    //assets目录
    FileCollection assetFiles;

    //复制改动的assets到的目录
    File toFile;

    @InputFiles
    public FileCollection getAssetDirs(){
        return assetFiles
    }

    @OutputDirectory
    public File getToFile(){
        return toFile
    }

    @TaskAction
    def copyAssets(){
//        FileCollection incrementalSource=  assetDirs.filter {
//
//        }
//        if(toFile.exists()){
            project.file(toFile).deleteDir()
//        }

        project.copy{
            from assetFiles
//            filter{
//                it.lastModified()>Long.valueOf(LAST_BUILD)
//            }
            include { details -> details.file.lastModified()>Long.valueOf(LAST_BUILD) }
//            rename { String fileName ->
//                TinkerunAppPlugin.RESOURCES_FILE_NAME
//            }
            into getToFile()
        }
    }


}