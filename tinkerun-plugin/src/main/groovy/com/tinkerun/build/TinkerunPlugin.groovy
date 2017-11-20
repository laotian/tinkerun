package com.tinkerun.build

import com.tinkerun.build.extension.*
import com.tinkerun.build.task.TinkerunCopyResourcesTask
import com.tinkerun.build.task.TinkerunDexTask
import com.tinkerun.build.task.TinkerunInstallTask
import com.tinkerun.build.task.TinkerunJavacTask
import com.tinkerun.build.task.TinkerunManifestTask
import com.tinkerun.build.task.TinkerunMultidexConfigTask
import com.tinkerun.build.task.TinkerunPatchSchemaTask
import com.tinkerun.build.task.TinkerunResourceIdTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.compile.JavaCompile

/**
 *
 * Created by tianlupan on 2017/11/14.
 */

public class TinkerunPlugin implements Plugin<Project> {

    public static final String TINKER_INTERMEDIATES = "build/intermediates/tinkerun_intermediates/"

    public static final String RESOURCES_FILE_NAME="resources.apk"
    public static final String PATH_DEFAULT_OUTPUT="tinkerunPatch"
    public static final String BASE_APK_NAME="base.apk"
    public static final String PATCH_APK_NAME="patch_signed.apk"
    public static final String BASE_PROPERTIES="base.properties"
    public static final String R_TXT="R.txt"
    public static final String BASE_PROPERTIES_TINKER_ID="tinker_id";
    public static final String BASE_PROPERTIES_LAST_BUILD="last_build";
    public static final String CLASSES="classes"
    public static final String jarName="changed_classes.jar"
    public static final String dexName="changed_classes.dex"


