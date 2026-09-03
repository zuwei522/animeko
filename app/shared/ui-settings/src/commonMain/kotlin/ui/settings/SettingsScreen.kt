/*
 * Copyright (C) 2024-2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 \u8BB8\u53EF\u8BC1\u7684\u7EA6\u675F, \u53EF\u4EE5\u5728\u4EE5\u4E0B\u94FE\u63A5\u627E\u5230\u8BE5\u8BB8\u53EF\u8BC1.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link:
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SettingsApplications
import androidx.compose.material.icons.outlined.SmartDisplay
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.navigation.rememberAsyncBrowserNavigator
import me.him188.ani.app.ui.adaptive.AniListDetailPaneScaffold
import me.him188.ani.app.ui.adaptive.AniTopAppBar
import me.him188.ani.app.ui.adaptive.AniTopAppBarDefaults
import me.him188.ani.app.ui.adaptive.ListDetailLayoutParameters
import me.him188.ani.app.ui.adaptive.PaneScope
import me.him188.ani.app.ui.adaptive.TopAppBarSize
import me.him188.ani.app.ui.foundation.LocalAniUiBehavior
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.animation.AniAnimatedVisibility
import me.him188.ani.app.ui.foundation.animation.LocalAniMotionScheme
import me.him188.ani.app.ui.foundation.animation.NavigationMotionScheme
import me.him188.ani.app.ui.foundation.ifThen
import me.him188.ani.app.ui.foundation.layout.AniWindowInsets
import me.him188.ani.app.ui.foundation.layout.currentWindowAdaptiveInfo1
import me.him188.ani.app.ui.foundation.layout.isHeightAtLeastExpanded
import me.him188.ani.app.ui.foundation.layout.isHeightAtLeastMedium
import me.him188.ani.app.ui.foundation.layout.paneVerticalPadding
import me.him188.ani.app.ui.foundation.theme.AniThemeDefaults
import me.him188.ani.app.ui.foundation.theme.LocalAppChromeHazeState
import me.him188.ani.app.ui.foundation.theme.appChromeHazeSource
import me.him188.ani.app.ui.foundation.theme.isAppChromeFrostedGlassActive
import me.him188.ani.app.ui.foundation.widgets.BackNavigationIconButton
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.acknowledgements
import me.him188.ani.app.ui.lang.developer_list
import me.him188.ani.app.ui.lang.settings
import me.him188.ani.app.ui.lang.settings_acknowledgements_oss_licenses
import me.him188.ani.app.ui.lang.settings_category_app_ui
import me.him188.ani.app.ui.lang.settings_category_data_playback
import me.him188.ani.app.ui.lang.settings_category_network_storage
import me.him188.ani.app.ui.lang.settings_category_others
import me.him188.ani.app.ui.lang.settings_debug_mode_enabled
import me.him188.ani.app.ui.lang.settings_tab_about
import me.him188.ani.app.ui.lang.settings_tab_account
import me.him188.ani.app.ui.lang.settings_tab_appearance
import me.him188.ani.app.ui.lang.settings_tab_bt
import me.him188.ani.app.ui.lang.settings_tab_danmaku
import me.him188.ani.app.ui.lang.settings_tab_debug
import me.him188.ani.app.ui.lang.settings_tab_log
import me.him188.ani.app.ui.lang.settings_tab_media_selector
import me.him188.ani.app.ui.lang.settings_tab_media_source
import me.him188.ani.app.ui.lang.settings_tab_player
import me.him188.ani.app.ui.lang.settings_tab_proxy
import me.him188.ani.app.ui.lang.settings_tab_settings_backup
import me.him188.ani.app.ui.lang.settings_tab_storage
import me.him188.ani.app.ui.lang.settings_tab_theme
import me.him188.ani.app.ui.lang.settings_tab_update
import me.him188.ani.app.ui.settings.account.BangumiSyncTab
import me.him188.ani.app.ui.settings.account.ProfileGroup
import me.him188.ani.app.ui.settings.account.SelfInfoBanner
import me.him188.ani.app.ui.settings.framework.components.LocalSliderBackKeyExitsLeft
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import me.him188.ani.app.ui.settings.rendering.P2p
import me.him188.ani.app.ui.settings.tabs.AniHelperDestination
import me.him188.ani.app.ui.settings.tabs.DebugTab
import me.him188.ani.app.ui.settings.tabs.about.AboutTab
import me.him188.ani.app.ui.settings.tabs.about.AcknowledgementsTab
import me.him188.ani.app.ui.settings.tabs.about.DevelopersTab
import me.him188.ani.app.ui.settings.tabs.about.OpenSourceLibrariesTab
import me.him188.ani.app.ui.settings.tabs.app.AppearanceGroup
import me.him188.ani.app.ui.settings.tabs.app.PlayerGroup
import me.him188.ani.app.ui.settings.tabs.app.SoftwareUpdateGroup
import me.him188.ani.app.ui.settings.tabs.app.WatchTogetherGroup
import me.him188.ani.app.ui.settings.tabs.log.LogTab
import me.him188.ani.app.ui.settings.tabs.media.BackupSettings
import me.him188.ani.app.ui.settings.tabs.media.CacheDirectoryGroup
import me.him188.ani.app.ui.settings.tabs.media.MediaSelectionGroup
import me.him188.ani.app.ui.settings.tabs.media.TorrentEngineGroup
import me.him188.ani.app.ui.settings.tabs.media.PikPakAcceleratorGroup
import me.him188.ani.app.ui.settings.tabs.media.source.MediaSourceGroup
import me.him188.ani.app.ui.settings.tabs.media.source.MediaSourceSelectionActions
import me.him188.ani.app.ui.settings.tabs.media.source.MediaSourceSubscriptionGroup
import me.him188.ani.app.ui.settings.tabs.media.source.rememberMediaSourceSelectionState
import me.him188.ani.app.ui.settings.tabs.network.ConfigureProxyGroup
import me.him188.ani.app.ui.settings.tabs.network.ServerSelectionGroup
import me.him188.ani.app.ui.settings.tabs.network.BangumiMirrorSettingsGroup
import me.him188.ani.app.ui.settings.tabs.theme.ThemeGroup
import me.him188.ani.utils.platform.hasScrollingBug
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/**
 * @see getName \u67E5\u770B\u540D\u79F0
 */
