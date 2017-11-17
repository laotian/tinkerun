package com.tinkerun.build.task

import com.android.build.gradle.api.ApplicationVariant
import com.android.builder.model.SourceProvider
import com.tinkerun.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.compile.JavaCompile

/**
 *
 *TinkerunPatch
 * @author tianlupan
 */
public class TinkerunDexTask extends DefaultTask {

    JavaCompile javaCompile
    ApplicationVariant applicationVariant
    long lastBuildTime

    TinkerunDexTask() {
        group = 'tinkerun'
    }

    @TaskAction
    def dex() {

        String dirName=applicationVariant.getDirName()
        String baseName=applicationVariant.getBaseName()
        List<File> keepJavaFile=new ArrayList<>()
        applicationVariant.sourceSets.each {
            Collection<File> dirs= it.getJavaDirectories();
            dirs.each {File dir->
                FileUtils.getOlderThan(lastBuildTime,dir,keepJavaFile);
            }
        }

        FileCollection original=javaCompile.source
        FileCollection now=  original.filter {
            it.lastModified()>lastBuildTime
        }
        now.each {File file->
            project.logger.error("file=="+file)
        }
        javaCompile.setSource(now)
////        JavaCompile javaCompile= variant.getJavaCompiler()
//        project.logger.error("sourceCompatibility ="+javaCompile.sourceCompatibility)
//        project.logger.error("compilerArgs="+javaCompile.options.compilerArgs)
//        javaCompile.source.asList().each {File s->
//            project.logger.error("source="+s.getAbsolutePath())
//        }
//        javaCompile.getClasspath().each {File f->
//            project.logger.error("getClasspath="+f.getAbsolutePath())
//        }

        project.logger.error("getDestinationDir="+javaCompile.getDestinationDir())

        javaCompile.setDidWork(false)
        javaCompile.execute()
        javaCompile.setSource(original)
        javaCompile.setDidWork(false)
        //TODO 恢复javaCompile的source

    }

}