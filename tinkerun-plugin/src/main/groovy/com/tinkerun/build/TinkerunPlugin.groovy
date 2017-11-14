package com.tinkerun.build;

import com.tinkerun.build.extension.*
import com.tinkerun.build.task.TinkerunManifestTask;
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

                def instantRunTask = getInstantRunTask(project, variantName)
                if (instantRunTask != null) {
                    throw new GradleException(
                            "Tinkerun does not support instant run mode, please trigger build"
                                    + " by assemble${variantName} or disable instant run"
                                    + " in 'File->Settings...'."
                    )
                }

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

}
