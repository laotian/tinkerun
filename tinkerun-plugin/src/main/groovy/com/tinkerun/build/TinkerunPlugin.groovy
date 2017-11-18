package com.tinkerun.build

import com.tinkerun.build.extension.*
import com.tinkerun.build.task.TinkerunDexTask
import com.tinkerun.build.task.TinkerunInstallTask
import com.tinkerun.build.task.TinkerunManifestTask
import com.tinkerun.build.task.TinkerunPatchSchemaTask
import com.tinkerun.build.task.TinkerunResourceIdTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

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

    static String getTargetDir(String variantName){
        return TINKER_INTERMEDIATES+variantName+"/"
    }

    static String getRTxt(String variantName){
        return getTargetDir(variantName)+R_TXT
    }

    @Override
    public void apply(Project project) {
        project.extensions.create("tinkerun", TinkerunExtension)
        if (!project.plugins.hasPlugin('com.android.application')) {
            throw new GradleException('Android Application plugin required')
        }

        def android = project.extensions.android
        project.afterEvaluate {
            TinkerunExtension configuration = project.tinkerun
            if(!configuration.enabled){
                project.logger.error("tinkerun tasks are disabled.")
                return
            }else{
                project.logger.error("tinkerun tasks are enabled.")
            }

            android.applicationVariants.all { variant ->

                def variantOutput = variant.outputs.first()
                def variantName = variant.name.capitalize()
                def variantData = variant.variantData
                def variantDirName=  variant.getDirName()

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

                project.logger.error("patchMode="+patchMode)

                def targetDir=project.file(getTargetDir(variantName)).getAbsolutePath()
                def basePropertyFile=project.file(getTargetDir(variantName)+BASE_PROPERTIES)
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

                if(!patchMode){
                    project.logger.error("patchMode ,assemble add work to do..")
                    variantOutput.assemble.doLast {

                        project.copy {
                            from "${project.buildDir}/intermediates/symbols/${variantDirName}/R.txt"
                            into   targetDir
                        }

                        project.copy{
                            from variant.outputs.outputFile
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

                        project.logger.error("已经保存基础包信息")
                    }
                }

                configuration.oldApk=targetDir+"/"+BASE_APK_NAME

                //resource id
                TinkerunResourceIdTask applyResourceTask = project.tasks.create("tinkerunProcess${variantName}ResourceId", TinkerunResourceIdTask)
                applyResourceTask.cleanMode=!patchMode
                applyResourceTask.rTxtFile=getRTxt(variantName)
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

                def applicationId=variant.applicationId

                //javac 与 dex
                TinkerunDexTask dexTask = project.tasks.create("tinkerun${variantName}Dex", TinkerunDexTask)
                dexTask.dependsOn  variantOutput.processResources
                dexTask.javaCompile= variant.getJavaCompiler()
                dexTask.lastBuildTime=Long.valueOf(LAST_BUILD)
                dexTask.applicationVariant=variant
                dexTask.targetDir=getTargetDir(variantName)

                def resourceApk=targetDir+"/"+RESOURCES_FILE_NAME
                def outputDir=targetDir+"/output/"
                def patchApk=outputDir+PATCH_APK_NAME
                //打包
                TinkerunPatchSchemaTask tinkerunPatchBuildTask = project.tasks.create("tinkerunPatch${variantName}", TinkerunPatchSchemaTask)
                tinkerunPatchBuildTask.signConfig = variantData.variantConfiguration.signingConfig
                tinkerunPatchBuildTask.outputFolder=outputDir
                tinkerunPatchBuildTask.targetDir=targetDir
                tinkerunPatchBuildTask.buildApkPath=resourceApk
                tinkerunPatchBuildTask.tinkerId=TINKER_ID
                tinkerunPatchBuildTask.resourcesFile=variantOutput.processResources.packageOutputFile
                tinkerunPatchBuildTask.configuration=configuration
                tinkerunPatchBuildTask.dependsOn dexTask

                //安装T
                TinkerunInstallTask installTask = project.tasks.create("tinkerunInstall${variantName}", TinkerunInstallTask)
                installTask.patchApk=patchApk
                installTask.packageName=variant.applicationId
                installTask.dependsOn  tinkerunPatchBuildTask



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

    Task getPackageTask(Project project,String variantName){
        String packageTaskName = "package${variantName}"
        return project.tasks.findByName(packageTaskName)
    }

    Task getAssembleTask(Project project,String variantName){
        String assemble = "assemble${variantName}"
        return project.tasks.findByName(assemble)
    }

     static String date() {
         return System.currentTimeMillis()+""
    }

}
