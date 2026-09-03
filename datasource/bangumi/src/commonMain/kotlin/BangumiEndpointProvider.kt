/*
 * Copyright (C) 2024-2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.datasources.bangumi

/**
 * Bangumi API 端点提供者.
 *
 * 用于支持自定义 Bangumi 镜像地址. 实现层从用户设置中读取当前生效的镜像域名.
 * 默认实现使用原站域名.
 */
interface BangumiEndpointProvider {
    /**
     * Bangumi v0 API 的 base URL, 如 "https://api.bgm.tv" 或镜像 "https://api.bangumi.pro".
     */
    val apiBaseUrl: String

    /**
     * Bangumi Next API 的 base URL, 如 "https://next.bgm.tv" 或镜像 "https://next.bangumi.pro".
     */
    val nextBaseUrl: String

    /**
     * Bangumi 图片站的 base URL, 如 "https://lain.bgm.tv" 或镜像 "https://lain.bangumi.pro".
     */
    val lainBaseUrl: String

    /**
     * Bangumi 主站的 base URL, 如 "https://bgm.tv" 或镜像 "https://bangumi.pro".
     */
    val rootBaseUrl: String
}

/**
 * 默认端点提供者, 使用原站域名.
 */
object DefaultBangumiEndpointProvider : BangumiEndpointProvider {
    override val apiBaseUrl: String = "https://api.bgm.tv"
    override val nextBaseUrl: String = "https://next.bgm.tv"
    override val lainBaseUrl: String = "https://lain.bgm.tv"
    override val rootBaseUrl: String = "https://bgm.tv"
}
