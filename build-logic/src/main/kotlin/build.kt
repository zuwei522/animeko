/*
 * Copyright (C) 2024-2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 \u8BB8\u53EF\u8BC1\u7684\u7EA6\u675F, \u53EF\u4EE5\u5728\u4EE5\u4E0B\u94FE\u63A5\u627E\u5230\u8BE5\u8BB8\u53EF\u8BC1.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link:
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetsContainer
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmAndroidCompilation
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.io.File
import kotlin.jvm.optionals.getOrNull

fun Project.sharedAndroidProguardRules(): Array<File> {
    val dir = project(":app:shared").projectDir
    return listOf(
        dir.resolve("proguard-rules.pro"),
        dir.resolve("kotlinx-coroutines.pro"),
        dir.resolve("kotlinx-serialization.pro"),
        dir.resolve("proguard-rules-keep-names.pro"),
    ).filter {
        it.exists()
    }.toTypedArray().also {
        check(it.isNotEmpty()) {
            "No proguard rules found in $dir"
        }
    }
}

val testOptInAnnotations = arrayOf(
    "kotlin.ExperimentalUnsignedTypes",
    "kotlin.time.ExperimentalTime",
    "io.ktor.util.KtorExperimentalAPI",
    "kotlin.io.path.ExperimentalPathApi",
    "kotlinx.coroutines.ExperimentalCoroutinesApi",
    "kotlinx.serialization.ExperimentalSerializationApi",
    "me.him188.ani.utils.platform.annotations.TestOnly",
    "androidx.compose.ui.test.ExperimentalTestApi",
)

val optInAnnotations = arrayOf(
    "kotlin.contracts.ExperimentalContracts",
    "kotlin.experimental.ExperimentalTypeInference",
    "kotlinx.serialization.ExperimentalSerializationApi",
    "kotlinx.coroutines.ExperimentalCoroutinesApi",
    "kotlinx.coroutines.FlowPreview",
    "androidx.compose.foundation.layout.ExperimentalLayoutApi",
    "androidx.compose.foundation.ExperimentalFoundationApi",
    "androidx.compose.material3.ExperimentalMaterial3Api",
    "androidx.compose.ui.ExperimentalComposeUiApi",
    "org.jetbrains.compose.resources.ExperimentalResourceApi",
    "kotlin.ExperimentalStdlibApi",
    "androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi",
    "androidx.compose.animation.ExperimentalSharedTransitionApi",
    "androidx.paging.ExperimentalPagingApi",
    "kotlin.ExperimentalSubclassOptIn",
    "kotlin.uuid.ExperimentalUuidApi",
    "androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
    "kotlin.time.ExperimentalTime",
)

// ContextParameters is stable since Kotlin 2.4 and no longer needs a language feature flag.

fun Project.configureKotlinOptIns() {
    val sourceSets = kotlinSourceSets ?: return
    sourceSets.configureEach {
        configureKotlinOptIns()
    }

    val libs = versionCatalogLibs()
    val (major, minor) = libs["kotlin"].split('.')
    val kotlinVersion = KotlinVersion.valueOf("KOTLIN_${major}_${minor}")

    val options = kotlinCommonCompilerOptions()
    options.apply {
        languageVersion.set(kotlinVersion)
    }
    // ksp task extends KotlinCompile
    project.tasks.withType(KotlinCompile::class.java).configureEach {
        @Suppress("MISSING_DEPENDENCY_SUPERCLASS_IN_TYPE_ARGUMENT")
        compilerOptions.languageVersion.set(kotlinVersion)
    }
}

private fun Project.versionCatalogLibs(): VersionCatalog =
    project.extensions.getByType<VersionCatalogsExtension>().named("libs")

private operator fun VersionCatalog.get(name: String): String = findVersion(name).get().displayName

fun VersionCatalog.getLibrary(name: String): String = findLibrary(name).getOrNull()?.orNull?.toString()
    ?: error("Library $name not found in version catalog")

private fun Project.kotlinCommonCompilerOptions(): KotlinCommonCompilerOptions = when (val ext = kotlinExtension) {
    is KotlinJvmProjectExtension -> ext.compilerOptions
    is KotlinAndroidProjectExtension -> ext.compilerOptions
    is KotlinMultiplatformExtension -> ext.compilerOptions
    else -> error("Unsupported kotlinExtension: ${ext::class}")
}

fun KotlinSourceSet.configureKotlinOptIns() {
    languageSettings.progressiveMode = true
    optInAnnotations.forEach { a ->
        languageSettings.optIn(a)
    }
    if (name.contains("test", ignoreCase = true)) {
        testOptInAnnotations.forEach { a ->
            languageSettings.optIn(a)
        }
    }
}

val Project.DEFAULT_JVM_TOOLCHAIN_VENDOR
    get() = getPropertyOrNull("jvm.toolchain.vendor")
        // "any" \u8868\u793A\u4E0D\u9650\u5236 vendor (JvmVendorSpec.matching \u662F\u5927\u5C0F\u5199\u654F\u611F\u7684 substring \u5339\u914D,
        // "any" \u5339\u914D\u4E0D\u5230 "Eclipse Temurin", \u56E0\u6B64\u6B64\u5904\u5BF9 any/blank \u8FD4\u56DE null \u8868\u793A\u4E0D\u9650 vendor)
        ?.takeUnless { it.equals("any", ignoreCase = true) || it.isBlank() }
        ?.let { JvmVendorSpec.matching(it) }

private fun Project.getProjectPreferredJvmTargetVersion() =
    JavaVersion.toVersion(getPropertyOrNull("jvm.toolchain.version")?.toInt() ?: 21)

fun Project.configureJvmTarget() {
    val ver = getProjectPreferredJvmTargetVersion()
    logger.info("JVM target for project ${this.path} is: $ver")
    val target = JvmTarget.fromTarget(ver.toString())

    // \u6211\u4E5F\u4E0D\u77E5\u9053\u5230\u5E95\u8BBE\u7F6E\u8C01\u5C31\u591F\u4E86, \u53CD\u6B63\u90FD\u8BBE\u7F6E\u4E86

    tasks.withType(KotlinJvmCompile::class.java).configureEach {
        compilerOptions.jvmTarget.set(target)
    }

    tasks.withType(KotlinCompile::class.java).configureEach {
        compilerOptions.jvmTarget.set(target)
    }

    tasks.withType(JavaCompile::class.java).configureEach {
        sourceCompatibility = ver.toString()
        targetCompatibility = ver.toString()
    }

    extensions.findByType(KotlinProjectExtension::class)?.apply {
        jvmToolchain {
            vendor.set(DEFAULT_JVM_TOOLCHAIN_VENDOR)
            languageVersion.set(JavaLanguageVersion.of(ver.getMajorVersion()))
        }
    }

    extensions.findByType(JavaPluginExtension::class)?.apply {
        toolchain {
            vendor.set(DEFAULT_JVM_TOOLCHAIN_VENDOR)
            languageVersion.set(JavaLanguageVersion.of(ver.getMajorVersion()))
            sourceCompatibility = ver
            targetCompatibility = ver
        }
    }

    // \u914D\u7F6E\u671F\u8BFB\u4E00\u6B21, \u907F\u514D\u6BCF\u4E2A compilation \u56DE\u8C03\u91CC\u91CD\u590D\u8BFB local.properties.
    val renderInternalDiagnosticNames =
        getLocalProperty("ani.kotlin.render-internal-diagnostic-names")?.toBooleanStrict() == true

    withKotlinTargets {
        it.compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    if (renderInternalDiagnosticNames) {
                        freeCompilerArgs.add("-Xrender-internal-diagnostic-names")
                    }
                    freeCompilerArgs.add("-Xdont-warn-on-error-suppression")
                    freeCompilerArgs.add("-Xannotation-target-all")
                    freeCompilerArgs.add("-Xannotation-default-target=param-property")
                }
            }
            if (this is KotlinJvmAndroidCompilation) {
                compileTaskProvider.configure {
                    compilerOptions {
                        jvmTarget.set(target)
                    }
                }
            }
        }
    }

    extensions.findByType(JavaPluginExtension::class.java)?.run {
        sourceCompatibility = ver
        targetCompatibility = ver
    }
}

fun Project.configureEncoding() {
    tasks.withType(JavaCompile::class.java).configureEach {
        options.encoding = "UTF8"
    }
}

fun Project.configureKotlinTestSettings() {
    tasks.withType(Test::class).configureEach {
        useJUnitPlatform()
    }

    val libs = versionCatalogLibs()
    val b = "Auto-set for project '${project.path}'. (configureKotlinTestSettings)"

    allKotlinTargets().configureEach {
        if (this !is KotlinJvmTarget) return@configureEach
        testRuns.configureEach { executionTask.configure { useJUnitPlatform() } }

        // \u4ECE target \u4FA7\u9A71\u52A8, \u514D\u53BB\u4ECE\u6E90\u96C6\u540D\u5B57\u53CD\u63A8 target.
        val targetName = name
        kotlinSourceSets?.matching { it.name == "${targetName}Test" }
            ?.configureEach { configureJvmTest(b) }
    }

    when {
        isKotlinJvmProject -> {
            dependencies {
                "testImplementation"(kotlin("test-junit5"))?.because(b)

                "testImplementation"(libs.getLibrary("junit5-jupiter-api"))?.because(b)
                "testRuntimeOnly"(libs.getLibrary("junit5-jupiter-engine"))?.because(b)
            }
        }

        isKotlinMpp -> {
            val sourceSets = kotlinSourceSets ?: return

            // \u4E09\u4E2A\u6E90\u96C6\u90FD\u8981\u62FF\u5230 JVM \u6D4B\u8BD5\u4F9D\u8D56, \u5C11\u4E86 androidTest \u4F1A\u4E22\u6389\u6574\u5957 junit5.
            // \u7528 live filtered collection \u6CE8\u518C, \u6E90\u96C6\u4F55\u65F6\u521B\u5EFA\u90FD\u80FD\u547D\u4E2D, \u56E0\u6B64\u4E0D\u9700\u8981 afterEvaluate.
            sourceSets.matching {
                it.name == "androidTest" || it.name == "androidHostTest" || it.name == "androidDeviceTest"
            }.configureEach { configureJvmTest(b) }

            // runner \u53EA\u52A0\u5728\u53F6\u5B50\u6E90\u96C6\u4E0A.
            sourceSets.matching { it.name == "androidHostTest" || it.name == "androidDeviceTest" }
                .configureEach {
                    dependencies {
                        implementation(libs.getLibrary("androidx-test-runner"))
                        implementation(libs.getLibrary("junit5-android-test-core"))
                        runtimeOnly(libs.getLibrary("junit5-android-test-runner"))
                    }
                }

            sourceSets.matching { it.name == "commonTest" }.configureEach {
                dependencies {
                    implementation(kotlin("test-annotations-common"))?.because(b)
                }
            }
        }
    }
}

/**
 * \u7ED9 Compose + Android KMP Library \u81EA\u52A8\u52A0\u4E0A ui-tooling.
 * androidRuntimeClasspath \u8981\u7B49 android target \u58F0\u660E\u540E\u624D\u5B58\u5728, \u6240\u4EE5\u7528 matching{} \u5EF6\u8FDF\u6CE8\u518C.
 */
