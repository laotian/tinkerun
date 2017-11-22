## Tinkerun
Android Incremental Build Plugin (Like InstantRun,but by Tinker)

##速度
测试对象：apk生成大小70M的项目. 增量编译时间一般在15S以内

##支持内容
android app Module的Java代码及资源文件的快速增量编译与替换；

##不支持
1. android app Module里的Assets替换
2. android lib Module里的任何替换； 近期解决

##配置方式
参照项目内的APP

app.gradle  

VERSION_NAME对应最新版：比如1.9.1-beta2

```gradle
buildscript {
    repositories {
        jcenter()
        maven{
           url "https://dl.bintray.com/laotian/tinkerun"
        }
    }
    dependencies {
        classpath "com.tinkerun:tinkerun-gradle-plugin:${VERSION_NAME}"
    }
}

dependencies {
    debugCompile("com.tinkerun:tinkerun-android-lib:${VERSION_NAME}")
}

tinkerun{
   //if you want to forbidden tinkerun
   //enabled false
}

```

##使用方式
首先确保停用InstantRun；先正常编译并安装App，然后调用tinkerunInstall{Flavor}Debug. Tinkerun不能使用在Release模式下自被自动禁用

##已知bug
1.Preverified - classes 错误；计划beta3解决.
2.假如classA 里的final field1被classB引用，并且修改了classA里的field1并没有修改classB文件，则classB无法感应到这个变化；近期解决

##特别说明
1. Tinkerun的运行时采用tinker. 原理为生成tinker所使用的补丁，以实现热替换。gradle插件也大多来自[Tinker](https://github.com/Tencent/tinker).
2. 希望大家能多多参予，共同解决android编译慢的问题。
