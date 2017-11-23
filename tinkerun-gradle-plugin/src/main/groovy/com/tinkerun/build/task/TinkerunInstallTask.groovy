package com.tinkerun.build.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 *
 *TinkerunPatch
 * @author tianlupan
 */
public class TinkerunInstallTask extends DefaultTask {


    public static final String INSTALL_POSITION="/data/local/tmp/tinkerun/"
    String patchApk
    def packageName
    TinkerunManifestTask manifestTask

    TinkerunInstallTask() {
        group = 'tinkerun'
    }

    @TaskAction
    def installToMachine() {
        project.logger.info("install patch, adb push")
        String adb=project.android.getAdbExe()
        //如果是补丁包模式，有此文件，如果是全量模式，为空
        if(patchApk!=null && new File(patchApk).exists()) {
            project.exec {
                executable adb
                args "push", patchApk, "${INSTALL_POSITION}${packageName}/patch.apk"
            }
            //通知手机通
            project.exec {
                executable adb
                args "shell","am","startservice" ,"-n","${packageName}/com.tinkerun.patch.TinkerunDaemonService"
            }
        }else{
            if(manifestTask.outputLaunchComponent!=null){
                project.exec {
                    executable adb
                    args "shell","am","start","-n",manifestTask.outputLaunchComponent
                }
            }else{
                project.logger.error("应用安装成功，但启动Activity类名未知，请手动启动");
            }

        }
    }

}