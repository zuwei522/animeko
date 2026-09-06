/*
 * Copyright (C) 2024-2025 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.foundation.animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.LocalAniUiBehavior
import me.him188.ani.app.ui.foundation.layout.currentWindowAdaptiveInfo1
import me.him188.ani.app.ui.foundation.layout.isWidthCompact
import me.him188.ani.app.ui.foundation.theme.EasingDurations
import me.him188.ani.utils.platform.Platform
import me.him188.ani.utils.platform.currentPlatform

/**
 * APP 统一动画方案
 *
 * @see NavigationMotionScheme
 */
@Immutable
// use object equality
class AniMotionScheme(
    val topLevelTransition: ContentTransform,
    val feedItemFadeInSpec: FiniteAnimationSpec<Float>,
    val feedItemPlacementSpec: FiniteAnimationSpec<IntOffset>,
    val feedItemFadeOutSpec: FiniteAnimationSpec<Float>,
    val animatedContent: AnimatedContentMotionScheme,
    val animatedVisibility: AnimatedVisibilityMotionScheme,
    val carouselAutoAdvanceSpec: FiniteAnimationSpec<Float> = tween(
        durationMillis = 1000,
        easing = EmphasizedEasing,
    ),
    val disableAnimations: Boolean = false,
) {
    companion object {
        fun calculate(density: Density): AniMotionScheme {
            val feedItemFadeOutTime = EasingDurations.standardAccelerate
            val feedItemFadeInTime = EasingDurations.standardDecelerate

            val topLevelTransition = run {
                val outTime = 50
                val inTime = 150
                fadeIn(
                    animationSpec = tween(
                        durationMillis = inTime,
                        delayMillis = outTime,
                        easing = StandardDecelerateEasing,
                    ),
                ).togetherWith(
                    fadeOut(
                        animationSpec = tween(
                            durationMillis = outTime,
                            delayMillis = 0,
                            easing = StandardAccelerateEasing,
                        ),
                    ),
                )
            }
            val animatedVisibility = calculateAnimatedVisibilityMotionScheme(density)
            val animatedContent = calculateAnimatedContentMotionScheme(density, topLevelTransition)
            return AniMotionScheme(
                topLevelTransition = topLevelTransition,
                feedItemFadeInSpec = tween(
                    durationMillis = feedItemFadeInTime,
                    delayMillis = feedItemFadeOutTime,
                    easing = StandardDecelerateEasing,
                ),
                feedItemFadeOutSpec = kotlin.run {
                    if (currentPlatform() is Platform.Desktop) {
                        snap()
                    } else {
                        tween(
                            durationMillis = feedItemFadeOutTime,
                            delayMillis = 0,
                            easing = StandardAccelerateEasing,
                        )
                    }
                },
                feedItemPlacementSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    visibilityThreshold = IntOffset.VisibilityThreshold,
                ),
                animatedContent = animatedContent,
                animatedVisibility = animatedVisibility,
            )
        }

        fun calculateLowEnd(density: Density): AniMotionScheme {
            val noneContentTransform: AnimatedContentTransitionScope<*>.() -> ContentTransform = {
                EnterTransition.None togetherWith ExitTransition.None using SizeTransform(clip = false)
            }
            return AniMotionScheme(
                topLevelTransition = EnterTransition.None togetherWith ExitTransition.None,
                feedItemFadeInSpec = snap(),
                feedItemFadeOutSpec = snap(),
                feedItemPlacementSpec = snap(),
                animatedContent = AnimatedContentMotionScheme(
                    standard = noneContentTransform,
                    topLevel = noneContentTransform,
                    screenEnter = noneContentTransform,
                ),
                animatedVisibility = AnimatedVisibilityMotionScheme(
                    standardEnter = EnterTransition.None,
                    standardExit = ExitTransition.None,
                    rowEnter = EnterTransition.None,
                    rowExit = ExitTransition.None,
                    columnEnter = EnterTransition.None,
                    columnExit = ExitTransition.None,
                    screenEnter = EnterTransition.None,
                    screenExit = ExitTransition.None,
                ),
                carouselAutoAdvanceSpec = snap(),
                disableAnimations = true,
            )
        }

        private fun calculateAnimatedContentMotionScheme(
            density: Density,
            topLevelTransition: ContentTransform
        ): AnimatedContentMotionScheme {
            return AnimatedContentMotionScheme(
                standard = {
                    val outTime = EasingDurations.standardAccelerate
                    val inTime = EasingDurations.standardDecelerate

                    val fadeIn = fadeIn(
                        tween(
                            durationMillis = inTime,
                            delayMillis = outTime,
                            easing = StandardDecelerateEasing,
                        ),
                    )
                    val fadeOut = fadeOut(
                        tween(
                            durationMillis = outTime,
                            delayMillis = 0,
                            easing = StandardAccelerateEasing,
                        ),
                    )
                    fadeIn.togetherWith(fadeOut).using(
                        SizeTransform(clip = true),
                    )
                },
                topLevel = {
                    topLevelTransition
                },
                screenEnter = {
                    fadeIn(
                        tween(
                            EasingDurations.emphasizedDecelerate,
                            delayMillis = EasingDurations.emphasizedAccelerate,
                            easing = EmphasizedDecelerateEasing,
                        ),
                    ) + slideInVertically(
                        tween(EasingDurations.emphasizedDecelerate),
                        initialOffsetY = { with(density) { 32.dp.toPx() }.coerceAtMost(it.toFloat()).toInt() },
                    ) togetherWith fadeOut(
                        tween(
                            EasingDurations.emphasizedAccelerate,
                            delayMillis = 0,
                            easing = EmphasizedAccelerateEasing,
                        ),
                    )
                },
            )
        }

        private fun calculateAnimatedVisibilityMotionScheme(density: Density): AnimatedVisibilityMotionScheme {
            val outTime = EasingDurations.standardAccelerate
            val inTime = EasingDurations.standardDecelerate
            fun <T> enterTween() = tween<T>(
                durationMillis = inTime,
                delayMillis = 0,
                easing = StandardDecelerateEasing,
            )

            fun <T> exitTween() = tween<T>(
                durationMillis = outTime,
                delayMillis = 0,
                easing = StandardAccelerateEasing,
            )

            val expandShrinkSpring = spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = IntSize.VisibilityThreshold,
            )
            return AnimatedVisibilityMotionScheme(
                standardEnter = fadeIn(enterTween()),
                standardExit = fadeOut(exitTween()),
                rowEnter = expandHorizontally(expandShrinkSpring, expandFrom = Alignment.Start),
                rowExit = shrinkHorizontally(expandShrinkSpring, shrinkTowards = Alignment.Start),
                columnEnter = expandVertically(expandShrinkSpring, expandFrom = Alignment.Top),
                columnExit = shrinkVertically(expandShrinkSpring, shrinkTowards = Alignment.Top),
                screenEnter = fadeIn(
                    tween(
                        EasingDurations.emphasized,
                        delayMillis = 0,
                        easing = EmphasizedEasing,
                    ),
                ) + slideInVertically(
                    tween(EasingDurations.emphasized),
                    initialOffsetY = { with(density) { 32.dp.toPx() }.coerceAtMost(it.toFloat()).toInt() },
                ),
                screenExit = fadeOut(snap()),
            )
        }
    }
}