    @Override
    public void apply(Project project) {
        project.extensions.create("tinkerun", TinkerunExtension)
        if (!project.plugins.hasPlugin('com.android.application')) {
            throw new GradleException('Android Application plugin required')
        }

        def android = project.extensions.android
        try {
            //close preDexLibraries
            android.dexOptions.preDexLibraries = false

            //open jumboMode
            android.dexOptions.jumboMode = true
            reflectAapt2Flag()
        } catch (Throwable e) {
            //no preDexLibraries field, just continue
        }

        project.afterEvaluate {
            TinkerunExtension configuration = project.tinkerun
            if(!configuration.enabled){
                project.logger.warn("tinkerun tasks are disabled.")
                return
            }

            android.applicationVariants.all { variant ->

                def variantOutput = variant.outputs.first()
                String variantName = variant.name.capitalize()
                def variantData = variant.variantData
                String variantDir=  variant.getDirName()

                //如果不能调试，比如Release模式，不启用Tinkerun
                if(!variant.getBuildType().buildType.isDebuggable()){
                    return
                }

                def instantRunTask = getInstantRunTask(project, variantName)
                if (instantRunTask != null) {
                    throw new GradleException(
                            "Tinkerun does not support instant run mode, please trigger build"
                                    + " by assemble${variantName} or disable instant run"
                                    + " in 'File->Settings...'."
                    )
                }

                //基础包模式或补丁模式
                def patchMode=false
                //违背了任务依赖解耦，FIXME
                project.gradle.startParameter.taskNames.each { task ->
                    if (task.startsWith("tinkerun")) {
                        patchMode=true
                    }
                }

                def targetDir=project.file(TINKER_INTERMEDIATES+variantDir).getAbsolutePath()
                def basePropertyFile=project.file(targetDir+"/"+BASE_PROPERTIES)
                // Add this proguard settings file to the list
                def TINKER_ID="TINKERUN_"+date()
                def LAST_BUILD=date()
                if(patchMode && basePropertyFile.exists()){
                    Properties properties = new Properties()
                    properties.load(basePropertyFile.newReader())
                    TINKER_ID=properties.getProperty(BASE_PROPERTIES_TINKER_ID)
                    LAST_BUILD=properties.getProperty(BASE_PROPERTIES_LAST_BUILD)
                }

                // Create a task to add a build TINKER_ID to AndroidManifest.xml
                // This task must be called after "process${variantName}Manifest", since it
                // requires that an AndroidManifest.xml exists in `build/intermediates`.
                TinkerunManifestTask manifestTask = project.tasks.create("tinkerunProcess${variantName}Manifest", TinkerunManifestTask)

                if (variantOutput.processManifest.properties['manifestOutputFile'] != null) {
                    manifestTask.manifestPath = variantOutput.processManifest.manifestOutputFile
                } else if (variantOutput.processResources.properties['manifestFile'] != null) {
                    manifestTask.manifestPath = variantOutput.processResources.manifestFile
                }
                manifestTask.tinkerId=TINKER_ID
                manifestTask.mustRunAfter variantOutput.processManifest
                variantOutput.processResources.dependsOn manifestTask

                //resource id
                TinkerunResourceIdTask applyResourceTask = project.tasks.create("tinkerunProcess${variantName}ResourceId", TinkerunResourceIdTask)
                applyResourceTask.cleanMode=!patchMode
                applyResourceTask.rTxtFile=targetDir+"/"+R_TXT
                if (variantOutput.processResources.properties['resDir'] != null) {
                    applyResourceTask.resDir = variantOutput.processResources.resDir
                } else if (variantOutput.processResources.properties['inputResourcesDir'] != null) {
                    applyResourceTask.resDir = variantOutput.processResources.inputResourcesDir.getFiles().first()
                }
                //let applyResourceTask run after manifestTask
                applyResourceTask.mustRunAfter manifestTask
                variantOutput.processResources.dependsOn applyResourceTask

                if (manifestTask.manifestPath == null || applyResourceTask.resDir == null) {
                    throw new RuntimeException("manifestTask.manifestPath or applyResourceTask.resDir is null.")
                }




                // Add this multidex proguard settings file to the list
                boolean multiDexEnabled = variantData.variantConfiguration.isMultiDexEnabled()

                if (multiDexEnabled) {
                    TinkerunMultidexConfigTask multidexConfigTask = project.tasks.create("tinkerunProcess${variantName}MultidexKeep", TinkerunMultidexConfigTask)
                    multidexConfigTask.applicationVariant = variant
                    multidexConfigTask.mustRunAfter manifestTask

                    // for java.io.FileNotFoundException: app/build/intermediates/multi-dex/release/manifest_keep.txt
                    // for gradle 3.x gen manifest_keep move to processResources task
                    multidexConfigTask.mustRunAfter variantOutput.processResources

                    def multidexTask = getMultiDexTask(project, variantName)
                    if (multidexTask != null) {
                        multidexTask.dependsOn multidexConfigTask
                    }
                    def collectMultiDexComponentsTask = getCollectMultiDexComponentsTask(project, variantName)
                    if (collectMultiDexComponentsTask != null) {
                        multidexConfigTask.mustRunAfter collectMultiDexComponentsTask
                    }
                }


                //javac task
                def classesDir=targetDir+"/"+CLASSES
                TinkerunJavacTask javacTask = project.tasks.create("tinkerunJavac${variantName}", TinkerunJavacTask)
                javacTask.classesDir=classesDir
                javacTask.LAST_BUILD=LAST_BUILD
                javacTask.copyFrom(variant.javaCompiler)

                if(!configuration.sourceSkipped){
                    javacTask.dependsOn variantOutput.processResources
                }

                //dex
                TinkerunDexTask dexTask = project.tasks.create("tinkerunDex${variantName}", TinkerunDexTask)
                dexTask.dependsOn  javacTask
                dexTask.baseName=variant.baseName
                dexTask.targetDir=targetDir
                dexTask.classesDir=classesDir

                //copy resources.ap_
                TinkerunCopyResourcesTask copyResourcesTask=project.tasks.create("tinkerunCopyResource${variantName}", TinkerunCopyResourcesTask)
                if (variantOutput.processResources.properties['packageOutputFile'] != null) {
                    copyResourcesTask.fromFile = variantOutput.processResources.packageOutputFile
                } else if (variantOutput.processResources.properties['resPackageOutputFolder'] != null) {
                    copyResourcesTask.fromFile = new File(variantOutput.processResources.resPackageOutputFolder, "resources-" + variant.name + ".ap_");
                }
//                copyResourcesTask.fromFile=variantOutput.processResources.packageOutputFile
                copyResourcesTask.toFile=project.file(targetDir+"/"+RESOURCES_FILE_NAME)
                copyResourcesTask.dependsOn dexTask

                //打包
                def resourceApk=targetDir+"/"+RESOURCES_FILE_NAME
                def outputDir=targetDir+"/output/"
                TinkerunPatchSchemaTask tinkerunPatchBuildTask = project.tasks.create("tinkerunPatch${variantName}", TinkerunPatchSchemaTask)
                tinkerunPatchBuildTask.signConfig = variantData.variantConfiguration.signingConfig
                tinkerunPatchBuildTask.outputFolder=outputDir
                tinkerunPatchBuildTask.targetDir=targetDir
                tinkerunPatchBuildTask.oldApk=targetDir+"/"+BASE_APK_NAME
                tinkerunPatchBuildTask.buildApkPath=resourceApk
                tinkerunPatchBuildTask.tinkerId=TINKER_ID
//                tinkerunPatchBuildTask.resourcesFile=variantOutput.processResources.packageOutputFile
                tinkerunPatchBuildTask.configuration=configuration
                tinkerunPatchBuildTask.dependsOn copyResourcesTask

                //安装
                TinkerunInstallTask installTask = project.tasks.create("tinkerunInstall${variantName}", TinkerunInstallTask)
                installTask.patchApk=outputDir+PATCH_APK_NAME
                installTask.packageName=variant.applicationId
                installTask.dependsOn  tinkerunPatchBuildTask


                //基础包模式下，保存基础包信息
                if(!patchMode){
                    variantOutput.assemble.doLast {
                        //清理
                        project.file(targetDir).deleteDir()
                        //复制R.txt
                        project.copy {
                            from "${project.buildDir}/intermediates/symbols/${variantDir}/R.txt"
                            into   targetDir
                        }
                        //复制APK
                        project.copy{
                            from variant.outputs.first().outputFile
                            rename { String fileName ->
                                BASE_APK_NAME
                            }
                            into targetDir
                        }
                        //保存TINKER_ID与最后生成时间
                        Properties properties = new Properties()
                        properties.setProperty(BASE_PROPERTIES_TINKER_ID,TINKER_ID)
                        properties.setProperty(BASE_PROPERTIES_LAST_BUILD,date())
                        properties.store(basePropertyFile.newWriter(),"Tinkerun base build snapshot")
                        project.logger.debug("Tinkerun已经保存基础包信息,Come on baby")
                    }
                }



            }

        }

    }


