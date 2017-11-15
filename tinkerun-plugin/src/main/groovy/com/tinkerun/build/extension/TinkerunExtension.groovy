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

    //开启资源
    boolean patchResource=true

    //原始的R.txt
    String applyResourceMapping;

    boolean  usingResourceMapping;

    public TinkerunExtension() {
        this.enabled=true;
    }

}