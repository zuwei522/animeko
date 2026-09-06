/*
 * Copyright (C) 2024-2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.android.activity

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.core.view.WindowCompat
import me.him188.ani.android.BuildConfig
import me.him188.ani.android.InstallFormFactorUi
import me.him188.ani.android.formFactorUiBehavior
import me.him188.ani.android.onFormFactorActivityCreated
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.him188.ani.app.data.repository.user.SettingsRepository
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.platform.AniComponentActivity
import me.him188.ani.app.platform.rememberPlatformWindow
import me.him188.ani.app.ui.exprovider.ExternalContentProviderFactory
import me.him188.ani.app.ui.exprovider.LocalExternalContentProvider
import me.him188.ani.app.ui.foundation.UiScaleApplier
import me.him188.ani.app.ui.foundation.layout.LocalPlatformWindow
import me.him188.ani.app.ui.foundation.theme.SystemBarColorEffect
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.foundation.widgets.Toaster
import me.him188.ani.app.ui.main.AniApp
import me.him188.ani.app.ui.main.AniAppContent
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.logger
import org.koin.android.ext.android.inject

class MainActivity : AniComponentActivity() {
    private val logger = logger<MainActivity>()
    private val aniNavigator = AniNavigator()

    private val externalContentProviderFactory: ExternalContentProviderFactory by inject()
    private val settingsRepository: SettingsRepository by inject()

    private var appliedUiScale: Float = 1f
    private var uiScaleRestartRequested = false

    private val uiScaleApplier = object : UiScaleApplier {
        override val appliedScale: Float get() = appliedUiScale

        override fun apply(scale: Float) {
            if (scale == appliedUiScale || uiScaleRestartRequested) return
            if (isFinishing || isDestroyed) return
            uiScaleRestartRequested = true
            UiScaleMirror.write(this@MainActivity, scale)
            recreate()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val scale = UiScaleMirror.read(newBase)
        appliedUiScale = scale
        super.attachBaseContext(newBase.withUiScale(scale))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        handleStartIntent(intent)
    }

    private fun handleStartIntent(intent: Intent) {
        val data = intent.data ?: return
        if (data.scheme != "ani") return
        if (data.host == "subjects") {
            val id = data.pathSegments.getOrNull(0)?.toIntOrNull() ?: return
            lifecycleScope.launch {
                try {
                    if (!aniNavigator.isBackStackReady()) {
                        aniNavigator.awaitBackStack()
                        delay(1000)
                    }
                    aniNavigator.navigateSubjectDetails(id, placeholder = null)
                } catch (e: Exception) {
                    logger.error(e) { "Failed to navigate to subject details" }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleStartIntent(intent)

        onFormFactorActivityCreated(this)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val toaster = object : Toaster {
            override fun toast(text: String) {
                Toast.makeText(this@MainActivity, text, Toast.LENGTH_LONG).show()
            }
        }

        val externalContentProvider = externalContentProviderFactory.create(this, lifecycleScope)

        lifecycleScope.launch(Dispatchers.IO) {
            settingsRepository.themeSettings.flow
                .map { it.effectiveUiScale }
                .distinctUntilChanged()
                .collect { UiScaleMirror.write(this@MainActivity, it) }
        }

        setContent {
            AniApp(uiBehavior = formFactorUiBehavior, uiScaleApplier = uiScaleApplier) {
                val externalComponentProviderUpdated by rememberUpdatedState(externalContentProvider)

                SystemBarColorEffect()

                CompositionLocalProvider(
                    LocalToaster provides toaster,
                    LocalPlatformWindow provides rememberPlatformWindow(this),
                    LocalExternalContentProvider provides externalComponentProviderUpdated,
                ) {
                    @OptIn(ExperimentalComposeUiApi::class)
                    val rootModifier = if (BuildConfig.DEBUG) {
                        Modifier.semantics { testTagsAsResourceId = true }
                    } else {
                        Modifier
                    }
                    val disableAnimations = remember {
                        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                        am.memoryClass <= 256 || Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
                    }
                    Box(rootModifier) {
                        InstallFormFactorUi(aniNavigator) {
                            AniAppContent(aniNavigator, disableAnimations = disableAnimations)
                        }
                    }
                }
            }
        }
    }
}
