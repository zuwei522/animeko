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
import me.him188.ani.app.ui.foundation.LocalAniUiBehavior
import me.him188.ani.app.ui.foundation.theme.AniThemeDefaults

/**
 * 缓存相关页面 (缓存管理 / 缓存详情) 在播放链路的深色外壳下渲染.
 *
 * 沉浸式外壳 (immersiveShell, 如 TV) 下页面容器可能是透明的 (透出外壳底色),
 * 而 TV 根背景为纯黑 (TvAniUiBehavior.blackRootBackground), 容器透明时全屏路由
 * 进入会露出黑色根背景. 这里给内容垫上不透明的页面背景色
 * [AniThemeDefaults.pageContentBackgroundColor] —— 该颜色跟随当前深浅主题,
 * 与其它页面背景一致, 同时保证不露出黑色根背景.
 *
 * 不强制深色: 背景与文字均跟随用户当前的深浅主题.
 */
@Composable
internal fun ForcedDarkTheme(content: @Composable () -> Unit) {
    if (LocalAniUiBehavior.current.forceDarkInPlayer) {
        Box(
            Modifier.fillMaxSize().background(AniThemeDefaults.pageContentBackgroundColor),
        ) {
            content()
        }
    } else {
        content()
    }
}
