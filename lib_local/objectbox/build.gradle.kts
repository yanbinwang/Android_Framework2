import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("io.objectbox")
}

@Suppress("DEPRECATION")
android {
    namespace = "com.example.objectbox"

    compileSdk {
        version = release(libs.versions.compileSdkVersion.get().toInt())
    }

    defaultConfig {
        minSdk = libs.versions.minSdkVersion.get().toInt()
        testInstrumentationRunner = libs.versions.testInstrumentationRunner.get()
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["objectbox.modelPath"] = "$projectDir/src/main/assets/dao/evidence.json"
            }
        }
    }

//    kapt {
//        arguments {
//            arg("objectbox.modelPath", "$projectDir/src/main/assets/dao/evidence.json")
//        }
//    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    testImplementation(libs.junit)
    androidTestImplementation(libs.bundles.androidx.testing)
}