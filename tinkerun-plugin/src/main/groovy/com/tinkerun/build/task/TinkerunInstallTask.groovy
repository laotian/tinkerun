package com.tinkerun.build.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 *
 *TinkerunPatch
 * @author tianlupan
 */
public class TinkerunInstallTask extends DefaultTask {


    public static final String INSTALL_POSITION="/sdcard/tinkerun/"
    File resourceApk
    def packageName

    TinkerunInstallTask() {
        group = 'tinkerun'
    }

    @TaskAction
    def installToMachine() {
        project.logger.info("install patch, adb push")
        String adb=project.android.getAdbExe()
        project.exec {
            executable adb
            args "push" ,"${resourceApk.getAbsolutePath()}" ,"${INSTALL_POSITION}${packageName}/res/resources.apk"
        }
        //通知手机通
        project.exec {
            executable adb
            args "shell","am","startservice" ,"-n","${packageName}/com.tinkerun.patch.TinkerunDaemonService"
        }
    }

}