fun Project.configureComposePreviewToolingDependency() {
    val notation = versionCatalogLibs().getLibrary("compose-ui-tooling")
    val reason =
        "Automatically add org.jetbrains.compose.ui:ui-tooling dependency to Compose & Android KMP Library."
    configurations.matching { it.name == "androidRuntimeClasspath" }.configureEach {
        dependencies.add(
            project.dependencies.create(notation).apply { because(reason) },
        )
    }
}

fun KotlinSourceSet.configureJvmTest(because: String) {
    val libs = project.versionCatalogLibs()
    dependencies {
        implementation(kotlin("test-junit5"))?.because(because)

        // also see above for androidInstrumentedTest
        implementation(libs.getLibrary("junit5-jupiter-api"))?.because(because)
        runtimeOnly(libs.getLibrary("junit5-jupiter-engine"))?.because(because)

        // TODO: if we need to run junit4 tests (especially ui tests), add this.
//        runtimeOnly("junit:junit:4.13.2")?.because(because)
//        runtimeOnly("org.junit.vintage:junit-vintage-engine:${JUNIT_VERSION}")?.because(because)
    }
}


fun Project.withKotlinTargets(fn: (KotlinTarget) -> Unit) {
    extensions.findByType(KotlinTargetsContainer::class.java)?.let { kotlinExtension ->
        // find all compilations given sourceSet belongs to
        kotlinExtension.targets
            .configureEach {
                fn(this)
            }
    }
}
