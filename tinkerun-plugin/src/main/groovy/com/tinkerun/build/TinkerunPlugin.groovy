package com.tinkerun.build;

import com.tinkerun.build.extension.*
import com.tinkerun.build.task.TinkerunCopyResourcesTask
import com.tinkerun.build.task.TinkerunDexTask
import com.tinkerun.build.task.TinkerunInstallTask
import com.tinkerun.build.task.TinkerunManifestTask
import com.tinkerun.build.task.TinkerunRTask
import com.tinkerun.build.task.TinkerunResourceIdTask
import com.tinkerun.build.task.TinkerunPatchTask
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

                // Add this proguard settings file to the list


//                variant.outputs.each { output ->
//                    setPatchNewApkPath(configuration, output, variant, tinkerPatchBuildTask)
//                    setPatchOutputFolder(configuration, output, variant, tinkerPatchBuildTask)
//                }

                // Create a task to add a build TINKER_ID to AndroidManifest.xml
                // This task must be called after "process${variantName}Manifest", since it
                // requires that an AndroidManifest.xml exists in `build/intermediates`.
                TinkerunManifestTask manifestTask = project.tasks.create("tinkerunProcess${variantName}Manifest", TinkerunManifestTask)

                if (variantOutput.processManifest.properties['manifestOutputFile'] != null) {
                    manifestTask.manifestPath = variantOutput.processManifest.manifestOutputFile
                } else if (variantOutput.processResources.properties['manifestFile'] != null) {
                    manifestTask.manifestPath = variantOutput.processResources.manifestFile
                }
                manifestTask.mustRunAfter variantOutput.processManifest

                variantOutput.processResources.dependsOn manifestTask

                //保存基顾包的R.txt
                TinkerunRTask rTask = project.tasks.create("tinkerunCopy${variantName}RTxt", TinkerunRTask)
                rTask.variantName=variantName
                def packageTask=getPackageTask(project, variantName)
                def assembleTask=getAssembleTask(project, variantName)
                assembleTask.dependsOn rTask
                rTask.mustRunAfter packageTask

                //resource id
                TinkerunResourceIdTask applyResourceTask = project.tasks.create("tinkerunProcess${variantName}ResourceId", TinkerunResourceIdTask)

                applyResourceTask.rTxtFile=TinkerunRTask.getRTxt(variantName)
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

                def resourcesFile=variantOutput.processResources.packageOutputFile

                //copy Resource
                TinkerunCopyResourcesTask copyResourceTask = project.tasks.create("tinkerunCopy${variantName}Resource", TinkerunCopyResourcesTask)
                copyResourceTask.variantName=variantName
                copyResourceTask.resourcesFile=resourcesFile
                copyResourceTask.dependsOn variantOutput.processResources


                TinkerunDexTask dexTask = project.tasks.create("tinkerun${variantName}Dex", TinkerunDexTask)
                dexTask.dependsOn  copyResourceTask

                def apkFile=project.file(TINKER_INTERMEDIATES+variantName+"/"+RESOURCES_FILE_NAME)

                TinkerunInstallTask installTask = project.tasks.create("tinkerunInstall${variantName}", TinkerunInstallTask)
                installTask.resourceApk=apkFile
                installTask.packageName=variant.applicationId
                installTask.dependsOn  dexTask

                TinkerunPatchTask patchTask = project.tasks.create("tinkerunPatch${variantName}", TinkerunPatchTask)
                patchTask.dependsOn  installTask

                def applyResourceTaskEnable=false
                if(configuration.patchResource) {
                    //违背了任务依赖解耦，FIXME
                    project.gradle.startParameter.taskNames.each { task ->
                        if (task.startsWith("tinkerun")) {
                            applyResourceTaskEnable=true
                        }
                    }
                }
                applyResourceTask.cleanMode=!applyResourceTaskEnable

            }

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

}
