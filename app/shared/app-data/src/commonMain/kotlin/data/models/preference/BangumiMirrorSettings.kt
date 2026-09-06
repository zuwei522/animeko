/*
 * Copyright (C) 2024-2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.data.models.preference

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Bangumi 镜像地址设置.
 *
 * 支持按子域单独配置镜像地址, 也支持一键设置全局镜像根域名.
 * 当镜像子域名与源站子域名一致时 (如 api.bangumi.pro 对应 api.bgm.tv),
 * 只需设置根域名即可自动拼接.
 *
 * 源站子域:
 * - [SUBKEY_API]   api.bgm.tv   (v0 API, 条目简介等)
 * - [SUBKEY_NEXT]  next.bgm.tv  (Next API, 评论等)
 * - [SUBKEY_LAIN]  lain.bgm.tv  (图片/静态资源, 表情等)
 * - [SUBKEY_ROOT]  bgm.tv       (主站, 无子域)
 */
@Serializable
data class BangumiMirrorSettings(
    /** 是否启用镜像. 关闭时所有请求走原站. */
    val enabled: Boolean = false,

    /**
     * 全局镜像根域名, 如 "bangumi.pro".
     * 启用后各子域自动拼接为 "子域.根域名" (如 api.bangumi.pro).
     * 主站 (无子域) 直接使用根域名.
     * 若 [subdomainOverrides] 中为某子域单独配置了地址, 则以单独配置为准.
     */
    val mirrorRootDomain: String = "",

    /**
     * 各子域的单独镜像地址覆盖.
     * key 为子域标识 ([SUBKEY_API] / [SUBKEY_NEXT] / [SUBKEY_LAIN] / [SUBKEY_ROOT]),
     * value 为完整的镜像域名 (如 "api.bangumi.pro") 或空字符串表示不覆盖.
     *
     * 优先级: 单独覆盖 > 全局根域名拼接 > 原站.
     */
    val subdomainOverrides: Map<String, String> = emptyMap(),
) {
    companion object {
        /** 子域标识: API (api.bgm.tv) */
        const val SUBKEY_API = "api"

        /** 子域标识: Next API (next.bgm.tv) */
        const val SUBKEY_NEXT = "next"

        /** 子域标识: 图片/静态资源 (lain.bgm.tv) */
        const val SUBKEY_LAIN = "lain"

        /** 子域标识: 主站 (bgm.tv, 无子域) */
        const val SUBKEY_ROOT = "root"

        val Default = BangumiMirrorSettings()

        /**
         * 内置预设镜像列表.
         * 每个预设包含显示名称和根域名.
         */
        val PRESETS: List<BangumiMirrorPreset> = listOf(
            BangumiMirrorPreset(
                name = "bangumi.pro (官方推荐)",
                rootDomain = "bangumi.pro",
                description = "Bangumi 全域名镜像, 支持 api / next / lain 等子域",
            ),
        )

        /** 所有支持的子域标识列表 */
        val ALL_SUBKEYS = listOf(SUBKEY_API, SUBKEY_NEXT, SUBKEY_LAIN, SUBKEY_ROOT)
    }

    /**
     * 获取指定子域的源站域名.
     */
    fun originalDomainOf(subkey: String): String = when (subkey) {
        SUBKEY_API -> "api.bgm.tv"
        SUBKEY_NEXT -> "next.bgm.tv"
        SUBKEY_LAIN -> "lain.bgm.tv"
        SUBKEY_ROOT -> "bgm.tv"
        else -> "bgm.tv"
    }

    /**
     * 获取指定子域的当前生效域名 (含 https:// 前缀).
     *
     * 解析优先级:
     * 1. 未启用 -> 原站
     * 2. 子域单独覆盖 (非空) -> 覆盖地址
     * 3. 全局根域名非空 -> 拼接 (主站直接用根域名)
     * 4. 否则 -> 原站
     */
    fun resolveBaseUrl(subkey: String): String {
        if (!enabled) return "https://${originalDomainOf(subkey)}"

        // 1. 子域单独覆盖
        val override = subdomainOverrides[subkey]
        if (!override.isNullOrBlank()) {
            return normalizeUrl(override)
        }

        // 2. 全局根域名拼接
        if (mirrorRootDomain.isNotBlank()) {
            val domain = if (subkey == SUBKEY_ROOT) {
                mirrorRootDomain
            } else {
                "$subkey.$mirrorRootDomain"
            }
            return "https://$domain"
        }

        // 3. 回退原站
        return "https://${originalDomainOf(subkey)}"
    }

    /**
     * 获取 API 子域的 base URL (api.bgm.tv 或其镜像).
     */
    val apiBaseUrl: String get() = resolveBaseUrl(SUBKEY_API)

    /**
     * 获取 Next API 子域的 base URL (next.bgm.tv 或其镜像).
     */
    val nextBaseUrl: String get() = resolveBaseUrl(SUBKEY_NEXT)

    /**
     * 获取图片子域的 base URL (lain.bgm.tv 或其镜像).
     */
    val lainBaseUrl: String get() = resolveBaseUrl(SUBKEY_LAIN)

    /**
     * 获取主站的 base URL (bgm.tv 或其镜像).
     */
    val rootBaseUrl: String get() = resolveBaseUrl(SUBKEY_ROOT)

    /**
     * 一键设置: 将全局根域名设为指定值, 并清空所有子域单独覆盖.
     * 适用于镜像子域名与源站子域名一致的情况 (如 bangumi.pro).
     */
    fun withOneClickMirror(rootDomain: String): BangumiMirrorSettings = copy(
        enabled = true,
        mirrorRootDomain = rootDomain.trim(),
        subdomainOverrides = emptyMap(),
    )

    /**
     * 为指定子域设置单独的镜像地址.
     */
    fun withSubdomainOverride(subkey: String, mirrorDomain: String): BangumiMirrorSettings {
        val newOverrides = subdomainOverrides.toMutableMap()
        if (mirrorDomain.isBlank()) {
            newOverrides.remove(subkey)
        } else {
            newOverrides[subkey] = mirrorDomain.trim()
        }
        return copy(subdomainOverrides = newOverrides)
    }

    /**
     * 规范化 URL: 确保有 https:// 前缀, 去掉末尾斜杠.
     */
    private fun normalizeUrl(domain: String): String {
        var url = domain.trim()
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }
        return url.trimEnd('/')
    }
}

/**
 * Bangumi 镜像预设.
 */
@Serializable
data class BangumiMirrorPreset(
    /** 显示名称 */
    val name: String,
    /** 镜像根域名 */
    val rootDomain: String,
    /** 描述 */
    val description: String = "",
)
