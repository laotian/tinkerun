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
    String classesDir
    String baseName
    String dirName

    public static final String jarName="changed_classes.jar"
    public static final String dexName="changed_classes.dex"
    TinkerunDexTask() {
        group = 'tinkerun'
    }

    @TaskAction
    def dex() {
//
       dirName=applicationVariant.getDirName()
       baseName=applicationVariant.getBaseName()
////        List<File> keepJavaFile=new ArrayList<>()
////        applicationVariant.sourceSets.each {
////            Collection<File> dirs= it.getJavaDirectories();
////            dirs.each {File dir->
////                FileUtils.getOlderThan(lastBuildTime,dir,keepJavaFile);
////            }
////        }
//
//
//        FileCollection originalSource=javaCompile.source
//        File originalDestination=javaCompile.getDestinationDir()
//        FileCollection originalClassPath=javaCompile.getClasspath()
//
//
//
//
//////        JavaCompile javaCompile= variant.getJavaCompiler()
////        project.logger.error("sourceCompatibility ="+javaCompile.sourceCompatibility)
////        project.logger.error("compilerArgs="+javaCompile.options.compilerArgs)
////        javaCompile.source.asList().each {File s->
////            project.logger.error("source="+s.getAbsolutePath())
////        }
////        javaCompile.getClasspath().each {File f->
////            project.logger.error("getClasspath="+f.getAbsolutePath())
////        }
//
//        project.logger.error("getDestinationDir="+javaCompile.getDestinationDir())
//
//        classesDir=targetDir+"/classes/"
//        project.file(classesDir).delete()
//        project.logger.error("copy class file begin...")
//        project.copy{
//            from originalDestination
//            into project.file(classesDir)
//        }
//        originalDestination.delete()
//        project.logger.error("copy class file end...")
//        javaCompile.setClasspath(originalClassPath+project.files(classesDir))
//        FileCollection incrementalSource=  originalSource.filter {
//            it.lastModified()>lastBuildTime
//        }
//
//        project.logger.error("incrementalSource size="+incrementalSource.files.size())
//        incrementalSource.files.each {File file->
//            project.logger.error("incrementalSource file="+file.getAbsolutePath())
//        }
//
//        javaCompile.setSource(incrementalSource)
////        javaCompile.setDestinationDir(project.file(classesDir))
//        long currentBuildTime=System.currentTimeMillis();
//        javaCompile.setDidWork(false)
//        javaCompile.execute()
//        javaCompile.setSource(originalSource)
//        javaCompile.setClasspath(originalClassPath)
////        javaCompile.setDestinationDir(originalDestination)
//        javaCompile.setDidWork(false)


//        project.fileTree(dir:project.relativePath(javaCompile.getDestinationDir())).each {File file->
//            project.logger.error("classes="+file)
//            project.copy {
//                from file
//                into project.relativePath(project.file(classesDir+file.getAbsolutePath().substring(javaCompile.getDestinationDir().getAbsolutePath().length())).getParent())
//            }
//        }

        classesDir=targetDir+"/classes/"
        def task = project.tasks.create("tinkerunZip"  +baseName.capitalize() , Jar.class, new Action<Jar>() {

            @Override
            void execute(Jar zip) {
                zip.from(classesDir)
                zip.setDestinationDir(project.file(targetDir))
                zip.setArchiveName(jarName)
            }
        })
        task.execute()
//        project.logger.error("copy class file back begin...")
//        originalDestination.delete()
//        project.copy{
//            from  project.file(classesDir)
//            into originalDestination
//        }
//        project.logger.error("copy class file back end...")
        project.file(classesDir).delete()
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