@file:OptIn(OpenApiPreview::class)

import io.ktor.plugin.OpenApiPreview

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
}

group = "com.francoherrero.ai_agent_multiplatform"
version = "1.0.0"
application {
    mainClass.set("com.francoherrero.ai_agent_multiplatform.ApplicationKt")
    
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.contentServerNegotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.response.sse)
    implementation(libs.ktor.plugin.cors)
    implementation(libs.ktor.plugin.openapi)
    implementation(libs.ktor.plugin.swagger)
    implementation(libs.ktor.plugin.statuspage)
    implementation("io.github.smiley4:schema-kenerator-core:2.5.0")
    implementation("io.github.smiley4:schema-kenerator-reflection:2.5.0")
    implementation("io.github.smiley4:schema-kenerator-serialization:2.5.0")
    implementation("io.github.smiley4:schema-kenerator-swagger:2.5.0")
    implementation(libs.ktor.plugin.koog)
    implementation(libs.ktor.server.call.logging)

    // Db stuff
    implementation(libs.postgresql)
    implementation(libs.hikariCP)

    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.javaTime)


    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}

ktor {
    development = true
}