import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.INT
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

// todo ios version
val appVersion = "0.0.1"
val appVersionCode = 1

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.buildkonfig.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.completeKotlin)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.buildkonfig)
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("org.overengineer")
        }
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            freeCompilerArgs += listOf(
                "-framework", "Network"
            )
        }
        iosTarget.compilations["main"].cinterops {
            val network by creating {
                defFile("src/iosMain/def/network.def")
                packageName("platform.network")
            }
        }
    }
    
    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting
        
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            //implementation(libs.koin.annotations)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.napier)
            implementation(libs.voyager.navigator)
            implementation(libs.sonner)
            implementation(libs.material.icons.extended)
            implementation(libs.okio)
            implementation(libs.multiplatform.paths)
            implementation(libs.compose.shimmer)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor2)
            // paging
            implementation(libs.paging.common)
            implementation(libs.paging.compose.common)
        }
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            //implementation(libs.koin.androidx.compose)
            implementation(libs.ktor.client.okhttp) // OkHttp engine for Android
            implementation(libs.android.driver)
            implementation(libs.androidx.localbroadcastmanager) // todo: deprecated, refactor and remove
            // paging
            implementation(libs.androidx.paging.runtime)
            implementation(libs.androidx.paging.compose)
            implementation(libs.androidx.paging.rxjava3)
            // Exoplayer
            implementation(libs.androidx.media3.exoplayer)
            implementation(libs.androidx.media3.exoplayer.dash)
            implementation(libs.androidx.media3.ui)
            implementation(libs.androidx.media3.session)
            implementation(libs.androidx.media3.datasource.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin) // Darwin engine for iOS
            implementation(libs.native.driver)
            // paging
            implementation(libs.paging.runtime.uikit)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.java) // Java engine for Desktop
            implementation(libs.sqlite.driver)
            implementation(libs.vlcj)

            // Proguard
            // Eventually needed everywhere and not just desktop, check proguard
            implementation(libs.slf4j.api)
            implementation(libs.slf4j.simple)
        }
    }
}

android {
    namespace = "org.overengineer.talelistener"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.overengineer.talelistener"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = appVersionCode
        versionName = appVersion
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "org.overengineer.talelistener.MainKt"

        nativeDistributions {
            val osName = System.getProperty("os.name").lowercase()
            val targetFormats = when {
                osName.contains("win") -> listOf(TargetFormat.Exe)
                osName.contains("mac") -> listOf(TargetFormat.Dmg)
                osName.contains("nux") -> listOf(TargetFormat.Deb, TargetFormat.Rpm, TargetFormat.AppImage)
                else -> listOf(TargetFormat.Exe, TargetFormat.Deb, TargetFormat.Rpm, TargetFormat.AppImage, TargetFormat.Dmg) // fallback option
            }
            targetFormats(*targetFormats.toTypedArray())
            
            packageName = "TaleListener"
            packageVersion = appVersion
            copyright = "Copyright (C) 2024 - 2025 Julian Hackinger"
            vendor = "OverEngineer"

            modules("java.net.http", "java.sql")
            // gradle thinks we need this modules, but it works with the ones above for now
            //modules("java.instrument", "java.management", "java.net.http", "java.prefs", "java.sql", "jdk.unsupported", "jdk.crypto.ec")

            // Application/Title bar theme color in MacOS
            jvmArgs(
                "-Dapple.awt.application.appearance=system"
            )

            windows {
                menu = true
            }

            buildTypes.release.proguard {
                configurationFiles.from("rules.pro")
            }

            // todo icons
            /*macOS {
                iconFile.set(project.file("app_icon.icns"))
            }

            windows {
                iconFile.set(project.file("app_icon.png"))
            }

            linux {
                iconFile.set(project.file("app_icon.png"))
            }*/
        }
    }
}

buildkonfig {
    packageName = "org.overengineer.talelistener"
    exposeObjectWithName = "BuildConfig"

    defaultConfigs {
        buildConfigField(STRING, "packageName", "org.overengineer.talelistener")
        buildConfigField(STRING, "appName", "TaleListener")
        buildConfigField(STRING, "version", appVersion)
        buildConfigField(INT, "versionCode", appVersionCode.toString())
    }
}