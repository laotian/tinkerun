package com.tinkerun.build.task

import com.tinkerun.build.TinkerunPlugin
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile

/**
 *
 *JavaC
 * @author tianlupan
 */
public class TinkerunJavacTask extends JavaCompile {

    String classesDir
    String LAST_BUILD

    TinkerunJavacTask() {
        group = 'tinkerun'
    }
    
    void  copyFrom(JavaCompile javaCompile){
        //javac task
       
        FileCollection originalSource=javaCompile.source
        File originalDestination=javaCompile.getDestinationDir()
        FileCollection originalClassPath=javaCompile.getClasspath()

        def destination=project.file(classesDir)
        destination.delete()

        def androidJar= "${project.android.getSdkDirectory()}/platforms/${project.android.compileSdkVersion}/android.jar"
        FileCollection incrementalSource=  originalSource.filter {
            it.lastModified()>Long.valueOf(LAST_BUILD)
        }
        setSource(incrementalSource)
        setDestinationDir(destination)
        setClasspath(originalClassPath+project.files(originalDestination)+project.files(androidJar))
        options.compilerArgs=javaCompile.options.compilerArgs
        options.sourcepath=javaCompile.options.sourcepath
        options.debug=javaCompile.options.debug
        targetCompatibility=javaCompile.targetCompatibility
        sourceCompatibility=javaCompile.sourceCompatibility
        options.bootClasspath=javaCompile.options.bootClasspath
    }

}