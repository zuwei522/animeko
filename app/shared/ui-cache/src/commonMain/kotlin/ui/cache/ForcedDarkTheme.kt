/*
 * Copyright (C) 2024-2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.cache

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.him188.ani.app.data.models.preference.DarkMode
import me.him188.ani.app.ui.foundation.LocalAniUiBehavior
import me.him188.ani.app.ui.foundation.theme.AniTheme
import me.him188.ani.app.ui.foundation.theme.AniThemeDefaults

/**
 * 按 [AniUiBehavior.forceDarkInPlayer][me.him188.ani.app.ui.foundation.AniUiBehavior.forceDarkInPlayer]
 * 强制深色主题, 否则原样.
 *
 * 缓存相关页面 (缓存管理 / 缓存详情) 在只从播放链路进入的形态下
 * (播放器 → 条目缓存页 → 管理全部缓存 → 缓存详情) 前后都是暗色内容;
 * 浅色主题下这些页面突然一页亮白非常刺眼, 统一成深色.
 *
 * 强制深色的同时必须给内容垫上不透明的深色页面背景: 沉浸式外壳
 * (immersiveShell, 如 TV) 下页面容器可能是透明的 (透出外壳底色), 若背景透明,
 * 全屏路由进入时会露出黑色根背景 (TvAniUiBehavior.blackRootBackground),
 * 浅色外壳下也会露出不匹配的外壳底色, 产生异常黑色背景.
 */
@Composable
internal fun ForcedDarkTheme(content: @Composable () -> Unit) {
    if (LocalAniUiBehavior.current.forceDarkInPlayer) {
        AniTheme(darkModeOverride = DarkMode.DARK) {
            Box(
                Modifier.fillMaxSize().background(AniThemeDefaults.pageContentBackgroundColor),
            ) {
                content()
            }
        }
    } else {
        content()
    }
}
