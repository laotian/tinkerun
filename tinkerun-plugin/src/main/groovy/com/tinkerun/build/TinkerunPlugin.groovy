package com.tinkerun.build;

import com.tinkerun.build.extension.*;
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * Created by tianlupan on 2017/11/14.
 */

public class TinkerunPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.extensions.create("tinkerun", TinkerunExtension)
        if (!project.plugins.hasPlugin('com.android.application')) {
            throw new GradleException('Android Application plugin required')
        }

        project.afterEvaluate {
            TinkerunExtension configuration = project.tinkerun
            if(!configuration.enabled){
                project.logger.error("tinkerun tasks are disabled.")
                return
            }else{
                project.logger.error("tinkerun tasks are enabled.")
            }
        }

    }
}
