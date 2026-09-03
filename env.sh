#!/usr/bin/env bash
# Animeko TV 开发环境配置脚本
# 用法: source env.sh
DEV_ENV_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../dev-env" && pwd)"
# JDK 21
export JAVA_HOME="${DEV_ENV_DIR}/jdk21"
# Android SDK
export ANDROID_HOME="${DEV_ENV_DIR}/android-sdk"
export ANDROID_SDK_ROOT="${ANDROID_HOME}"
# PATH
export PATH="${JAVA_HOME}/bin:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/build-tools/37.0.0:${PATH}"
# Gradle 用户目录 (持久化, 避免重复下载 wrapper 与依赖)
export GRADLE_USER_HOME="${DEV_ENV_DIR}/gradle-home"
# 本地 Maven 仓库 (持久化): 存放手动下载的 google-services 插件等
export M2_REPO="${DEV_ENV_DIR}/m2-repo"
# Gradle 配置 (沙箱内存有限, 避免 OOM)
export GRADLE_OPTS="-Xmx2g -Dorg.gradle.daemon=false -Dkotlin.daemon.jvm.options=-Xmx1024m"
echo "Animeko TV 开发环境已配置:"
echo "  JAVA_HOME: ${JAVA_HOME}"
echo "  ANDROID_HOME: ${ANDROID_HOME}"
echo "  GRADLE_USER_HOME: ${GRADLE_USER_HOME}"
echo "  M2_REPO: ${M2_REPO}"
java -version 2>&1 | head -1
