package com.tinkerun.build.extension

import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * The configuration properties.
 *
 * @author zhangshaowen
 */

public class TinkerunExtension {
    //开启Tinkerun
    boolean  enabled;

    public TinkerunExtension() {
        this.enabled=true;
    }

}