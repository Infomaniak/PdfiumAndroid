buildscript {

    extra.apply {
        set("libMinSdk", 21)
        set("libCompileSdk", 34)
        set("libTargetSdk", 34)

        set("libGroupId", "com.github.infomaniak")
        set("libVersionName", "1.9.6-vte")
        set("libArtifactId", "pdfiumandroid")
        set("ndkVersion", "28.2.13676358")
        set("javaVersion", JavaVersion.VERSION_17)
    }
}

plugins {
    alias(libs.plugins.androidLibrary)
    id("maven-publish")
}

val libMinSdk: Int by rootProject.extra
val libCompileSdk: Int by rootProject.extra
val libTargetSdk: Int by rootProject.extra

val libGroupId: String by rootProject.extra
val libVersionName: String by rootProject.extra
val libArtifactId: String by rootProject.extra
val ndkVersion: String by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

android {
    namespace = "com.shockwave.pdfium"
    compileSdk = libCompileSdk
    ndkVersion = ndkVersion

    externalNativeBuild {
        ndkBuild {
            path = File("src/main/jni/Android.mk")
        }
    }

    packaging {
        resources {
            jniLibs.pickFirsts.addAll(
                listOf(
                    "lib/armeabi-v7a/libc++_shared.so",
                    "lib/arm64-v8a/libc++_shared.so",
                    "lib/x86_64/libc++_shared.so",
                    "lib/x86/libc++_shared.so",

                    "lib/armeabi-v7a/libjniPdfium.so",
                    "lib/arm64-v8a/libjniPdfium.so",
                    "lib/x86_64/libjniPdfium.so",
                    "lib/x86/libjniPdfium.so",

                    "lib/armeabi-v7a/libmodpdfium.so",
                    "lib/arm64-v8a/libmodpdfium.so",
                    "lib/x86_64/libmodpdfium.so",
                    "lib/x86/libmodpdfium.so"
                )
            )
        }
    }

    defaultConfig {
        minSdk = libMinSdk
        targetSdk = libTargetSdk
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    buildFeatures {
        buildConfig = true
    }
}

repositories {
    google()
    // mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.collection)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components.findByName("release")!!)
                groupId = libGroupId
                artifactId = libArtifactId
                version = libVersionName
            }
        }
    }
}
