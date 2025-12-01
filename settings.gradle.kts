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
        // 1. 优先使用国内镜像（加速下载）
        /**
         * 第三方开源项目的 “便捷分发仓库”
         * 不在 Maven Central/Google 仓库里的开源项目（比如 GitHub 上的个人库、小众库）。
         * 例：某个 GitHub 上的自定义 View 库、工具类库，作者没上传到中央仓库，只提供了 JitPack 地址。
         */
        maven("https://jitpack.io")
//        /**
//         * 阿里云镜像的 “Google 官方仓库”
//         * 所有 Google 官方发布的依赖：
//         * - Android 官方插件（如 com.android.application）
//         * - Google 服务库（如 play-services-*）
//         * - KSP（com.google.devtools.ksp，因为 KSP 是 Google 主导开发的）
//         */
//        maven("https://maven.aliyun.com/repository/google")
//        /**
//         * 阿里云镜像的 “Gradle 插件专用仓库”
//         * 仅 Gradle 插件（注意：是 “插件”，不是普通依赖）：
//         * - 部分非 Google 开发的 Gradle 插件（如旧版的一些第三方构建插件）。
//         */
//        maven("https://maven.aliyun.com/repository/gradle-plugin/")
        /**
         * 阿里云 “综合镜像仓库”
         * 几乎覆盖所有主流仓库的内容（是个 “大合集”）：
         * - 同步了 Maven Central（全球最大的中央仓库，大部分开源库在这里）
         * - 同步了 Google 仓库的大部分内容
         * - 同步了 Gradle 插件仓库的大部分内容
         */
        maven("https://maven.aliyun.com/repository/public")
        // 2. 官方仓库放最后（作为镜像的 fallback）
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
include(":lib_local:objectbox")
include(":lib_local:gallery")
include(":lib_local:klinechart")

// 调试库，正式包不会被打入
include(":lib_debugging")