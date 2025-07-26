pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        maven("https://jitpack.io")
        // 阿里云公共 Maven 镜像（替代 Maven Central）
        maven("https://maven.aliyun.com/repository/public")
        google()
        mavenCentral()
    }
}

/**
 * 1.三方库so库/jar包很多的情况下，在lib_local下建立库，方便后续升级替换
 * 2.三方库集成在github中，直接引用的情况下，如果和整体项目lib_common无关联（文字，样式）在lib_local下建立库，写一些对应的工具栏分装
 * 3.三方库集成在github中，直接引用的情况下，在lib_thirdparty下统一建立库，写一些对应的工具栏分装
 */
rootProject.name = "Android_Framework2"
include(":app")
include(":module_home")
include(":module_account")
include(":lib_thirdparty")
include(":lib_common")
include(":lib_framework")
include(":lib_local:glide")
include(":lib_local:topsheet")
include(":lib_local:amap_sdk")
include(":lib_local:greendao")
include(":lib_local:objectbox")
include(":lib_local:gallery")
include(":lib_debugging")//调试库，正式包不会被打入