@Immutable
class AnimatedContentMotionScheme(
    val standard: AnimatedContentTransitionScope<*>.() -> ContentTransform,
    val topLevel: AnimatedContentTransitionScope<*>.() -> ContentTransform,
    val screenEnter: AnimatedContentTransitionScope<*>.() -> ContentTransform
)

@Immutable
class AnimatedVisibilityMotionScheme(
    val standardEnter: EnterTransition,
    val standardExit: ExitTransition,
    val rowEnter: EnterTransition,
    val rowExit: ExitTransition,
    val columnEnter: EnterTransition,
    val columnExit: ExitTransition,
    val screenEnter: EnterTransition,
    val screenExit: ExitTransition,
)

@Stable
val LocalAniMotionScheme: ProvidableCompositionLocal<AniMotionScheme> =
    staticCompositionLocalOf { error("No AniMotionScheme provided") }


@Composable
fun ProvideAniMotionCompositionLocals(
    disableAnimations: Boolean = false,
    content: @Composable () -> Unit
) {
    val density by rememberUpdatedState(LocalDensity.current)
    val windowSizeClass by rememberUpdatedState(currentWindowAdaptiveInfo1().windowSizeClass)

    val isWidthCompact by remember {
        derivedStateOf {
            windowSizeClass.isWidthCompact
        }
    }
    val crossfade = LocalAniUiBehavior.current.crossfadeNavigation
    val navigationMotionScheme by remember(crossfade, disableAnimations) {
        derivedStateOf {
            if (disableAnimations) {
                NavigationMotionScheme(
                    enterTransition = EnterTransition.None,
                    exitTransition = ExitTransition.None,
                    popEnterTransition = EnterTransition.None,
                    popExitTransition = ExitTransition.None,
                )
            } else if (crossfade) {
                NavigationMotionScheme.calculateCrossfade()
            } else {
                NavigationMotionScheme.calculate(useSlide = isWidthCompact)
            }
        }
    }
    val aniMotionScheme by remember(disableAnimations) {
        derivedStateOf {
            if (disableAnimations) AniMotionScheme.calculateLowEnd(density)
            else AniMotionScheme.calculate(density)
        }
    }
    CompositionLocalProvider(
        LocalNavigationMotionScheme provides navigationMotionScheme,
        LocalAniMotionScheme provides aniMotionScheme,
        content = content,
    )
}
