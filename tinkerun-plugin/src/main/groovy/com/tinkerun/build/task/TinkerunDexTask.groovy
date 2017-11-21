package com.tinkerun.build.task

import com.android.build.gradle.api.ApplicationVariant
import com.android.builder.model.SourceProvider
import com.tinkerun.build.TinkerunPlugin
import com.tinkerun.io.FileUtils
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.compile.JavaCompile

import java.util.regex.Pattern

/**
 *
 *TinkerunPatch
 * @author tianlupan
 */
public class TinkerunDexTask extends DefaultTask {

    String targetDir
    String classesDir
    String baseName
    Set<String> rClasses
    public static final String jarName=TinkerunPlugin.jarName
    public static final String dexName=TinkerunPlugin.dexName

    /**
     * 有变化的Java类需要编译成dex
     */
    boolean foundChangedClass

     Closure zipExclude={
        if(it.isDirectory()){
            return false;
        }else if(rClasses.contains(classesDir.toPath().relativize(it.file.toPath()).toString())){
            return true
        }else{
            this.foundChangedClass =true
            return false
        }
    }

    TinkerunDexTask() {
        group = 'tinkerun'
        rClasses=new HashSet<>()
    }

//    @Input
//    public String getBaseName(){
//        return baseName;
//    }

    @InputDirectory
    public File getClassesDir(){
        return project.file(classesDir)
    }

    @OutputFile
    public File getDexFile(){
        return project.file(targetDir+"/"+dexName)
    }

    @TaskAction
    def dex() {
        foundChangedClass=false
        def task = project.tasks.create("tinkerunZip"  +baseName.capitalize() , Jar.class, new Action<Jar>() {

            @Override
            void execute(Jar zip) {
                zip.from(classesDir)
                zip.includeEmptyDirs=false
                zip.exclude(zipExclude)
                zip.setDestinationDir(project.file(targetDir))
                zip.setArchiveName(jarName)
            }
        })
        task.execute()
        getClassesDir().delete()
        File jarFile=project.file(targetDir+"/"+jarName)
        if(foundChangedClass) {
            //如果jar中没有任何class,调用dx.jar会报错
            doDex(jarFile, project.android.getDexOptions())
        }else{
            dexFile.delete()
        }
        jarFile.delete()
    }


    private void doDex(File classZip, def dexOptions) {
        def dexJar = "${project.android.getSdkDirectory()}/build-tools/${project.android.buildToolsVersion}/lib/dx.jar"
        def task = project.tasks.create("tinkerunDoDex" +baseName.capitalize(), JavaExec.class, new Action<JavaExec>() {
            @Override
            void execute(JavaExec javaExec) {
                ArrayList<String> execArgs = new ArrayList()
                execArgs.add("--dex")
                if (dexOptions.getJumboMode()) {
                    execArgs.add("--force-jumbo");
                }
                if (dexOptions.getIncremental()) {
                    execArgs.add("--incremental");
                    execArgs.add("--no-strict");
                }
                execArgs.add("--output=${getDexFile().getAbsolutePath()}".toString())
                execArgs.add(classZip.absolutePath)
                project.logger.info(execArgs.toString())
                javaExec.setClasspath(project.files(dexJar))
                javaExec.setMain("com.android.dx.command.Main")
                javaExec.setArgs(execArgs)
            }
        })
        task.execute()
    }

}