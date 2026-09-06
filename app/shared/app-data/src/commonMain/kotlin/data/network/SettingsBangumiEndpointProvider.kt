/*
 * Copyright (C) 2024-2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.data.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.him188.ani.app.data.models.preference.BangumiMirrorSettings
import me.him188.ani.app.domain.settings.BangumiMirrorProvider
import me.him188.ani.datasources.bangumi.BangumiEndpointProvider

/**
 * 基于设置的 Bangumi 端点提供者.
 *
 * 从 [BangumiMirrorProvider] 读取当前生效的镜像配置, 动态解析各子域的 base URL.
 * 设置变化时自动更新, 用户在设置中切换镜像后立即生效.
 *
 * @param mirrorProvider 镜像设置提供者
 * @param backgroundScope 后台协程作用域, 用于监听设置变化
 */
class SettingsBangumiEndpointProvider(
    mirrorProvider: BangumiMirrorProvider,
    backgroundScope: CoroutineScope,
) : BangumiEndpointProvider {
    private val settingsState: StateFlow<BangumiMirrorSettings> = mirrorProvider.settings
        .stateIn(backgroundScope, started = SharingStarted.Eagerly, initialValue = BangumiMirrorSettings.Default)

    private val current: BangumiMirrorSettings get() = settingsState.value

    override val apiBaseUrl: String get() = current.apiBaseUrl
    override val nextBaseUrl: String get() = current.nextBaseUrl
    override val lainBaseUrl: String get() = current.lainBaseUrl
    override val rootBaseUrl: String get() = current.rootBaseUrl
}
