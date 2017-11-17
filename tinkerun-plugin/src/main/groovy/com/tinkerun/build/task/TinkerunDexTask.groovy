package com.tinkerun.build.task

import com.android.build.gradle.api.ApplicationVariant
import com.android.builder.model.SourceProvider
import com.tinkerun.io.FileUtils
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.compile.JavaCompile

/**
 *
 *TinkerunPatch
 * @author tianlupan
 */
public class TinkerunDexTask extends DefaultTask {

    JavaCompile javaCompile
    ApplicationVariant applicationVariant
    String targetDir
    long lastBuildTime
    def classesDir
    String baseName
    String dirName

    public static final String jarName="changed_classes.jar"
    public static final String dexName="changed_classes.dex"
    TinkerunDexTask() {
        group = 'tinkerun'
    }

    @TaskAction
    def dex() {

       dirName=applicationVariant.getDirName()
       baseName=applicationVariant.getBaseName()
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

        long currentBuildTime=System.currentTimeMillis();
        javaCompile.setDidWork(false)
        javaCompile.execute()
        javaCompile.setSource(original)
        javaCompile.setDidWork(false)
        //TODO 恢复javaCompile的source
        classesDir=targetDir+"/classes/"
        project.file(classesDir).delete()
        project.fileTree(dir:project.relativePath(javaCompile.getDestinationDir())).each {File file->
            project.logger.error("classes="+file)
            project.copy {
                from file
                into project.relativePath(project.file(classesDir+file.getAbsolutePath().substring(javaCompile.getDestinationDir().getAbsolutePath().length())).getParent())
            }
        }

        def task = project.tasks.create("tinkerunZip"  +baseName.capitalize() , Jar.class, new Action<Jar>() {

            @Override
            void execute(Jar zip) {
//                project.logger.error("classes="+file)
                zip.from(classesDir)
                zip.setDestinationDir(project.file(targetDir))
                zip.setArchiveName(jarName)
            }
        })
        task.execute()
        File jarFile=project.file(targetDir+"/"+jarName)
        doDex(jarFile,project.android.getDexOptions())
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
                execArgs.add("--output=${project.file(targetDir).absolutePath}/${dexName}".toString())
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