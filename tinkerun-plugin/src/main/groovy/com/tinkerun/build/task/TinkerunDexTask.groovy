package com.tinkerun.build.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 *
 *TinkerunPatch
 * @author tianlupan
 */
public class TinkerunDexTask extends DefaultTask {

    TinkerunDexTask() {
        group = 'tinkerun'
    }

    @TaskAction
    def dex() {

    }

}