/*
 * Copyright (C) 2024-2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 \u8BB8\u53EF\u8BC1\u7684\u7EA6\u675F, \u53EF\u4EE5\u5728\u4EE5\u4E0B\u94FE\u63A5\u627E\u5230\u8BE5\u8BB8\u53EF\u8BC1.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

import java.util.Properties

plugins {
    `kotlin-dsl`
}

// included build \u4E0D\u7EE7\u627F\u4E3B\u6784\u5EFA\u7684 gradle.properties, jvm.toolchain.* \u5FC5\u987B\u663E\u5F0F\u4ECE\u4ED3\u5E93\u6839\u8BFB,
// \u5426\u5219 toolchain \u4F1A\u6084\u6084\u56DE\u843D\u5230\u9ED8\u8BA4 JDK. (settingsDirectory \u662F build-logic/ \u81EA\u5DF1, ".." \u624D\u662F\u4ED3\u5E93\u6839)
fun rootProperties(fileName: String): Provider<Properties> =
    providers.fileContents(layout.settingsDirectory.dir("..").file(fileName)).asText
        .map { text -> Properties().apply { text.reader().use { load(it) } } }

val rootLocalProperties = rootProperties("local.properties")
val rootGradleProperties = rootProperties("gradle.properties")

fun toolchainProperty(name: String): Provider<String> =
    rootLocalProperties.map { it.getProperty(name) }
        .orElse(rootGradleProperties.map { it.getProperty(name) })
        .orElse(providers.gradleProperty(name))

kotlin {
    jvmToolchain {
        // \u672C\u5730\u6784\u5EFA\u73AF\u5883: \u79FB\u9664 vendor \u9650\u5236, \u4F7F\u7528\u4EFB\u610F JDK 21
        // toolchainProperty("jvm.toolchain.vendor").orNull?.let { vendor.set(JvmVendorSpec.matching(it)) }
        toolchainProperty("jvm.toolchain.version").orNull?.let { languageVersion.set(JavaLanguageVersion.of(it)) }
    }
    compilerOptions {
        optIn.add("org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi")
    }
}

dependencies {
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.awssdk.s3)
}

dependencies {
    api(gradleApi())
    api(gradleKotlinDsl())

    api(libs.kotlin.gradle.plugin) {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")
        exclude("org.jetbrains.kotlin", "kotlin-reflect")
    }

    api(libs.android.gradle.plugin)
    api(libs.atomicfu.gradle.plugin)
    api(libs.android.application.gradle.plugin)
    api(libs.android.kotlin.multiplatform.library.gradle.plugin)
    api(libs.compose.multiplatfrom.gradle.plugin)
    api(libs.kotlin.compose.compiler.gradle.plugin)
    api(libs.kotlin.native.cocoapods.gradle.plugin)
    api(libs.mannodermaus.android.junit5.gradle.plugin)
    api(libs.compose.stability.analyzer.gradle.plugin)
    implementation(kotlin("script-runtime"))
    implementation(libs.snakeyaml)
}
