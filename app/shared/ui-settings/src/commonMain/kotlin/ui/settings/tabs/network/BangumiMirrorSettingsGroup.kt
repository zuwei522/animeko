/*
 * Copyright (C) 2024-2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link:
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.tabs.network

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.him188.ani.app.data.models.preference.BangumiMirrorSettings
import me.him188.ani.app.data.models.preference.BangumiMirrorPreset
import me.him188.ani.app.data.repository.user.Settings
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import me.him188.ani.app.ui.settings.framework.components.SwitchItem
import me.him188.ani.app.ui.settings.framework.components.TextFieldItem

/**
 * Bangumi 镜像地址设置组.
 *
 * 功能:
 * - 启用/禁用镜像
 * - 内置预设镜像选择 (一键设置)
 * - 自定义镜像根域名
 * - 按子域单独配置镜像地址
 *
 * @param settings Bangumi 镜像设置 (来自 SettingsRepository)
 * @param backgroundScope 后台协程作用域, 用于保存设置
 */
@Composable
fun SettingsScope.BangumiMirrorSettingsGroup(
    settings: Settings<BangumiMirrorSettings>,
    backgroundScope: CoroutineScope,
    modifier: Modifier = Modifier,
) {
    val current by settings.flow.collectAsStateWithLifecycle(BangumiMirrorSettings.Default)
    var showAdvanced by remember { mutableStateOf(false) }

    Group(
        title = { Text("Bangumi 镜像地址") },
        useThinHeader = true,
        modifier = modifier,
    ) {
        // 启用开关
        SwitchItem(
            checked = current.enabled,
            title = { Text("启用 Bangumi 镜像") },
            description = { Text("通过镜像站点访问 Bangumi API, 解决国内无法访问的问题") },
            onCheckedChange = { enabled ->
                backgroundScope.launch {
                    settings.update { copy(enabled = enabled) }
                }
            },
        )

        if (current.enabled) {
            HorizontalDividerItem()

            // 预设镜像选择 + 一键设置
            PresetSelector(
                current = current,
                onSelectPreset = { preset ->
                    backgroundScope.launch {
                        settings.set(current.withOneClickMirror(preset.rootDomain))
                    }
                },
            )

            HorizontalDividerItem()

            // 自定义根域名
            TextFieldItem(
                value = current.mirrorRootDomain,
                title = { Text("镜像根域名") },
                description = { Text("如 bangumi.pro, 各子域自动拼接 (api.bangumi.pro 等)") },
                placeholder = { Text("bangumi.pro") },
                onValueChangeCompleted = { value ->
                    backgroundScope.launch {
                        settings.update { copy(mirrorRootDomain = value.trim()) }
                    }
                },
                sanitizeValue = { it.trim() },
            )

            HorizontalDividerItem()

            // 高级设置: 子域单独配置
            TextButton(onClick = { showAdvanced = !showAdvanced }) {
                Text(if (showAdvanced) "收起子域单独配置" else "展开子域单独配置")
            }

            if (showAdvanced) {
                SubdomainOverrides(
                    current = current,
                    onUpdate = { subkey, domain ->
                        backgroundScope.launch {
                            settings.set(current.withSubdomainOverride(subkey, domain))
                        }
                    },
                )
            }

            HorizontalDividerItem()

            // 当前生效地址预览
            CurrentAddressesPreview(current)
        }
    }
}

/**
 * 预设镜像选择器.
 */
@Composable
private fun PresetSelector(
    current: BangumiMirrorSettings,
    onSelectPreset: (BangumiMirrorPreset) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "预设镜像",
            style = MaterialTheme.typography.titleSmall,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BangumiMirrorSettings.PRESETS.forEach { preset ->
                val isSelected = current.mirrorRootDomain == preset.rootDomain &&
                        current.subdomainOverrides.isEmpty()
                Button(
                    onClick = { onSelectPreset(preset) },
                    modifier = Modifier.weight(1f),
                ) {
                    if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text(preset.name, softWrap = false)
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "点击预设一键设置所有子域 (镜像子域名与源站子域名一致)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * 子域单独配置.
 */
@Composable
private fun SettingsScope.SubdomainOverrides(
    current: BangumiMirrorSettings,
    onUpdate: (subkey: String, domain: String) -> Unit,
) {
    Column {
        BangumiMirrorSettings.ALL_SUBKEYS.forEach { subkey ->
            val originalDomain = current.originalDomainOf(subkey)
            val override = current.subdomainOverrides[subkey] ?: ""
            val effective = current.resolveBaseUrl(subkey)

            TextFieldItem(
                value = override,
                title = { Text("$originalDomain 镜像") },
                description = {
                    Text(
                        "当前生效: $effective" +
                                if (override.isBlank()) " (使用根域名自动拼接)" else "",
                    )
                },
                placeholder = { Text("留空使用根域名自动拼接") },
                onValueChangeCompleted = { value ->
                    onUpdate(subkey, value)
                },
                sanitizeValue = { it.trim() },
            )
            HorizontalDividerItem()
        }
    }
}

/**
 * 当前生效地址预览.
 */
@Composable
private fun CurrentAddressesPreview(current: BangumiMirrorSettings) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "当前生效地址",
            style = MaterialTheme.typography.titleSmall,
        )
        Spacer(modifier = Modifier.height(8.dp))
        BangumiMirrorSettings.ALL_SUBKEYS.forEach { subkey ->
            val original = current.originalDomainOf(subkey)
            val effective = current.resolveBaseUrl(subkey)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = original,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "→ $effective",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (effective.contains("bgm.tv"))
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
