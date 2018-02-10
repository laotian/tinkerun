package com.tinkerun.build.task

import com.android.build.gradle.api.ApplicationVariant
import com.tinkerun.io.ReflectUtils
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.compile.CompileOptions
import org.gradle.api.tasks.compile.JavaCompile

/**
 *
 *JavaC
 *
 * FIXME
 *
 * BUG1:
 * 当前是根据文件修改时间为依据，增量编译这些类（假如classA);假如 classA.java定义了final类型的变量，被classB引用，此时classB并不会被重新编译，导致classB未变化；需要分析final field依赖图
 *
 *
 * 参照：android-apt
 * https://bitbucket.org/hvisser/android-apt/src/7bf3a58def8e3116dbbf1bf3723bdecd47b2e5f8/src/main/groovy/com/neenbedankt/gradle/androidapt/AndroidAptPlugin.groovy?at=default&fileviewer=file-view-default
 *
 * @author tianlupan
 */
public class TinkerunJavacTask extends JavaCompile {

    String classesDir
    String LAST_BUILD
    Set<String> rClasses
    JavaCompile javaCompile

    private static final String R_DIR="build/generated/source/r/"
    private static final String[] R_TYPES = ['anim','animator','array','attr','bool','color','dimen','drawable','id','integer','layout','mipmap','raw','string','style','styleable']

    TinkerunJavacTask() {
        group = 'tinkerun'
        rClasses=new HashSet<>()
    }


    void setVariant(ApplicationVariant variant){
        rClasses.clear()
        //javac task
        javaCompile=variant.hasProperty('javaCompiler') ? variant.javaCompiler : variant.javaCompile

        FileCollection originalSource=javaCompile.source
        File originalDestination=javaCompile.getDestinationDir()
        FileCollection originalClassPath=javaCompile.getClasspath()

        def androidJar= "${project.android.getSdkDirectory()}/platforms/${project.android.compileSdkVersion}/android.jar"
        FileCollection incrementalSource=  originalSource.filter {
            it.lastModified()>Long.valueOf(LAST_BUILD)
        }

        File rDir=project.file(R_DIR+variant.getDirName())
        originalSource.each {File file->
            if(file.toPath().startsWith(rDir.toPath()) && file.getName()=="R.java"){
                //如com/laotian/app
                String classDir= rDir.toPath().relativize(file.getParentFile().toPath())
                rClasses.add(classDir+ File.separator+ "R.class")
                R_TYPES.each {
                    rClasses.add(classDir+ File.separator+ 'R$'+it+'.class')
                }
            }
        }
        setSource(incrementalSource)
        setDestinationDir(project.file(classesDir))
        setClasspath(originalClassPath+project.files(originalDestination)+project.files(androidJar))
        targetCompatibility=javaCompile.targetCompatibility
        sourceCompatibility=javaCompile.sourceCompatibility

        //如果配置期就设置options的话，当com.tinkerun.app 放在android-apt 插件的后面，
        //会导致无法获取annotation processor设置
        doFirst {
            ReflectUtils.copyFrom(CompileOptions.class,javaCompile.options,options)
        }
    }


}