typealias SettingsTab = me.him188.ani.app.navigation.SettingsTab

@Composable
fun SettingsScreen(
    vm: SettingsViewModel,
    onNavigateToEmailLogin: () -> Unit,
    onNavigateToBangumiOAuth: () -> Unit,
    loadOpenSourceLibrariesJsons: suspend () -> List<ByteArray>,
    modifier: Modifier = Modifier,
    initialTab: SettingsTab? = null,
    windowInsets: WindowInsets = AniWindowInsets.forColumnPageContent(),
    navigationIcon: @Composable () -> Unit = {},
) {
    // \u754C\u9762\u7F29\u653E\u6539\u52A8\u540E, \u79BB\u5F00\u8BBE\u7F6E\u9875\u65F6\u628A\u7A97\u53E3\u5C42 (\u5F39\u7A97/\u83DC\u5355) \u4E00\u5E76\u5BF9\u9F50
    UiScaleSyncEffect()

    val navigator: ThreePaneScaffoldNavigator<Nothing?> = rememberListDetailPaneScaffoldNavigator(
        initialDestinationHistory = buildList {
            add(ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.List))
            if (initialTab != null) {
                add(ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.Detail))
            }
        },
    )
    val layoutParameters = ListDetailLayoutParameters.calculate(navigator.scaffoldDirective)
    var lastSelectedTab by rememberSaveable(initialTab) {
        mutableStateOf(initialTab)
    }
    val mediaSourceSelectionState = rememberMediaSourceSelectionState()

    LaunchedEffect(Unit) {
        if (lastSelectedTab == null && !layoutParameters.preferSinglePane) {
            lastSelectedTab = SettingsTab.APPEARANCE
        }
    }
    LaunchedEffect(lastSelectedTab) {
        if (lastSelectedTab != SettingsTab.MEDIA_SOURCE) {
            mediaSourceSelectionState.clear()
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val browserNavigator = rememberAsyncBrowserNavigator()
    val context = LocalContext.current

    fun navigateToTab(tab: SettingsTab) {
        coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
            lastSelectedTab = tab
        }
    }

    SettingsPageLayout(
        navigator,
        // TODO: 2025/2/14 We should have a SettingsNavController or so to control the tab state
        { lastSelectedTab },
        onSelectedTab = { tab ->
            navigateToTab(tab)
        },
        onClickBackOnListPage = {
            coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
                navigator.navigateBack()
            }
        },
        onClickBackOnDetailPage = {
            if (mediaSourceSelectionState.inSelection) {
                mediaSourceSelectionState.clear()
            } else {
                coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
                    navigator.navigateBack(BackNavigationBehavior.PopUntilScaffoldValueChange)
                }
            }
        },
        navItems = {
            val selfInfoState by vm.selfInfoFlow.collectAsStateWithLifecycle()
            val bannerChecked by remember {
                derivedStateOf {
                    lastSelectedTab == SettingsTab.PROFILE
                }
            }
            SelfInfoBanner(
                selfInfoState,
                checked = bannerChecked,
                { navigateToTab(SettingsTab.PROFILE) },
                onNavigateToEmailLogin,
                Modifier.fillMaxWidth().tabFocusTarget(SettingsTab.PROFILE),
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            )

            Title(stringResource(Lang.settings_category_app_ui))
            Item(SettingsTab.APPEARANCE)
            Item(SettingsTab.THEME)

            Title(stringResource(Lang.settings_category_data_playback))
            Item(SettingsTab.PLAYER)
            Item(SettingsTab.MEDIA_SOURCE)
            Item(SettingsTab.MEDIA_SELECTOR)

            Title(stringResource(Lang.settings_category_network_storage))
            Item(SettingsTab.SERVER)
            Item(SettingsTab.PROXY)
            Item(SettingsTab.BT)
//            Item(SettingsTab.CACHE)
            Item(SettingsTab.STORAGE)

            Title(stringResource(Lang.settings_category_others))
            Item(SettingsTab.UPDATE)
            Item(SettingsTab.LOG)
            Item(SettingsTab.ABOUT)
            if (vm.isInDebugMode) {
                Item(SettingsTab.DEBUG)
            }
            Item(SettingsTab.SETTINGS_BACKUP)
        },
        tabContent = { currentTab ->
            val tabModifier = Modifier
            val toaster = LocalToaster.current
            val scope = rememberCoroutineScope()

            Column {
                when (currentTab) {
                    SettingsTab.ABOUT -> AboutTab(
                        vm.aboutTabInfo,
                        {
                            scope.launch {
                                if (vm.debugTriggerState.triggerDebugMode()) {
                                    toaster.toast(getString(Lang.settings_debug_mode_enabled))
                                }
                            }
                        },
                        onClickReleaseNotes = {
                            browserNavigator.openBrowser(
                                context,
                                AniHelperDestination.RELEASE_PREFIX + vm.aboutTabInfo.version,
                            )
                        },
                        onClickWebsite = { browserNavigator.openBrowser(context, AniHelperDestination.ANI_WEBSITE) },
                        onClickFeedback = { browserNavigator.openBrowser(context, AniHelperDestination.ISSUE_TRACKER) },
                        onClickSource = { browserNavigator.openBrowser(context, AniHelperDestination.GITHUB_HOME) },
                        onClickDevelopers = {
                            navigateTo(DetailPaneRoutes.Developers)
                        },
                        onClickAcknowledgements = {
                            navigateTo(DetailPaneRoutes.Acknowledgements)
                        },
                        modifier = tabModifier,
                    )

                    SettingsTab.LOG -> LogTab(
                        onClickFeedback = { browserNavigator.openBrowser(context, AniHelperDestination.ISSUE_TRACKER) },
                    )

                    SettingsTab.DEBUG -> DebugTab(
                        vm.debugSettingsState,
                        tabModifier,
                    )

                    else -> SettingsTab(
                        tabModifier,
                    ) {
                        when (currentTab) {
                            SettingsTab.PROFILE -> ProfileGroup(
                                onNavigateToEmail = onNavigateToEmailLogin,
                                onNavigateToBangumiSync = {
                                    navigateTo(DetailPaneRoutes.BangumiSync)
                                },
                                onNavigateToBangumiOAuth = onNavigateToBangumiOAuth,
                            )

                            SettingsTab.APPEARANCE -> AppearanceGroup(vm.uiSettings, vm.themeSettings)
                            SettingsTab.THEME -> ThemeGroup(vm.themeSettings)
                            SettingsTab.UPDATE -> SoftwareUpdateGroup(vm.softwareUpdateGroupState)
                            SettingsTab.PLAYER -> {
                                PlayerGroup(
                                    vm.videoScaffoldConfig,
                                    vm.playerKernelConfig,
                                    vm.danmakuFilterConfigState,
                                    vm.danmakuRegexFilterState,
                                    vm.isInDebugMode,
                                    vm.themeSettings,
                                )
                                WatchTogetherGroup(vm.watchTogetherSettings)
                            }

                            SettingsTab.MEDIA_SOURCE -> {
                                MediaSourceSubscriptionGroup(
                                    vm.mediaSourceSubscriptionGroupState,
                                )
                                MediaSourceGroup(
                                    vm.mediaSourceGroupState,
                                    vm.editMediaSourceState,
                                    mediaSourceSelectionState,
                                )
                            }

                            SettingsTab.MEDIA_SELECTOR -> MediaSelectionGroup(vm.mediaSelectionGroupState)
                            SettingsTab.SERVER -> ServerSelectionGroup(vm.danmakuSettingsState, vm.danmakuServerTesters)
                            SettingsTab.PROXY -> {
                                ConfigureProxyGroup(
                                    state = vm.configureProxyState,
                                    onStartProxyTestLoop = { vm.startProxyTesterLoop() },
                                )
                                HorizontalDividerItem()
                                BangumiMirrorSettingsGroup(
                                    settings = vm.bangumiMirrorSettings,
                                    backgroundScope = vm.backgroundScope,
                                )
                            }

                            SettingsTab.BT -> {
                                TorrentEngineGroup(vm.torrentSettingsState)
                                PikPakAcceleratorGroup(
                                    vm.pikpakSettingsState,
                                    vm.mediaSelectorSettingsState,
                                    vm.pikpakConnectionTester,
                                )
                            }
//                            SettingsTab.CACHE -> AutoCacheGroup(vm.mediaCacheSettingsState)
                            SettingsTab.STORAGE -> CacheDirectoryGroup(vm.cacheDirectoryGroupState)
                            SettingsTab.SETTINGS_BACKUP -> BackupSettings(vm.cacheDirectoryGroupState)
                            SettingsTab.ABOUT -> {} // see above
                            SettingsTab.DEBUG -> {}
                            SettingsTab.LOG -> {}
                            null -> {}
                        }
                    }
                }
                if (currentTab == SettingsTab.MEDIA_SOURCE) {
                    AniAnimatedVisibility(mediaSourceSelectionState.inSelection) {
                        Spacer(Modifier.height(80.dp))
                    }
                }
                Spacer(
                    Modifier.height(
                        currentWindowAdaptiveInfo1().windowSizeClass.paneVerticalPadding,
                    ),
                )
            }
        },
        detailPaneBottomBar = { currentTab, bottomBarInsets ->
            // \u6D6E\u52A8\u5DE5\u5177\u680F\u53EA\u7ED9\u6307\u9488\u8BBE\u5907: \u9065\u63A7\u5668\u4E0A\u591F\u5230\u5E95\u90E8\u8FD9\u6761\u8981\u7A7F\u8FC7\u6574\u9875\u8BBE\u7F6E\u9879,
            // \u6539\u6210\u957F\u6309\u9009\u4E2D\u9879\u51FA\u4E0B\u62C9\u83DC\u5355 (\u89C1 MediaSourceGroup)
            if (currentTab == SettingsTab.MEDIA_SOURCE && !LocalAniUiBehavior.current.focusDrivenNavigation) {
                AniAnimatedVisibility(
                    visible = mediaSourceSelectionState.inSelection,
                    modifier = Modifier.align(Alignment.BottomCenter),
                ) {
                    MediaSourceSelectionActions(
                        mediaSources = vm.mediaSourceGroupState.mediaSources,
                        selectionState = mediaSourceSelectionState,
                        editState = vm.editMediaSourceState,
                        windowInsets = bottomBarInsets,
                    )
                }
            }
        },
        modifier = modifier,
        contentWindowInsets = windowInsets,
        navigationIcon = navigationIcon,
        layoutParameters = layoutParameters,
        loadOpenSourceLibrariesJsons = loadOpenSourceLibrariesJsons,
    )
}

