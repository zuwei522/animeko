/*
 * Copyright (C) 2024-2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link:
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.settings

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.shareIn
import me.him188.ani.app.data.models.preference.BangumiMirrorSettings
import me.him188.ani.app.data.repository.user.SettingsRepository

/**
 * Bangumi 镜像地址提供者.
 *
 * 提供当前生效的 [BangumiMirrorSettings], 支持动态更新.
 * 各网络服务通过它获取对应的 base URL.
 */
interface BangumiMirrorProvider {
    val settings: Flow<BangumiMirrorSettings>
}

/**
 * 不使用镜像的提供者 (原站直连).
 */
data object NoBangumiMirrorProvider : BangumiMirrorProvider {
    override val settings: Flow<BangumiMirrorSettings> = flowOf(BangumiMirrorSettings.Default)
}

/**
 * 固定镜像配置的提供者.
 */
data class ConstantBangumiMirrorProvider(
    val value: BangumiMirrorSettings,
) : BangumiMirrorProvider {
    override val settings: Flow<BangumiMirrorSettings> = flowOf(value)
}

/**
 * 基于设置仓库的镜像提供者.
 * 从 [SettingsRepository.bangumiMirrorSettings] 读取配置, 支持动态更新.
 */
class SettingsBasedBangumiMirrorProvider(
    private val settingsRepository: SettingsRepository,
    backgroundScope: CoroutineScope,
) : BangumiMirrorProvider {
    override val settings: Flow<BangumiMirrorSettings> =
        settingsRepository.bangumiMirrorSettings.flow
            .distinctUntilChanged()
            .shareIn(backgroundScope, started = SharingStarted.WhileSubscribed(), replay = 1)
}
