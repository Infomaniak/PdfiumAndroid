buildscript {

    // Appending -SNAPSHOT is triggered by passing -Psnapshot to Gradle (used by the snapshot
    // publishing workflow), so the version doesn't need to be typed in by hand.
    val baseVersionName = "1.9.12"

    extra.apply {
        set("libMinSdk", 21)
        set("libCompileSdk", 37)
        set("libTargetSdk", 37)

        set("libGroupId", "com.infomaniak.pdfiumandroid")
        set("libVersionName", if (project.hasProperty("snapshot")) "$baseVersionName-SNAPSHOT" else baseVersionName)
        set("libArtifactId", "pdfiumandroid")
        set("ndkVersion", "28.2.13676358")
        set("javaVersion", JavaVersion.VERSION_17)
    }
}

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.nmcp)
    alias(libs.plugins.nmcp.aggregation)
    id("maven-publish")
    id("signing")
}

val libMinSdk: Int by rootProject.extra
val libCompileSdk: Int by rootProject.extra

val libGroupId: String by rootProject.extra
val libVersionName: String by rootProject.extra
val libArtifactId: String by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

group = libGroupId
version = libVersionName

android {
    namespace = "com.shockwave.pdfium"
    compileSdk = libCompileSdk
    ndkVersion = ndkVersion

    sourceSets {
        getByName("main") {
            jniLibs.directories.add("src/main/jni/lib")
        }
    }

    defaultConfig {
        minSdk = libMinSdk
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
    nmcpAggregation(project(":"))
}

nmcpAggregation {
    centralPortal {
        username = providers.gradleProperty("ossrhUsername")
            .orElse(providers.environmentVariable("ossrhUsername"))
            .orNull
        password = providers.gradleProperty("ossrhPassword")
            .orElse(providers.environmentVariable("ossrhPassword"))
            .orNull
        publishingType = "AUTOMATIC"
    }
}

fun getPropertyValue(propertyName: String): String? {
    if (project.hasProperty(propertyName)) return project.property(propertyName) as String
    return System.getenv(propertyName)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components.findByName("release")!!)
                groupId = libGroupId
                artifactId = libArtifactId
                version = libVersionName
                pom {
                    name.set("PdfiumAndroid")
                    description.set("Android Pdfium binding with Bitmap rendering")
                    url.set("https://github.com/Infomaniak/PdfiumAndroid")
                    licenses {
                        license {
                            name.set("Apache-2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/Infomaniak/PdfiumAndroid.git")
                        developerConnection.set("scm:git:ssh://github.com/Infomaniak/PdfiumAndroid.git")
                        url.set("https://github.com/Infomaniak/PdfiumAndroid")
                    }
                    developers {
                        developer {
                            id.set("Infomaniak")
                            name.set("Infomaniak Development Team")
                            email.set("mobile+libraries@infomaniak-dev.ch")
                            url.set("https://www.infomaniak.com/")
                        }
                    }
                }
            }
        }

        repositories {
            maven {
                name = "reposilite"
                url = uri(
                    if (libVersionName.endsWith("SNAPSHOT")) {
                        "https://maven.infomaniak.app/snapshots"
                    } else {
                        "https://maven.infomaniak.app/releases"
                    }
                )
                credentials {
                    username = getPropertyValue("reposiliteUsername")
                    password = getPropertyValue("reposilitePassword")
                }
            }
        }
    }
}

val gpgKeyId = providers.gradleProperty("GPG_key_id")
    .orElse(providers.environmentVariable("GPG_key_id"))
    .orNull
    ?.takeIf { it.isNotBlank() }
val gpgPrivateKey = providers.gradleProperty("GPG_private_key")
    .orElse(providers.environmentVariable("GPG_private_key"))
    .orNull
    ?.takeIf { it.isNotBlank() }
val gpgPassword = providers.gradleProperty("GPG_private_password")
    .orElse(providers.environmentVariable("GPG_private_password"))
    .orNull
    ?.takeIf { it.isNotBlank() }

signing {
    val isPublishTask = gradle.startParameter.taskNames.any {
        it.contains("ToReposiliteRepository") || it.contains("CentralPortal") || it.contains("CentralSnapshots")
    }
    if (gpgKeyId != null && gpgPrivateKey != null && gpgPassword != null) {
        useInMemoryPgpKeys(gpgKeyId, gpgPrivateKey.replace('#', '\n'), gpgPassword)
        sign(publishing.publications)
    } else if (isPublishTask) {
        error("Missing signing secrets: GPG_key_id, GPG_private_key, GPG_private_password")
    }
}