    void reflectAapt2Flag() {
        try {
            def booleanOptClazz = Class.forName('com.android.build.gradle.options.BooleanOption')
            def enableAAPT2Field = booleanOptClazz.getDeclaredField('ENABLE_AAPT2')
            enableAAPT2Field.setAccessible(true)
            def enableAAPT2EnumObj = enableAAPT2Field.get(null)
            def defValField = enableAAPT2EnumObj.getClass().getDeclaredField('defaultValue')
            defValField.setAccessible(true)
            defValField.set(enableAAPT2EnumObj, false)
        } catch (Throwable thr) {
            project.logger.error("relectAapt2Flag error: ${thr.getMessage()}.")
        }
    }

    Task getInstantRunTask(Project project, String variantName) {
        String instantRunTask = "transformClassesWithInstantRunFor${variantName}"
        return project.tasks.findByName(instantRunTask)
    }

    Task getCollectMultiDexComponentsTask(Project project, String variantName) {
        String collectMultiDexComponents = "collect${variantName}MultiDexComponents"
        return project.tasks.findByName(collectMultiDexComponents)
    }

    Task getMultiDexTask(Project project, String variantName) {
        String multiDexTaskName = "transformClassesWithMultidexlistFor${variantName}"
        return project.tasks.findByName(multiDexTaskName)
    }

//    Task getPackageTask(Project project,String variantName){
//        String packageTaskName = "package${variantName}"
//        return project.tasks.findByName(packageTaskName)
//    }

//    Task getAssembleTask(Project project,String variantName){
//        String assemble = "assemble${variantName}"
//        return project.tasks.findByName(assemble)
//    }

     static String date() {
         return System.currentTimeMillis()+""
    }

}
