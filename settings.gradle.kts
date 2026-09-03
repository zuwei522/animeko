/*
 * Copyright (C) 2024-2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link:
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

import java.util.Properties

rootProject.name = "animeko"

pluginManagement {
    // \u7EA6\u5B9A\u63D2\u4EF6\u6765\u81EA build-logic; \u5FC5\u987B\u5728 settings \u91CC includeBuild, plugins {} \u624D\u89E3\u6790\u5F97\u5230 `ani.*`.
    includeBuild("build-logic")

    repositories {
        // \u672C\u5730 Maven \u4ED3\u5E93 (\u6301\u4E45\u5316\u76EE\u5F55): \u5B58\u653E\u4ECE mvnrepository \u624B\u52A8\u4E0B\u8F7D\u7684 google-services \u63D2\u4EF6
        // (com.google.gms:google-services:4.4.2), \u7528\u4E8E\u7ED5\u8FC7 dl.google.com \u5BF9\u8BE5\u63D2\u4EF6 artifact \u7684 404 \u95EE\u9898.
        maven {
            url = uri(rootDir.resolve("..").resolve("dev-env").resolve("m2-repo").absolutePath)
        }
        gradlePluginPortal()
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev") // Compose Multiplatform pre-release versions
    }
}

dependencyResolutionManagement {
    // \u4ED3\u5E93\u7B56\u7565\u5C5E\u4E8E settings; FAIL_ON_PROJECT_REPOS \u9632\u6B62\u5B50\u9879\u76EE\u518D\u81EA\u5DF1\u52A0\u4ED3\u5E93.
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    @Suppress("UnstableApiUsage")
    repositories {
        // mavenLocal \u7684\u4F4D\u7F6E\u4E0D\u80FD\u52A8: \u672C\u5730 mediamp / anitorrent \u8C03\u8BD5\u6784\u5EFA\u4F9D\u8D56\u5B83\u6392\u5728\u8FD9\u91CC.
        mavenCentral()
        google()
        mavenLocal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://androidx.dev/storage/compose-compiler/repository/")
        maven("https://jogamp.org/deployment/maven")
    }
    versionCatalogs {
        create("anitorrentLibs") {
            from("org.openani.anitorrent:catalog:0.2.0")
        }

    }
}

plugins {
    id("com.gradle.develocity") version "4.3.2"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

develocity {
    buildScan {
        // Keep scans opt-in via --scan and only allow publication from CI.
        publishing.onlyIf { !System.getenv("CI").isNullOrEmpty() }
        termsOfUseUrl = "https://gradle.com/terms-of-service"
        termsOfUseAgree = "yes"
        uploadInBackground = System.getenv("CI").isNullOrEmpty()
    }
}

fun includeProject(projectPath: String, dir: String? = null) {
    include(projectPath)
    if (dir != null) project(projectPath).projectDir = file(dir)
}

// Utilities shared by client and server (targeting JVM)
includeProject(":utils:platform") // \u9002\u914D\u5404\u4E2A\u5E73\u53F0\u7684\u57FA\u7840 API
includeProject(":utils:intellij-annotations")
includeProject(":utils:logging") // shared by client and server (targets JVM)
includeProject(":utils:serialization", "utils/serialization")
includeProject(":utils:coroutines", "utils/coroutines")
includeProject(":utils:ktor-client", "utils/ktor-client")
includeProject(":utils:io", "utils/io")
includeProject(":utils:testing", "utils/testing")
includeProject(":utils:xml")
includeProject(":utils:jsonpath")
includeProject(":utils:bbcode", "utils/bbcode")
includeProject(":utils:bbcode:test-codegen")
includeProject(":utils:ip-parser", "utils/ip-parser")
includeProject(":utils:ui-testing")
includeProject(":utils:androidx-lifecycle-runtime-testing")
includeProject(":utils:ui-preview")
includeProject(":utils:analytics")
includeProject(":utils:http-downloader")
includeProject(":utils:build-config")
includeProject(":utils:video-enhancement-shader-provider")
includeProject(":utils:selector-workflow") // \u6570\u636E\u6E90\u9009\u62E9\u6D41\u7A0B\u793A\u610F\u52A8\u753B\u7684\u6570\u636E\u5C42


includeProject(":torrent:torrent-api", "torrent/api") // Torrent \u7CFB\u7EDF API
includeProject(":torrent:anitorrent")
//includeProject(":torrent:anitorrent:anitorrent-native")
includeProject(":torrent:pikpak") // PikPak \u4E91\u79BB\u7EBF\u4E0B\u8F7D\u540E\u7AEF

includeProject(":app:shared")
includeProject(":app:shared:app-platform")
includeProject(":app:shared:app-data")
includeProject(":app:shared:app-data-aidl")
includeProject(":app:shared:app-lang") // We have a separate module so that the project compiles faster
includeProject(":app:shared:ui-foundation")
includeProject(":app:shared:ui-settings")
includeProject(":app:shared:ui-adaptive")
includeProject(":app:shared:ui-subject")
includeProject(":app:shared:ui-cache")
includeProject(":app:shared:ui-exploration")
includeProject(":app:shared:ui-comment")
includeProject(":app:shared:ui-onboarding")
includeProject(":app:shared:ui-mediaselect")
includeProject(":app:shared:ui-tv") // Android TV (\u9065\u63A7\u5668) \u4E13\u5C5E\u754C\u9762, \u53EA\u542B android target
includeProject(":app:shared:ui-episode")
includeProject(":app:shared:ui-exprovider")
includeProject(":app:shared:ui-watchtogether")
includeProject(":app:shared:video-player:video-player-api", "app/shared/video-player/api")
includeProject(":app:shared:video-player:torrent-source")
includeProject(":app:shared:video-player")
includeProject(":app:shared:application")

includeProject(":app:shared:placeholder", "app/shared/thirdparty/placeholder")
includeProject(":app:shared:paging-compose", "app/shared/thirdparty/paging-compose")
includeProject(":app:shared:image-viewer", "app/shared/thirdparty/image-viewer")
includeProject(":app:shared:reorderable", "app/shared/thirdparty/reorderable")

includeProject(":app:desktop", "app/desktop") // desktop JVM client for macOS, Windows, and Linux
includeProject(":app:android", "app/android") // Android client
includeProject(":app:ios", "app/ios") // iOS Launcher

includeProject(":client")

// server
//includeProject(":server:core", "server/core") // server core
//includeProject(":server:database", "server/database") // server database interfaces
//includeProject(":server:database-xodus", "server/database-xodus") // database implementation with Xodus

// data sources
includeProject(":datasource:datasource-api", "datasource/api") // data source interfaces: Media, MediaSource 
includeProject(":datasource:datasource-api:test-codegen", "datasource/api/test-codegen") // \u751F\u6210\u5355\u5143\u6D4B\u8BD5
includeProject(
    ":datasource:datasource-core",
    "datasource/core",
) // data source managers: MediaFetcher, MediaCacheStorage
includeProject(":datasource:bangumi", "datasource/bangumi") // https://bangumi.tv
//   BT \u6570\u636E\u6E90
includeProject(":datasource:dmhy", "datasource/bt/dmhy") // https://dmhy.org
includeProject(":datasource:mikan", "datasource/bt/mikan") // https://mikanani.me/
//   Web \u6570\u636E\u6E90
includeProject(":datasource:web-base", "datasource/web/web-base") // web \u57FA\u7840
includeProject(":datasource:jellyfin", "datasource/jellyfin")
includeProject(":datasource:ikaros", "datasource/ikaros") // https://ikaros.run/

// danmaku
includeProject(":danmaku:danmaku-ui-config", "danmaku/ui-config")
includeProject(":danmaku:danmaku-api", "danmaku/api")
includeProject(":danmaku:danmaku-ui", "danmaku/ui")
includeProject(":danmaku:dandanplay", "danmaku/dandanplay")

includeProject(
    ":datasource:dmhy:dataset-tools",
    "datasource/bt/dmhy/dataset-tools",
) // tools for generating dataset for ML title parsing

// ci
includeProject(":ci-helper", "ci-helper") // 
includeProject(
    ":ci-helper:sqlite-woa64",
    "ci-helper/sqlite-woa64",
) // Windows ARM64 SQLite natives, see its build.gradle.kts
includeProject(":tools:datasource-test-mcp", "tools/datasource-test-mcp")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")


// settings \u5148\u4E8E build-logic \u6784\u5EFA, \u62FF\u4E0D\u5230 LocalPropertiesValueSource, \u8FD9\u91CC\u5355\u72EC\u5B9E\u73B0\u4E00\u4EFD.
val localProperties: Provider<Properties> =
    providers.fileContents(layout.settingsDirectory.file("local.properties")).asText
        .map { text -> Properties().apply { text.reader().use { load(it) } } }

fun findLocalProperty(key: String): String? = localProperties.orNull?.getProperty(key)

findLocalProperty("ani.build.mediamp.path")?.let { mediampPath ->
    println("i:: Including mediamp as a Composite Build from: $mediampPath")
    includeBuild(mediampPath) {
        dependencySubstitution {
            substitute(module("org.openani.mediamp:mediamp-api"))
                .using(project(":mediamp-api"))
            substitute(module("org.openani.mediamp:mediamp-exoplayer"))
                .using(project(":mediamp-exoplayer"))
            substitute(module("org.openani.mediamp:mediamp-mpv"))
                .using(project(":mediamp-mpv"))
            /*substitute(module("org.openani.mediamp:mediamp-ffmpeg"))
                .using(project(":mediamp-ffmpeg"))*/
            substitute(module("org.openani.mediamp:mediamp-test"))
                .using(project(":mediamp-test"))
            substitute(module("org.openani.mediamp:mediamp-source-ktxio"))
                .using(project(":mediamp-source-ktxio"))
            substitute(module("org.openani.mediamp:mediamp-avkit"))
                .using(project(":mediamp-avkit"))
        }
    }
}

findLocalProperty("ani.build.anitorrent.path")?.let { anitorrentPath ->
    println("i:: Including anitorrent as a Composite Build from: $anitorrentPath")
    includeBuild(anitorrentPath) {
        dependencySubstitution {
            substitute(module("org.openani.anitorrent:anitorrent-native"))
                .using(project(":anitorrent-native"))
            substitute(module("org.openani.anitorrent:anitorrent-native-desktop-jni"))
                .using(project(":anitorrent-native-desktop-jni"))
        }
    }
}
