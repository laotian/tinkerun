package com.tinkerun.build.task

import com.android.build.gradle.api.ApplicationVariant
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.compile.JavaCompile

/**
 *
 *JavaC
 *
 * FIXME
 *
 * BUG1:
 * 当前是根据文件修改时间为依据，增量编译这些类（假如classA);假如 classA.java定义了final类型的变量，被classB引用，此时classB并不会被重新编译，导致classB未变化；需要分析final field依赖图
 * @author tianlupan
 */
public class TinkerunJavacTask extends JavaCompile {

    String classesDir
    String LAST_BUILD
    Set<String> rClasses

    private static final String R_DIR="build/generated/source/r/"
    private static final String[] R_TYPES = ['anim','animator','array','attr','bool','color','dimen','drawable','id','integer','layout','mipmap','raw','string','style','styleable']

    TinkerunJavacTask() {
        group = 'tinkerun'
        rClasses=new HashSet<>()
    }


    void setVariant(ApplicationVariant variant){
        rClasses.clear()
        //javac task
        JavaCompile javaCompile=variant.javaCompiler

        FileCollection originalSource=javaCompile.source
        File originalDestination=javaCompile.getDestinationDir()
        FileCollection originalClassPath=javaCompile.getClasspath()

        def destination=project.file(classesDir)
        destination.delete()

        def androidJar= "${project.android.getSdkDirectory()}/platforms/${project.android.compileSdkVersion}/android.jar"
        FileCollection incrementalSource=  originalSource.filter {
            it.lastModified()>Long.valueOf(LAST_BUILD)
        }

        File rDir=project.file(R_DIR+variant.getDirName())
        incrementalSource.each {File file->
            if(file.toPath().startsWith(rDir.toPath()) && file.getName()=="R.java"){
                //如com/laotian/app
                String classDir= rDir.toPath().relativize(file.getParentFile().toPath())
                rClasses.add(classDir+"/R.class")
                R_TYPES.each {
                    rClasses.add(classDir+'/R$'+it+'.class')
                }
            }
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