@Composable
internal fun SettingsPageLayout(
    navigator: ThreePaneScaffoldNavigator<Nothing?>,
    currentTab: () -> SettingsTab?,
    onSelectedTab: (SettingsTab) -> Unit,
    onClickBackOnListPage: () -> Unit,
    onClickBackOnDetailPage: () -> Unit,
    navItems: @Composable (SettingsDrawerScope.() -> Unit),
    tabContent: @Composable SettingsDetailPaneScope.(currentTab: SettingsTab?) -> Unit, // inside Column verticalScroll
    detailPaneBottomBar: @Composable BoxScope.(currentTab: SettingsTab?, windowInsets: WindowInsets) -> Unit =
        { _, _ -> },
    modifier: Modifier = Modifier,
    contentWindowInsets: WindowInsets = AniWindowInsets.forColumnPageContent(),
    containerColor: Color = AniThemeDefaults.pageContentBackgroundColor,
    layoutParameters: ListDetailLayoutParameters = ListDetailLayoutParameters.calculate(navigator.scaffoldDirective),
    navigationIcon: @Composable () -> Unit = {},
    loadOpenSourceLibrariesJsons: suspend () -> List<ByteArray>,
) = SettingsPageSurface(containerColor) {
    val layoutParametersState by rememberUpdatedState(layoutParameters)

    @Stable
    fun SettingsTab?.orDefault(): SettingsTab? {
        return if (layoutParametersState.preferSinglePane) {
            // \u5355\u9875\u6A21\u5F0F, \u81EA\u52A8\u9009\u62E9\u4F20\u5165\u7684 tab
            this
        } else {
            // \u53CC\u9875\u6A21\u5F0F, \u9ED8\u8BA4\u9009\u62E9\u7B2C\u4E00\u4E2A tab, \u4EE5\u514D\u53F3\u8FB9\u5F88\u7A7A
            this ?: SettingsTab.Default
        }
    }

    // \u6BDB\u73BB\u7483\u6A21\u5F0F\u4E0B\u9876\u680F\u8986\u76D6\u5728\u5185\u5BB9\u4E0A\u65B9\u5E76\u4FDD\u6301\u5E38\u9A7B, \u4EE5\u4FBF\u5C55\u793A\u6A21\u7CCA\u6548\u679C.
    val frostedGlassActive = isAppChromeFrostedGlassActive()

    val uiBehavior = LocalAniUiBehavior.current

    // \u9065\u63A7\u5668: \u8BE6\u60C5\u680F\u6309\u5DE6\u952E\u56DE\u5230\u5DE6\u4FA7\u5BFC\u822A\u7684\u201C\u5F53\u524D\u9009\u4E2D\u9879\u201D (\u9ED8\u8BA4\u7A7A\u95F4\u7126\u70B9\u641C\u7D22\u53EA\u627E\u51E0\u4F55\u6700\u8FD1\u90BB,
    // \u4F1A\u843D\u5230\u6CA1\u9009\u4E2D\u7684\u9879\u4E0A). \u8BF7\u6C42\u5668\u7531 tabFocusTarget \u6302\u5728\u9009\u4E2D\u9879\u4E0A, \u5DE6\u4FA7\u5BFC\u822A\u5217\u8868 focusGroup
    // \u7684 onEnter \u8D1F\u8D23\u91CD\u5B9A\u5411; slider \u805A\u7126\u65F6\u5DE6\u952E\u88AB\u8C03\u503C\u6D88\u8D39, \u7531\u8FD4\u56DE\u952E\u4EE3\u66FF (\u89C1 SliderItem).
    val focusDriven = uiBehavior.focusDrivenNavigation
    val selectedNavItemFocus = remember { FocusRequester() }

    val usePinnedTopAppBar = LocalPlatform.current.hasScrollingBug() || uiBehavior.pinTopAppBar
    val listPaneTopAppBarScrollBehavior = if (usePinnedTopAppBar) {
        TopAppBarDefaults.pinnedScrollBehavior()
    } else {
        TopAppBarDefaults.enterAlwaysScrollBehavior()
    }

    val detailPaneTopAppBarScrollBehavior = if (usePinnedTopAppBar) {
        TopAppBarDefaults.pinnedScrollBehavior()
    } else {
        TopAppBarDefaults.enterAlwaysScrollBehavior()
    }

    val listPaneScrollState = rememberScrollState()
    val topAppBarSize = if (LocalPlatform.current.hasScrollingBug()) {
        TopAppBarSize.SMALL
    } else {
        val windowSizeClass = currentWindowAdaptiveInfo1().windowSizeClass
        when {
            windowSizeClass.isHeightAtLeastExpanded -> TopAppBarSize.LARGE
            windowSizeClass.isHeightAtLeastMedium -> TopAppBarSize.MEDIUM
            else -> TopAppBarSize.SMALL
        }
    }
    val listPaneTopAppBar: @Composable PaneScope.() -> Unit = {
        AniTopAppBar(
            title = { AniTopAppBarDefaults.Title(stringResource(Lang.settings)) },
            navigationIcon = {
                if (navigator.canNavigateBack()) {
                    BackNavigationIconButton(
                        onNavigateBack = {
                            onClickBackOnListPage()
                        },
                    )
                } else {
                    navigationIcon()
                }
            },
            colors = if (isSinglePane) {
                TopAppBarDefaults.topAppBarColors(
                    containerColor = containerColor,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                )
            } else {
                TopAppBarDefaults.topAppBarColors(
                    containerColor = containerColor,
                    scrolledContainerColor = containerColor,
                )
            },
            scrollBehavior = listPaneTopAppBarScrollBehavior,
            windowInsets = paneContentWindowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
            size = topAppBarSize,
        )
    }
    // \u8FD4\u56DE\u6309\u94AE\u9690\u85CF\u65F6, \u5217\u8868\u4FA7\u9876\u680F\u53EA\u5269\u201C\u8BBE\u7F6E\u201D\u6807\u9898\u5360\u4F4D, \u6574\u6761\u4E0D\u6E32\u67D3
    val hideNavigationTopAppBar = !uiBehavior.showNavigationTopAppBar
    AniListDetailPaneScaffold(
        navigator,
        // \u6BDB\u73BB\u7483\u6A21\u5F0F\u4E0B\u9876\u680F\u7531 listPaneContent \u5185\u90E8\u8986\u76D6\u7ED8\u5236.
        listPaneTopAppBar = if (frostedGlassActive || hideNavigationTopAppBar) null else listPaneTopAppBar,
        listPaneContent = paneScope@{
            var listTopAppBarHeight by remember { mutableStateOf(0) }
            val drawerSheet: @Composable PaneScope.() -> Unit = {
                PermanentDrawerSheet(
                    Modifier
                        .paneContentPadding(extraStart = (-8).dp, extraEnd = (-8).dp)
                        .paneWindowInsetsPadding()
                        .fillMaxWidth()
                        .nestedScroll(listPaneTopAppBarScrollBehavior.nestedScrollConnection)
                        .verticalScroll(listPaneScrollState)
                        .ifThen(focusDriven) {
                            // \u9065\u63A7\u5668: \u4ECE\u8BE6\u60C5\u680F\u56DE\u5230\u5BFC\u822A\u5217\u8868\u65F6, \u7126\u70B9\u6062\u590D\u5230\u4E0A\u6B21\u5728\u5217\u8868\u91CC\u505C\u7559\u7684\u9879
                            // (\u6CA1\u6709\u5386\u53F2\u65F6\u624D\u843D\u5230\u5F53\u524D\u9009\u4E2D\u9879), \u800C\u4E0D\u662F\u6BCF\u6B21\u90FD\u62C9\u56DE\u9009\u4E2D\u9879\u6216\u51E0\u4F55\u6700\u8FD1\u90BB.
                            // \u6CE8\u610F\u4E0D\u80FD\u53CD\u8FC7\u6765\u5728\u8BE6\u60C5\u4FA7\u5305\u7EC4\u62E6 onExit: \u65B9\u5411\u641C\u7D22\u662F\u5206\u5C42\u7684, \u8BE6\u60C5\u9875\u6EDA\u52A8
                            // scope \u5185\u7684\u515C\u5E95\u5019\u9009 (\u5982\u8C03\u8272\u677F) \u4F1A\u628A\u201C\u5411\u5DE6\u201D\u6D88\u5316\u5728\u7EC4\u5185, \u79BB\u7EC4\u94A9\u5B50
                            // \u6839\u672C\u4E0D\u89E6\u53D1.
                            focusRestorer(fallback = selectedNavItemFocus).focusGroup()
                        },
                    drawerContainerColor = Color.Unspecified,
                ) {
                    val highlightSelectedItemState = rememberUpdatedState(layoutParametersState.highlightSelectedItem)
                    val scope = remember(this, navigator, currentTab, highlightSelectedItemState) {
                        object : SettingsDrawerScope(), ColumnScope by this {
                            @Composable
                            override fun Item(item: SettingsTab) {
                                NavigationDrawerItem(
                                    icon = { Icon(getIcon(item), contentDescription = null) },
                                    label = { Text(getName(item)) },
                                    selected = item == currentTab() && highlightSelectedItemState.value,
                                    onClick = {
                                        onSelectedTab(item)
                                    },
                                    modifier = Modifier.tabFocusTarget(item),
                                )
                            }

                            override fun Modifier.tabFocusTarget(tab: SettingsTab): Modifier =
                                // \u515C\u5E95\u5230\u9ED8\u8BA4 tab: focusRestorer \u7684 fallback \u4F1A\u65E0\u6761\u4EF6 requestFocus,
                                // \u8BF7\u6C42\u5668\u5FC5\u987B\u59CB\u7EC8\u6302\u5728\u67D0\u4E2A\u8282\u70B9\u4E0A, \u5426\u5219\u672A\u9009\u4E2D\u4EFB\u4F55 tab \u7684\u9996\u5E27\u4F1A\u629B\u5F02\u5E38
                                ifThen(tab == (currentTab() ?: SettingsTab.Default)) {
                                    focusRequester(selectedNavItemFocus)
                                }
                        }
                    }


                    val verticalPadding = currentWindowAdaptiveInfo1().windowSizeClass.paneVerticalPadding

                    // \u6BDB\u73BB\u7483\u9876\u680F\u8986\u76D6\u5728\u5185\u5BB9\u4E0A\u65B9\u65F6, \u5728\u6EDA\u52A8\u5185\u5BB9\u9876\u90E8\u7559\u51FA\u9876\u680F\u7684\u7A7A\u95F4.
                    if (frostedGlassActive) {
                        Spacer(Modifier.height(with(LocalDensity.current) { listTopAppBarHeight.toDp() })) // scrollable
                    }
                    Spacer(Modifier.height(verticalPadding - 8.dp)) // scrollable
                    navItems(scope)
                    Spacer(Modifier.height(verticalPadding)) // scrollable
                }
            }

            if (frostedGlassActive) {
                Box {
                    Box(
                        Modifier
                            .fillMaxSize()
                            // \u9876\u680F\u8986\u76D6\u5728\u5185\u5BB9\u4E0A, \u8FD9\u91CC\u4EE3\u66FF scaffold \u6D88\u8017\u9876\u680F\u7684 insets.
                            .consumeWindowInsets(paneContentWindowInsets.only(WindowInsetsSides.Top))
                            .appChromeHazeSource(backgroundColor = containerColor),
                    ) {
                        drawerSheet()
                    }
                    if (!hideNavigationTopAppBar) {
                        Box(Modifier.onSizeChanged { listTopAppBarHeight = it.height }) {
                            listPaneTopAppBar()
                        }
                    }
                }
            } else {
                drawerSheet()
            }
        },
        // empty because our detailPaneContent already has it
        detailPane = {
            AnimatedContent(
                currentTab(),
                Modifier.fillMaxSize(),
                transitionSpec = LocalAniMotionScheme.current.animatedContent.topLevel,
            ) { navigationTab ->
                val navMotionScheme = NavigationMotionScheme.current
                val topAppBarWindowInsets =
                    paneContentWindowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                val topAppBarColors = AniThemeDefaults.topAppBarColors(
                    containerColor = if (isSinglePane) {
                        containerColor
                    } else {
                        MaterialTheme.colorScheme.surfaceContainer
                    },
                )
                val detailPaneBackStack = rememberSaveable(saver = DetailPaneBackStackSaver) {
                    mutableStateListOf<DetailPaneRoutes>(DetailPaneRoutes.Main)
                }
                // \u6808\u5E95\u7684 Main \u4E0D\u80FD\u88AB\u5F39\u51FA, \u7A7A\u6808\u4F1A\u8BA9 NavDisplay \u629B\u5F02\u5E38
                val navigateUp: () -> Unit = {
                    if (detailPaneBackStack.size > 1) {
                        detailPaneBackStack.removeAt(detailPaneBackStack.lastIndex)
                    }
                }

                @Composable
                fun PaneScope.RouteContent(
                    scrollable: Boolean = true,
                    content: @Composable SettingsDetailPaneScope.() -> Unit,
                ) {
                    val paneScope = this
                    val scope = remember(paneScope, detailPaneBackStack) {
                        object : SettingsDetailPaneScope, PaneScope by paneScope {
                            override fun navigateTo(route: DetailPaneRoutes) {
                                // \u540C\u4E00\u4E2A\u9875\u9762\u91CD\u590D\u5165\u6808\u4F1A\u8BA9\u6808\u91CC\u51FA\u73B0\u76F8\u540C\u7684 key, NavDisplay \u4E0D\u5141\u8BB8
                                if (detailPaneBackStack.lastOrNull() != route) {
                                    detailPaneBackStack.add(route)
                                }
                            }

                            override fun navigateUp() {
                                if (detailPaneBackStack.size > 1) {
                                    detailPaneBackStack.removeAt(detailPaneBackStack.lastIndex)
                                }
                            }
                        }
                    }
                    Column(
                        Modifier
                            .ifThen(scrollable) {
                                verticalScroll(rememberScrollState())
                            }
                            .ifThen(focusDriven) {
                                // \u4E0E\u5DE6\u680F\u5BF9\u79F0\u7684\u505C\u7559\u5386\u53F2: \u4ECE\u5DE6\u680F\u56DE\u5230\u8BE6\u60C5\u680F\u65F6\u6062\u590D\u4E0A\u6B21\u805A\u7126\u7684\u8BBE\u7F6E\u9879,
                                // \u65E0\u5386\u53F2 (\u9996\u6B21\u8FDB\u5165/\u5207\u4E86 tab \u539F\u8282\u70B9\u5DF2\u4E0D\u5728) \u65F6\u8D70\u9ED8\u8BA4\u7A7A\u95F4\u8FDB\u5165.
                                // \u5FC5\u987B\u6302\u5728 verticalScroll \u4E4B\u540E (\u6EDA\u52A8 scope \u4E4B\u5185): \u6EDA\u52A8\u5BB9\u5668\u81EA\u5E26
                                // \u7126\u70B9\u8282\u70B9, restorer \u7684\u5B58/\u53D6\u53EA\u770B\u76F4\u63A5\u5B50\u7126\u70B9\u8282\u70B9, \u5305\u5728\u6EDA\u52A8\u5916\u9762
                                // \u5B58\u5230\u7684\u662F\u6EDA\u52A8\u8282\u70B9\u672C\u8EAB, \u6062\u590D\u4E0D\u4E86\u5177\u4F53\u6761\u76EE.
                                focusRestorer().focusGroup()
                            }
                            .padding(horizontal = SettingsScope.itemExtraHorizontalPadding)
                            .fillMaxWidth()
                            .wrapContentWidth()
                            .widthIn(max = 1000.dp),
                    ) {
                        // \u6BDB\u73BB\u7483\u9876\u680F\u8986\u76D6\u5728\u5185\u5BB9\u4E0A\u65B9\u65F6, \u5728\u6EDA\u52A8\u5185\u5BB9\u9876\u90E8\u7559\u51FA\u9876\u680F\u7684\u7A7A\u95F4
                        val topAppBarUnderlapHeight = LocalSettingsTopAppBarUnderlapHeight.current
                        if (topAppBarUnderlapHeight > 0) {
                            Spacer(Modifier.height(with(LocalDensity.current) { topAppBarUnderlapHeight.toDp() }))
                        }

                        CompositionLocalProvider(LocalSliderBackKeyExitsLeft provides focusDriven) {
                            scope.content()
                        }

                        // \u6EDA\u52A8\u5BB9\u5668\u5E95\u90E8\u7559\u51FA\u5B89\u5168\u533A\u57DF
                        Spacer(
                            Modifier.windowInsetsBottomHeight(
                                AniWindowInsets.safeDrawing,
                            ),
                        )
                    }
                }

                NavDisplay(
                    backStack = detailPaneBackStack,
                    onBack = navigateUp,
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(),
                    ),
                    transitionSpec = {
                        navMotionScheme.enterTransition togetherWith navMotionScheme.exitTransition
                    },
                    popTransitionSpec = {
                        navMotionScheme.popEnterTransition togetherWith navMotionScheme.popExitTransition
                    },
                    predictivePopTransitionSpec = {
                        navMotionScheme.popEnterTransition togetherWith navMotionScheme.popExitTransition
                    },
                    entryProvider = entryProvider {
                    entry<DetailPaneRoutes.Main> {
                        val tab = navigationTab.orDefault()
                        DetailPaneRoute(
                            topAppBar = {
                                tab?.let {
                                    AniTopAppBar(
                                        title = {
                                            AniTopAppBarDefaults.Title(getName(it))
                                        },
                                        navigationIcon = {
                                            if (listDetailLayoutParameters.preferSinglePane) {
                                                BackNavigationIconButton(onClickBackOnDetailPage)
                                            }
                                        },
                                        colors = topAppBarColors,
                                        windowInsets = topAppBarWindowInsets,
                                        size = topAppBarSize,
                                        scrollBehavior = detailPaneTopAppBarScrollBehavior,
                                    )
                                }
                            },
                            detailPaneTopAppBarScrollBehavior,
                            tabContent = {
                                RouteContent {
                                    tabContent(tab)
                                }
                            },
                            floatingContent = {
                                detailPaneBottomBar(
                                    tab,
                                    paneContentWindowInsets.only(
                                        WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal,
                                    ),
                                )
                            },
                        )
                    }
                    entry<DetailPaneRoutes.Acknowledgements> {
                        DetailPaneRoute(
                            topAppBar = {
                                AniTopAppBar(
                                    title = { AniTopAppBarDefaults.Title(stringResource(Lang.acknowledgements)) },
                                    navigationIcon = {
                                        BackNavigationIconButton(navigateUp)
                                    },
                                    colors = topAppBarColors,
                                    windowInsets = topAppBarWindowInsets,
                                    size = topAppBarSize,
                                    scrollBehavior = detailPaneTopAppBarScrollBehavior,
                                )
                            },
                            detailPaneTopAppBarScrollBehavior,
                        ) {
                            RouteContent {
                                AcknowledgementsTab(
                                    onClickOpenSourceLicenses = {
                                        navigateTo(DetailPaneRoutes.OpenSourceLicenses)
                                    },
                                    Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }
                    entry<DetailPaneRoutes.OpenSourceLicenses> {
                        DetailPaneRoute(
                            topAppBar = {
                                AniTopAppBar(
                                    title = {
                                        AniTopAppBarDefaults.Title(
                                            stringResource(Lang.settings_acknowledgements_oss_licenses),
                                        )
                                    },
                                    navigationIcon = {
                                        BackNavigationIconButton(navigateUp)
                                    },
                                    colors = topAppBarColors,
                                    windowInsets = topAppBarWindowInsets,
                                    size = topAppBarSize,
                                    scrollBehavior = detailPaneTopAppBarScrollBehavior,
                                )
                            },
                            detailPaneTopAppBarScrollBehavior,
                        ) {
                            // LibrariesContainer \u81EA\u5E26 LazyColumn, \u4E0D\u80FD\u5957\u5728 verticalScroll \u91CC
                            RouteContent(scrollable = false) {
                                OpenSourceLibrariesTab(
                                    loadOpenSourceLibrariesJsons,
                                    Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }
                    entry<DetailPaneRoutes.Developers> {
                        DetailPaneRoute(
                            topAppBar = {
                                AniTopAppBar(
                                    title = { AniTopAppBarDefaults.Title(stringResource(Lang.developer_list)) },
                                    navigationIcon = {
                                        BackNavigationIconButton(navigateUp)
                                    },
                                    colors = topAppBarColors,
                                    windowInsets = topAppBarWindowInsets,
                                    size = topAppBarSize,
                                    scrollBehavior = detailPaneTopAppBarScrollBehavior,
                                )
                            },
                            detailPaneTopAppBarScrollBehavior,
                        ) {
                            RouteContent {
                                DevelopersTab(Modifier.fillMaxSize())
                            }
                        }
                    }
                    entry<DetailPaneRoutes.BangumiSync> {
                        DetailPaneRoute(
                            topAppBar = {
                                AniTopAppBar(
                                    title = { AniTopAppBarDefaults.Title("Bangumi \u540C\u6B65") },
                                    navigationIcon = {
                                        BackNavigationIconButton(navigateUp)
                                    },
                                    colors = topAppBarColors,
                                    windowInsets = topAppBarWindowInsets,
                                    size = topAppBarSize,
                                    scrollBehavior = detailPaneTopAppBarScrollBehavior,
                                )
                            },
                            detailPaneTopAppBarScrollBehavior,
                        ) {
                            RouteContent(scrollable = false) {
                                BangumiSyncTab()
                            }
                        }
                    }
                    },
                )
            }
        },
        modifier,
        layoutParameters = layoutParameters,
        contentWindowInsets = contentWindowInsets,
    )
}

/**
 * \u8BBE\u7F6E\u9875\u81EA\u5E26\u4E00\u4E2A\u72EC\u7ACB\u7684\u6BDB\u73BB\u7483\u4F5C\u7528\u57DF: \u542F\u7528\u6BDB\u73BB\u7483\u65F6, \u9876\u680F\u6A21\u7CCA\u5176\u4E0B\u65B9\u6EDA\u52A8\u7684\u5185\u5BB9.
 */
@Composable
private fun SettingsPageSurface(containerColor: Color, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAppChromeHazeState provides rememberHazeState()) {
        Surface(color = containerColor, content = content)
    }
}

/**
 * \u6BDB\u73BB\u7483\u9876\u680F\u8986\u76D6\u5728 pane \u5185\u5BB9\u4E0A\u65B9\u65F6, \u6EDA\u52A8\u5185\u5BB9\u9700\u8981\u5728\u9876\u90E8\u7559\u51FA\u7684\u7A7A\u95F4 (px).
 *
 * \u4E0D\u542F\u7528\u6BDB\u73BB\u7483\u65F6\u4E3A 0.
 */
private val LocalSettingsTopAppBarUnderlapHeight = compositionLocalOf { 0 }

@Stable
interface SettingsDetailPaneScope : PaneScope {
    /**
     * \u5728\u8BE6\u60C5\u9875\u5185\u90E8\u5BFC\u822A\u5230 [route]. \u5982\u679C\u5B83\u5DF2\u7ECF\u5728\u6808\u9876\u5219\u4E0D\u505A\u4EFB\u4F55\u64CD\u4F5C.
     */
    fun navigateTo(route: DetailPaneRoutes)

    /**
     * \u8FD4\u56DE\u8BE6\u60C5\u9875\u5185\u90E8\u7684\u4E0A\u4E00\u9875. \u5DF2\u7ECF\u5728 [DetailPaneRoutes.Main] \u65F6\u4E0D\u505A\u4EFB\u4F55\u64CD\u4F5C.
     */
    fun navigateUp()
}

@Composable
private fun PaneScope.DetailPaneRoute(
    topAppBar: @Composable () -> Unit,
    detailPaneTopAppBarScrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    floatingContent: @Composable BoxScope.() -> Unit = {},
    tabContent: @Composable (PaneScope.() -> Unit),
) {
    // \u8FD4\u56DE\u6309\u94AE\u9690\u85CF\u65F6, \u8BE6\u60C5\u4FA7\u9876\u680F\u53EA\u5269\u6807\u9898\u5360\u4F4D, \u6574\u6761\u4E0D\u6E32\u67D3 (\u5F53\u524D\u5206\u7C7B\u7531\u5DE6\u4FA7\u5BFC\u822A\u9879\u9AD8\u4EAE\u627F\u62C5)
    @Suppress("NAME_SHADOWING")
    val topAppBar: @Composable () -> Unit =
        if (LocalAniUiBehavior.current.showNavigationTopAppBar) topAppBar else ({})
    if (isAppChromeFrostedGlassActive()) {
        // \u6BDB\u73BB\u7483: \u9876\u680F\u8986\u76D6\u5728\u5185\u5BB9\u4E0A\u65B9, \u5185\u5BB9\u4ECE\u9876\u680F\u4E0B\u65B9\u6EDA\u8FC7\u5E76\u88AB\u6A21\u7CCA.
        // \u5185\u5BB9\u901A\u8FC7 LocalSettingsTopAppBarUnderlapHeight \u5728\u6EDA\u52A8\u5185\u5BB9\u9876\u90E8\u7559\u51FA\u9876\u680F\u7684\u7A7A\u95F4.
        var topAppBarHeight by remember { mutableStateOf(0) }
        Box(modifier) {
            Box(
                Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(paneContentWindowInsets.only(WindowInsetsSides.Top))
                    .appChromeHazeSource(backgroundColor = AniThemeDefaults.pageContentBackgroundColor),
            ) {
                Column(
                    Modifier
                        .paneContentPadding(
                            extraStart = -SettingsScope.itemHorizontalPadding,
                            extraEnd = -SettingsScope.itemHorizontalPadding,
                        )
                        .paneWindowInsetsPadding()
                        .nestedScroll(detailPaneTopAppBarScrollBehavior.nestedScrollConnection),
                ) {
                    CompositionLocalProvider(LocalSettingsTopAppBarUnderlapHeight provides topAppBarHeight) {
                        tabContent()
                    }
                }
            }
            Box(Modifier.onSizeChanged { topAppBarHeight = it.height }) {
                topAppBar()
            }
            floatingContent()
        }
        return
    }

    Column(modifier) {
        topAppBar()

        Box(
            Modifier
                .fillMaxHeight()
                .consumeWindowInsets(paneContentWindowInsets.only(WindowInsetsSides.Top)),
        ) {
            Column(
                Modifier
                    .paneContentPadding(
                        extraStart = -SettingsScope.itemHorizontalPadding,
                        extraEnd = -SettingsScope.itemHorizontalPadding,
                    )
                    .paneWindowInsetsPadding()
                    .nestedScroll(detailPaneTopAppBarScrollBehavior.nestedScrollConnection),
            ) {
                tabContent()
            }
            floatingContent()
        }
    }
}

/**
 * \u8BBE\u7F6E\u8BE6\u60C5\u9875\u5185\u90E8\u7684\u5BFC\u822A\u76EE\u6807. \u6808\u5E95\u603B\u662F [Main].
 */
@Serializable
sealed class DetailPaneRoutes : NavKey {
    @Serializable
    data object Main : DetailPaneRoutes()

    @Serializable
    data object Acknowledgements : DetailPaneRoutes()

    @Serializable
    data object OpenSourceLicenses : DetailPaneRoutes()

    @Serializable
    data object Developers : DetailPaneRoutes()

    @Serializable
    data object BangumiSync : DetailPaneRoutes()
}

private val DetailPaneBackStackSaver: Saver<SnapshotStateList<DetailPaneRoutes>, Any> = listSaver(
    save = { stack -> stack.map { it::class.simpleName ?: "Main" } },
    restore = { saved ->
        // \u7A7A\u6808\u4F1A\u8BA9 NavDisplay \u629B\u5F02\u5E38, \u6B64\u65F6\u653E\u5F03\u6062\u590D
        if (saved.isEmpty()) {
            null
        } else {
            saved.map { name ->
                when (name as String) {
                    "Acknowledgements" -> DetailPaneRoutes.Acknowledgements
                    "OpenSourceLicenses" -> DetailPaneRoutes.OpenSourceLicenses
                    "Developers" -> DetailPaneRoutes.Developers
                    "BangumiSync" -> DetailPaneRoutes.BangumiSync
                    else -> DetailPaneRoutes.Main
                }
            }.toMutableStateList()
        }
    },
)

@Stable
abstract class SettingsDrawerScope internal constructor() : ColumnScope {
    @Composable
    abstract fun Item(item: SettingsTab)

    /**
     * \u628A\u6B64\u8282\u70B9\u6807\u8BB0\u4E3A [tab] \u9009\u4E2D\u65F6\u7684\u7126\u70B9\u56DE\u5F52\u76EE\u6807: \u9065\u63A7\u5668\u4E0A\u8BE6\u60C5\u680F\u6309\u5DE6\u952E\u8DF3\u56DE\u5B83.
     * [tab] \u975E\u5F53\u524D\u9009\u4E2D\u9879\u65F6\u65E0\u6548\u679C. [Item] \u5DF2\u81EA\u52A8\u6302\u8F7D, \u5217\u8868\u91CC\u4E0D\u7ECF [Item] \u6E32\u67D3\u7684\u5165\u53E3
     * (\u5982\u8D26\u53F7\u6A2A\u5E45) \u9700\u8981\u81EA\u884C\u6302\u5230\u53EF\u805A\u7126\u7684\u6839 modifier \u4E0A.
     */
    abstract fun Modifier.tabFocusTarget(tab: SettingsTab): Modifier

    @Composable
    fun Title(text: String, paddingTop: Dp = 20.dp) {
        Text(
            text,
            Modifier
                .padding(horizontal = 16.dp)
                .padding(top = paddingTop, bottom = 12.dp),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Stable
private fun getIcon(tab: SettingsTab): ImageVector {
    return when (tab) {
        SettingsTab.PROFILE -> Icons.Outlined.AccountCircle
        SettingsTab.APPEARANCE -> Icons.Outlined.SettingsApplications
        SettingsTab.THEME -> Icons.Outlined.Palette
        SettingsTab.UPDATE -> Icons.Outlined.Update
        SettingsTab.PLAYER -> Icons.Outlined.SmartDisplay
        SettingsTab.MEDIA_SOURCE -> Icons.Outlined.Subscriptions
        SettingsTab.MEDIA_SELECTOR -> Icons.Outlined.FilterList
        SettingsTab.SERVER -> Icons.Outlined.Public
        SettingsTab.PROXY -> Icons.Outlined.VpnKey
        SettingsTab.BT -> Icons.Filled.P2p
//        SettingsTab.CACHE -> Icons.Rounded.Download // Icons.Outlined.Download \u592A sharp \u4E86
        SettingsTab.STORAGE -> Icons.Outlined.Storage
        SettingsTab.SETTINGS_BACKUP -> Icons.Outlined.Settings
        SettingsTab.ABOUT -> Icons.Outlined.Info
        SettingsTab.LOG -> Icons.Outlined.Feedback
        SettingsTab.DEBUG -> Icons.Outlined.Science
    }
}

@Stable
@Composable
private fun getName(tab: SettingsTab): String {
    return when (tab) {
        SettingsTab.PROFILE -> stringResource(Lang.settings_tab_account)
        SettingsTab.APPEARANCE -> stringResource(Lang.settings_tab_appearance)
        SettingsTab.THEME -> stringResource(Lang.settings_tab_theme)
        SettingsTab.PLAYER -> stringResource(Lang.settings_tab_player)
        SettingsTab.MEDIA_SOURCE -> stringResource(Lang.settings_tab_media_source)
        SettingsTab.MEDIA_SELECTOR -> stringResource(Lang.settings_tab_media_selector)
        SettingsTab.SERVER -> stringResource(Lang.settings_tab_danmaku)
        SettingsTab.PROXY -> stringResource(Lang.settings_tab_proxy)
        SettingsTab.BT -> stringResource(Lang.settings_tab_bt)
//        SettingsTab.CACHE -> stringResource(Lang.settings_tab_cache)
        SettingsTab.STORAGE -> stringResource(Lang.settings_tab_storage)
        SettingsTab.SETTINGS_BACKUP -> stringResource(Lang.settings_tab_settings_backup)
        SettingsTab.LOG -> stringResource(Lang.settings_tab_log)
        SettingsTab.UPDATE -> stringResource(Lang.settings_tab_update)
        SettingsTab.ABOUT -> stringResource(Lang.settings_tab_about)
        SettingsTab.DEBUG -> stringResource(Lang.settings_tab_debug)
    }
}

// a lot of call-sites, don't make it internal
@Composable
fun SettingsTab(
    modifier: Modifier = Modifier,
    content: @Composable SettingsScope.() -> Unit,
) {
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(SettingsScope.itemVerticalSpacing),
    ) {
        val scope = remember(this) {
            object : SettingsScope(), ColumnScope by this@Column {}
        }
        scope.content()
    }
}