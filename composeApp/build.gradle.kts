import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.koin.compiler)
}

// Generate BuildConfig with environment variables from local.properties
val generateBuildConfig by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/buildconfig")
    val localPropertiesFile = rootProject.file("local.properties")

    inputs.file(localPropertiesFile).optional()
    outputs.dir(outputDir)

    doLast {
        val localProperties = Properties().apply {
            if (localPropertiesFile.exists()) load(localPropertiesFile.inputStream())
        }

        val configFile = outputDir.get().file("BuildConfig.kt").asFile
        configFile.parentFile.mkdirs()
        configFile.writeText(
            """
            |package com.francoherrero.ai_agent_multiplatform
            |
            |object BuildConfig {
            |    const val SUPABASE_URL: String = "${localProperties.getProperty("SUPABASE_URL", "")}"
            |    const val SUPABASE_ANON_KEY: String = "${localProperties.getProperty("SUPABASE_ANON_KEY", "")}"
            |    const val SERVER_BASE_URL: String = "${localProperties.getProperty("SERVER_BASE_URL", "http://localhost:8080")}"
            |}
            """.trimMargin()
        )
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            linkerOpts("-lsqlite3")
        }
    }
    
    sourceSets {
        commonMain {
            kotlin.srcDir(generateBuildConfig)
        }

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.security.crypto)

        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.navigation.compose)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.kotlin.serialization)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kmp.datastore)

            // Koin DI
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)


            implementation(projects.shared)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.francoherrero.ai_agent_multiplatform"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.francoherrero.ai_agent_multiplatform"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
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

