#!/usr/bin/env bash
# Animeko TV 构建脚本
#
# 用法:
#   ./build.sh debug          - 构建 Debug APK (默认 TV flavor)
#   ./build.sh debug phone    - 构建 Debug APK (手机 flavor)
#   ./build.sh release        - 构建 Release APK (需要签名)
#   ./build.sh clean          - 清理构建缓存
#   ./build.sh deps           - 仅下载依赖
#   ./build.sh check          - 编译检查 (编译所有改动模块的 Android target)
#
# 说明:
#   - 环境依赖 (JDK21 / Android SDK / Gradle 缓存 / 本地 Maven 仓库) 全部持久化在
#     ../dev-env/ 目录, 由 env.sh 自动配置.
#   - 项目使用 com.android.kotlin.multiplatform.library, Android 编译任务名为
#     compileAndroidMain (非旧版 compileKotlinAndroid).
#   - app:android 有 DefaultPhone / DefaultTv 两个 flavor, 编译任务为
#     compileDefaultTvDebugKotlin / compileDefaultPhoneDebugKotlin 等.
#   - 沙箱内存有限, 默认 --max-workers=2 防止 OOM; 内存充足时可自行调高.

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}"

# 加载环境 (JDK/Android SDK/Gradle 用户目录等)
source "${SCRIPT_DIR}/env.sh"

GRADLE="${SCRIPT_DIR}/gradlew"
chmod +x "${GRADLE}"

# 构建输出目录
OUTPUT_DIR="${SCRIPT_DIR}/build-outputs"
mkdir -p "${OUTPUT_DIR}"

# 默认 flavor (TV 版 fork 以 DefaultTv 为主)
FLAVOR="${2:-DefaultTv}"

# 限制并行度, 防止沙箱内存不足
MAX_WORKERS=2

build_debug() {
    echo "==> 构建 Debug APK (flavor: ${FLAVOR})..."
    "${GRADLE}" ":app:android:assemble${FLAVOR}Debug" --max-workers=${MAX_WORKERS} --stacktrace
    APK_PATH=$(find "${SCRIPT_DIR}/app/android/build/outputs/apk" -name "*${FLAVOR}*debug*.apk" -type f | head -1)
    if [ -n "${APK_PATH}" ]; then
        cp "${APK_PATH}" "${OUTPUT_DIR}/animeko-tv-${FLAVOR}-debug.apk"
        echo "==> Debug APK 已输出: ${OUTPUT_DIR}/animeko-tv-${FLAVOR}-debug.apk"
        ls -lh "${OUTPUT_DIR}/animeko-tv-${FLAVOR}-debug.apk"
    else
        echo "==> 错误: 未找到生成的 APK"
        exit 1
    fi
}

build_release() {
    echo "==> 构建 Release APK (flavor: ${FLAVOR})..."
    "${GRADLE}" ":app:android:assemble${FLAVOR}Release" --max-workers=${MAX_WORKERS} --stacktrace
    APK_PATH=$(find "${SCRIPT_DIR}/app/android/build/outputs/apk" -name "*${FLAVOR}*release*.apk" -type f | head -1)
    if [ -n "${APK_PATH}" ]; then
        cp "${APK_PATH}" "${OUTPUT_DIR}/animeko-tv-${FLAVOR}-release.apk"
        echo "==> Release APK 已输出: ${OUTPUT_DIR}/animeko-tv-${FLAVOR}-release.apk"
        ls -lh "${OUTPUT_DIR}/animeko-tv-${FLAVOR}-release.apk"
    else
        echo "==> 错误: 未找到生成的 APK"
        exit 1
    fi
}

clean_build() {
    echo "==> 清理构建缓存..."
    "${GRADLE}" clean
    rm -rf "${OUTPUT_DIR}"
    echo "==> 清理完成"
}

download_deps() {
    echo "==> 下载项目依赖..."
    "${GRADLE}" buildEnvironment --max-workers=${MAX_WORKERS} 2>/dev/null || true
    echo "==> 依赖下载完成"
}

run_check() {
    echo "==> 运行编译检查 (所有改动模块的 Android target)..."
    "${GRADLE}" \
        :datasource:bangumi:compileAndroidMain \
        :app:shared:app-data:compileAndroidMain \
        :app:shared:ui-foundation:compileAndroidMain \
        :app:shared:ui-settings:compileAndroidMain \
        :app:shared:application:compileAndroidMain \
        --max-workers=${MAX_WORKERS} --stacktrace
    echo "==> 检查完成"
}

case "${1:-debug}" in
    debug)
        build_debug
        ;;
    release)
        build_release
        ;;
    clean)
        clean_build
        ;;
    deps)
        download_deps
        ;;
    check)
        run_check
        ;;
    *)
        echo "用法: $0 {debug|release|clean|deps|check} [DefaultTv|DefaultPhone]"
        exit 1
        ;;
esac
