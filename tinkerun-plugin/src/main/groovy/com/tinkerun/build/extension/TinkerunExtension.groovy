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

    //原始的R.txt
    String applyResourceMapping;

    boolean  usingResourceMapping;

    String oldApk
    String newApk
    String outputFolder
    static final boolean useSign=true
    boolean  sourceSkipped


    /**
     * raw or jar, if you want to support below 4.0, you should use jar
     * default: raw, keep the orginal file type
     */
    String dexMode;

    /**
     * the dex file patterns, which dex or jar files will be deal to gen patch
     * such as [classes.dex, classes-*.dex, assets/multiDex/*.jar]
     */
    Iterable<String> pattern;
    /**
     * the loader files, they will be removed during gen patch main dex
     * and they should be at the primary dex
     * such as [com.tencent.tinker.loader.*, com.tinker.sample.MyApplication]
     */
    Iterable<String> loader;


    /**
     * Whether tinker should support component hotplug (add new component dynamically).
     * If this attribute is true, the component added in new apk will be available after
     * patch is successfully loaded. Otherwise an error would be announced when generating patch
     * on compile-time.
     *
     * <b>Notice that currently this feature is incubating and only support NON-EXPORTED Activity</b>
     */
    boolean supportHotplugComponent


    Iterable<String> sourcePattern

    private Map<String, String> fields

    public TinkerunExtension() {
        this.enabled=true;
        dexMode="jar"
        pattern=["changed_classes.dex"]
        loader=[]
        sourcePattern=["res/*", "assets/*", "resources.arsc", "AndroidManifest.xml"]
        sourceSkipped=false
        fields = [:]
    }

    def skipResource(){
        sourceSkipped=true
        sourcePattern=[]
    }

    Map<String, String> getFields() {
        return fields
    }

    void configField(String name, String value) {
        fields.put(name, value)
    }

}