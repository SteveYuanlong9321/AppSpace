package com.example.appspace

import android.os.Bundle
import android.os.Build
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import android.os.Environment
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import java.io.File
import java.io.BufferedInputStream
import java.util.zip.ZipInputStream
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.PauseCircleOutline
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material3.Switch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Build as BuildCategoryIcon
import androidx.compose.material.icons.outlined.SdStorage
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Factory
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Cast
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.FlashlightOn
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Podcasts
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import rikka.shizuku.Shizuku
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Parcel
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

// App-wide theme switch. Blue is the default; Android 12+ can opt into wallpaper-derived Monet colors.
private val monetColorsEnabled = mutableStateOf(false)

private val AppSpaceBlueDarkColorScheme = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    onPrimary = Color(0xFF08264C),
    primaryContainer = Color(0xFF204A7A),
    onPrimaryContainer = Color(0xFFD7E7FF),
    secondary = Color(0xFF9BCBFF),
    onSecondary = Color(0xFF082A49),
    secondaryContainer = Color(0xFF243D5A),
    onSecondaryContainer = Color(0xFFD7E8FF),
    tertiary = Color(0xFF80D4F5),
    onTertiary = Color(0xFF003545),
    tertiaryContainer = Color(0xFF164B60),
    onTertiaryContainer = Color(0xFFC4EEFF),
    background = Color(0xFF101116),
    onBackground = Color(0xFFF5F7FB),
    surface = Color(0xFF171B24),
    onSurface = Color(0xFFE7ECF5),
    surfaceVariant = Color(0xFF252D3A),
    onSurfaceVariant = Color(0xFFBAC7DA),
    outline = Color(0xFF687A93),
    outlineVariant = Color(0xFF3E4B60)
)

// 系统安装器返回后，应用管理页在下次显示时刷新一次。
private var pendingInstalledAppsRefresh by mutableStateOf(false)
private const val APK_MIME_TYPE = "application/vnd.android.package-archive"
private const val REQUEST_NATIVE_INSTALLER = 7402
private var pendingExternalApkUri by mutableStateOf<Uri?>(null)

private const val HUAWEI_DOWNLOAD_USER_AGENT = "HiSpace##16.5.1.301##google##Pixel 8 Pro"

class MainActivity : ComponentActivity() {
    private var pendingNativeInstallerResultUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        captureIncomingApkIntent(intent)

        window.statusBarColor = android.graphics.Color.rgb(16, 17, 22)
        window.navigationBarColor = android.graphics.Color.rgb(16, 17, 22)

        monetColorsEnabled.value = getSharedPreferences("appspace_settings", MODE_PRIVATE)
            .getBoolean("use_monet_colors", false)

        setContent {
            val useMonet = monetColorsEnabled.value
            val appColorScheme = remember(useMonet) {
                if (useMonet && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    dynamicDarkColorScheme(this@MainActivity)
                } else {
                    AppSpaceBlueDarkColorScheme
                }
            }
            MaterialTheme(colorScheme = appColorScheme) {
                SideEffect {
                    // Keep the separate AndroidView-based navigation composition in sync with the active theme.
                    AppSpaceNavigationAccentColor.value = appColorScheme.primary
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppSpaceGlassRoot()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        captureIncomingApkIntent(intent)
    }

    private fun captureIncomingApkIntent(incoming: Intent?) {
        if (incoming == null) return
        val action = incoming.action
        if (action != Intent.ACTION_VIEW && action != Intent.ACTION_INSTALL_PACKAGE) return
        val uri = incoming.data ?: return
        val mime = incoming.type ?: runCatching { contentResolver.getType(uri) }.getOrNull().orEmpty()
        val looksLikeApk = mime == APK_MIME_TYPE || uri.lastPathSegment.orEmpty().endsWith(".apk", ignoreCase = true)
        if (looksLikeApk) pendingExternalApkUri = uri
    }

    internal fun launchNativeInstallerForResult(intent: Intent, apkUri: Uri) {
        pendingNativeInstallerResultUri = apkUri
        startActivityForResult(intent, REQUEST_NATIVE_INSTALLER)
    }

    @Deprecated("Deprecated by Android, retained for install-result compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_NATIVE_INSTALLER) return
        val uri = pendingNativeInstallerResultUri
        pendingNativeInstallerResultUri = null
        // EXTRA_INSTALL_RESULT is hidden from the public Android SDK.
        // ACTION_INSTALL_PACKAGE with EXTRA_RETURN_RESULT reports RESULT_OK on success.
        val installed = resultCode == Activity.RESULT_OK
        if (installed && uri != null) {
            val removed = deleteDownloadedApkAfterSuccess(this, uri)
            if (removed) Toast.makeText(this, "安装成功，已自动清理 APK", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
private fun AppSpaceGlassRoot() {
    val selectedTabState = remember { mutableStateOf("发现") }
    val selectedTab by selectedTabState
    val isSearchPageState = remember { mutableStateOf(false) }
    // Hide the bottom navigation on every child/detail screen; only root tabs show it.
    val hideBottomNavigationState = remember { mutableStateOf(false) }

    val tabs = listOf(
        Triple("发现", Icons.Outlined.Explore, "发现"),
        Triple("下载", Icons.Outlined.Download, "下载"),
        Triple("应用管理", Icons.Outlined.Smartphone, "应用管理")
    )

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            val root = FrameLayout(context).apply {
                setBackgroundColor(android.graphics.Color.rgb(16, 17, 22))
                clipChildren = false
                clipToPadding = false
            }

            // 第一层：独立的首页采样源。
            val contentContainer = FrameLayout(context).apply {
                clipChildren = false
                clipToPadding = false
                setBackgroundColor(android.graphics.Color.rgb(16, 17, 22))
            }

            val pageCompose = ComposeView(context).apply {
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
                )
                setContent {
                    val useMonet = monetColorsEnabled.value
                    val pageColorScheme = remember(useMonet) {
                        if (useMonet && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            dynamicDarkColorScheme(context)
                        } else {
                            AppSpaceBlueDarkColorScheme
                        }
                    }
                    MaterialTheme(colorScheme = pageColorScheme) {
                        AppSpacePageContent(
                            selectedTab = selectedTab,
                            isSearchPageState = isSearchPageState,
                            hideBottomNavigationState = hideBottomNavigationState,
                            onOpenSettings = { selectedTabState.value = "设置" },
                            onCloseSettings = { selectedTabState.value = "发现" },
                            onOpenSearch = { initialQuery ->
                                if (initialQuery.isNotBlank()) {
                                    // AppSpacePageContent 会在打开搜索页前保存预填关键词。
                                }
                                isSearchPageState.value = true
                            },
                            onCloseSearch = { isSearchPageState.value = false }
                        )
                    }
                }
            }

            contentContainer.addView(
                pageCompose,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )

            root.addView(
                contentContainer,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )

            val density = context.resources.displayMetrics.density
            val navHeight = (76f * density).toInt()
            val navSideMargin = (14f * density).toInt()
            val navBottomMargin = (8f * density).toInt()

            val navigationBar = AppSpaceLiquidGlassNavigationBar(
                context = context,
                contentContainer = contentContainer,
                tabs = tabs,
                selectedTabState = selectedTabState,
                onTabSelected = { selectedTabState.value = it }
            ).apply {
                tag = "appspace_liquid_glass_navigation"
            }

            root.addView(
                navigationBar,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    navHeight
                ).apply {
                    gravity = Gravity.BOTTOM
                    leftMargin = navSideMargin
                    rightMargin = navSideMargin
                    bottomMargin = navBottomMargin
                }
            )
            root
        },
        update = { root ->
            // 独立导航栏只更新胶囊位置，不重建玻璃视图。
            root.findViewWithTag<View>("appspace_liquid_glass_navigation")
                ?.let { navView ->
                    navView.visibility = if (
                        isSearchPageState.value ||
                        hideBottomNavigationState.value ||
                        selectedTabState.value == "设置"
                    ) View.GONE else View.VISIBLE
                    (navView as? AppSpaceLiquidGlassNavigationBar)?.updateSelection()
                }
        }
    )
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val incomingUri = pendingExternalApkUri
    if (incomingUri != null) {
        var installingExternalApk by remember(incomingUri) { mutableStateOf(false) }
        var externalInstallStatus by remember(incomingUri) { mutableStateOf<String?>(null) }
        val displayName = remember(incomingUri) { resolveApkDisplayName(context, incomingUri) }
        AlertDialog(
            onDismissRequest = { if (!installingExternalApk) pendingExternalApkUri = null },
            title = { Text("使用 AppSpace 安装 APK") },
            text = {
                Column {
                    Text(displayName, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(8.dp))
                    Text("AppSpace 会优先尝试使用已授权的 Shizuku 安装；否则打开系统安装器。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    externalInstallStatus?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(enabled = !installingExternalApk, onClick = {
                    scope.launch {
                        installingExternalApk = true
                        val started = installApkUsingPreferences(context, incomingUri, automatic = false) { externalInstallStatus = it }
                        installingExternalApk = false
                        if (started) pendingExternalApkUri = null
                        else Toast.makeText(context, externalInstallStatus ?: "无法启动安装", Toast.LENGTH_LONG).show()
                    }
                }) { Text(if (installingExternalApk) "正在处理…" else "安装") }
            },
            dismissButton = {
                TextButton(enabled = !installingExternalApk, onClick = { pendingExternalApkUri = null }) { Text("取消") }
            }
        )
    }

}

@Composable
private fun AppSpacePageContent(
    selectedTab: String,
    isSearchPageState: MutableState<Boolean>,
    hideBottomNavigationState: MutableState<Boolean>,
    onOpenSettings: () -> Unit,
    onCloseSettings: () -> Unit,
    onOpenSearch: (String) -> Unit,
    onCloseSearch: () -> Unit
) {
    val isSearchPage by isSearchPageState
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val selectedCategoryState = remember { mutableStateOf<AppSpaceCategory?>(null) }
    val isAppDetailPageState = remember { mutableStateOf(false) }
    val openSearchWithQuery: (String) -> Unit = { initialQuery ->
        searchQuery = initialQuery
        onOpenSearch(initialQuery)
    }
    val tabOrder = listOf("发现", "下载", "应用管理", "设置")
    val saveableStateHolder = rememberSaveableStateHolder()

    LaunchedEffect(selectedTab) {
        if (selectedTab != "发现") selectedCategoryState.value = null
        if (selectedTab != "发现") isAppDetailPageState.value = false
    }
    SideEffect {
        hideBottomNavigationState.value =
            isSearchPage ||
                    selectedCategoryState.value != null ||
                    selectedTab == "设置" ||
                    isAppDetailPageState.value
    }
    BackHandler(enabled = isSearchPage) { onCloseSearch() }
    BackHandler(enabled = selectedCategoryState.value != null && !isSearchPage) {
        selectedCategoryState.value = null
    }
    BackHandler(enabled = selectedTab == "设置" && !isSearchPage) { onCloseSettings() }

    AnimatedContent(
        targetState = selectedTab,
        label = "AppSpace 页面切换",
        transitionSpec = {
            val fromIndex = tabOrder.indexOf(initialState).coerceAtLeast(0)
            val toIndex = tabOrder.indexOf(targetState).coerceAtLeast(0)
            (if (toIndex >= fromIndex) {
                (slideInHorizontally(
                    animationSpec = tween(durationMillis = 260),
                    initialOffsetX = { fullWidth -> fullWidth / 7 }
                ) + fadeIn(animationSpec = tween(durationMillis = 220))) togetherWith
                        (slideOutHorizontally(
                            animationSpec = tween(durationMillis = 190),
                            targetOffsetX = { fullWidth -> -fullWidth / 12 }
                        ) + fadeOut(animationSpec = tween(durationMillis = 160)))
            } else {
                (slideInHorizontally(
                    animationSpec = tween(durationMillis = 260),
                    initialOffsetX = { fullWidth -> -fullWidth / 7 }
                ) + fadeIn(animationSpec = tween(durationMillis = 220))) togetherWith
                        (slideOutHorizontally(
                            animationSpec = tween(durationMillis = 190),
                            targetOffsetX = { fullWidth -> fullWidth / 12 }
                        ) + fadeOut(animationSpec = tween(durationMillis = 160)))
            })
        }
    ) { targetTab ->
        saveableStateHolder.SaveableStateProvider(key = targetTab) {
            AnimatedContent(
                targetState = isSearchPage,
                label = "AppSpace 搜索页面切换",
                transitionSpec = {
                    if (targetState) {
                        // 打开搜索：搜索页从右侧进入，发现页向左退出。
                        (fadeIn(tween(220)) + slideInHorizontally(
                            animationSpec = tween(260),
                            initialOffsetX = { fullWidth -> fullWidth }
                        )) togetherWith
                                (fadeOut(tween(180)) + slideOutHorizontally(
                                    animationSpec = tween(220),
                                    targetOffsetX = { fullWidth -> -fullWidth / 4 }
                                ))
                    } else {
                        // 返回发现页：搜索页向右退出，发现页从左侧进入。
                        (fadeIn(tween(220)) + slideInHorizontally(
                            animationSpec = tween(260),
                            initialOffsetX = { fullWidth -> -fullWidth / 4 }
                        )) togetherWith
                                (fadeOut(tween(180)) + slideOutHorizontally(
                                    animationSpec = tween(220),
                                    targetOffsetX = { fullWidth -> fullWidth }
                                ))
                    }
                }
            ) { showingSearch ->
                if (showingSearch) {
                    AppSearchPage(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onBack = onCloseSearch,
                        onDetailPageVisibilityChange = { isAppDetailPageState.value = it }
                    )
                } else {
                    when (targetTab) {
                        "下载" -> DownloadsPage()
                        "应用管理" -> AppManagementPage()
                        "设置" -> SettingsPage(onBack = onCloseSettings)
                        else -> {
                            // 分类页复用搜索页的进出场动画：进入时从右侧滑入，
                            // 返回时分类页向右退出、发现页从左侧进入。
                            AnimatedContent(
                                targetState = selectedCategoryState.value,
                                label = "AppSpace 分类页面切换",
                                transitionSpec = {
                                    if (targetState != null) {
                                        // 进入分类：分类页从右侧进入，发现页向左退出。
                                        (fadeIn(tween(220)) + slideInHorizontally(
                                            animationSpec = tween(260),
                                            initialOffsetX = { fullWidth -> fullWidth }
                                        )) togetherWith
                                                (fadeOut(tween(180)) + slideOutHorizontally(
                                                    animationSpec = tween(220),
                                                    targetOffsetX = { fullWidth -> -fullWidth / 4 }
                                                ))
                                    } else {
                                        // 返回发现：发现页从左侧进入，分类页向右退出。
                                        (fadeIn(tween(220)) + slideInHorizontally(
                                            animationSpec = tween(260),
                                            initialOffsetX = { fullWidth -> -fullWidth / 4 }
                                        )) togetherWith
                                                (fadeOut(tween(180)) + slideOutHorizontally(
                                                    animationSpec = tween(220),
                                                    targetOffsetX = { fullWidth -> fullWidth }
                                                ))
                                    }
                                }
                            ) { category ->
                                if (category == null) {
                                    DiscoveryPageContent(
                                        onOpenSearch = openSearchWithQuery,
                                        onOpenSettings = onOpenSettings,
                                        onOpenCategory = { selectedCategoryState.value = it }
                                    )
                                } else {
                                    AppSpaceCategoryPage(
                                        category = category,
                                        onBack = { selectedCategoryState.value = null },
                                        onOpenSearch = { openSearchWithQuery("") },
                                        onDetailPageVisibilityChange = { isAppDetailPageState.value = it }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class AppSpaceCategory(
    val title: String,
    val subtitle: String,
    val query: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private data class AppSpaceCategoryGroup(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val subcategories: List<AppSpaceCategory>
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DiscoveryPageContent(
    onOpenSearch: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCategory: (AppSpaceCategory) -> Unit
) {
    // 按 F-Droid 的分类组织方式展示可折叠分类与子分类。
    // 当前仓库接口不返回正式分类 ID，因此点击子分类时仍用其相关词请求应用。
    val categories = listOf(
        AppSpaceCategoryGroup(
            "健康", "健身、营养和医疗应用", Icons.Outlined.HealthAndSafety,
            listOf(
                AppSpaceCategory("健身", "健身与运动应用", "fitness exercise workout", Icons.Outlined.FitnessCenter),
                AppSpaceCategory("健康管理工具", "健康记录与健康跟踪", "health tracker medical", Icons.Outlined.HealthAndSafety),
                AppSpaceCategory("睡眠追踪", "睡眠与作息分析", "sleep tracker", Icons.Outlined.Bedtime),
                AppSpaceCategory("冥想", "冥想与放松练习", "meditation mindfulness", Icons.Outlined.SelfImprovement),
                AppSpaceCategory("习惯追踪程序", "习惯与日常目标", "habit tracker", Icons.Outlined.CheckCircle),
                AppSpaceCategory("营养与饮食", "营养、饮食与食谱", "nutrition diet food", Icons.Outlined.Restaurant),
                AppSpaceCategory("药物", "药物提醒与用药记录", "medication medicine reminder", Icons.Outlined.Medication),
                AppSpaceCategory("心理健康", "情绪与心理健康支持", "mental health mood", Icons.Outlined.Psychology)
            )
        ),
        AppSpaceCategoryGroup(
            "兴趣", "科学、体育、绘图和菜谱", Icons.Outlined.FavoriteBorder,
            listOf(
                AppSpaceCategory("烹饪管理工具", "食谱与烹饪管理", "cooking recipes", Icons.Outlined.MenuBook),
                AppSpaceCategory("绘图", "绘画、素描与插画", "drawing sketch painting", Icons.Outlined.Palette),
                AppSpaceCategory("开发", "编程与开发辅助", "development programming", Icons.Outlined.Code),
                AppSpaceCategory("科学与教育", "科学、学习与教育", "science education learning", Icons.Outlined.School),
                AppSpaceCategory("园艺", "园艺与植物管理", "gardening plants", Icons.Outlined.LocalFlorist),
                AppSpaceCategory("新闻", "新闻与资讯阅读", "news reader", Icons.Outlined.Article),
                AppSpaceCategory("音乐练习工具", "乐器、节奏与练习", "music practice instrument", Icons.Outlined.MusicNote),
                AppSpaceCategory("宗教", "宗教与灵修工具", "religion spiritual", Icons.Outlined.Star)
            )
        ),
        AppSpaceCategoryGroup(
            "娱乐和媒体", "音乐、播客和电子书阅读器", Icons.Outlined.Movie,
            listOf(
                AppSpaceCategory("本地媒体播放器", "播放本地音乐与视频", "local media player", Icons.Outlined.PlayCircleOutline),
                AppSpaceCategory("播客", "播客订阅与播放", "podcast player", Icons.Outlined.Podcasts),
                AppSpaceCategory("电子书阅读器", "电子书与文档阅读", "ebook reader", Icons.Outlined.MenuBook),
                AppSpaceCategory("多媒体", "音频、视频与媒体工具", "multimedia media", Icons.Outlined.Movie),
                AppSpaceCategory("环境音", "白噪音与环境音效", "ambient sound white noise", Icons.Outlined.MusicNote),
                AppSpaceCategory("收音机", "网络电台与广播", "radio player", Icons.Outlined.Radio),
                AppSpaceCategory("投屏", "投屏与媒体串流", "cast screen streaming", Icons.Outlined.Cast),
                AppSpaceCategory("音量", "音量和音频控制", "volume audio control", Icons.Outlined.VolumeUp),
                AppSpaceCategory("有声书", "有声书与语音阅读", "audiobook audio book", Icons.Outlined.MenuBook),
                AppSpaceCategory("阅读", "阅读与书库管理", "reading books", Icons.Outlined.MenuBook),
                AppSpaceCategory("在线媒体播放器", "在线音乐与视频播放", "online media player video", Icons.Outlined.PlayCircleOutline)
            )
        ),
        AppSpaceCategoryGroup(
            "工具", "导航、天气、计算器和转换器", Icons.Outlined.BuildCategoryIcon,
            listOf(
                AppSpaceCategory("学业与学生工具", "学习计划与学生工具", "student tools study", Icons.Outlined.School),
                AppSpaceCategory("导航", "地图、路线与导航", "navigation maps", Icons.Outlined.Map),
                AppSpaceCategory("翻译和词典", "翻译、词典与语言工具", "translation dictionary", Icons.Outlined.Language),
                AppSpaceCategory("公共交通", "公交、地铁与出行", "public transport transit", Icons.Outlined.DirectionsBus),
                AppSpaceCategory("购物清单", "购物与清单管理", "shopping list", Icons.Outlined.ShoppingCart),
                AppSpaceCategory("科学计算器", "科学计算与数学工具", "scientific calculator math", Icons.Outlined.Calculate),
                AppSpaceCategory("计时器", "倒计时与计时工具", "timer stopwatch", Icons.Outlined.Schedule),
                AppSpaceCategory("计算器", "计算器与单位计算", "calculator converter", Icons.Outlined.Calculate),
                AppSpaceCategory("手电筒", "手电筒与照明工具", "flashlight torch", Icons.Outlined.FlashlightOn),
                AppSpaceCategory("天气", "天气预报与气象信息", "weather forecast", Icons.Outlined.WbSunny),
                AppSpaceCategory("位置管理与分享", "位置、定位与分享", "location sharing gps", Icons.Outlined.LocationOn),
                AppSpaceCategory("文本加密", "文本加密与解密", "text encryption", Icons.Outlined.Security),
                AppSpaceCategory("物品追踪", "物品、库存与清单追踪", "inventory tracker item tracker", Icons.Outlined.Inventory2),
                AppSpaceCategory("密码管理", "密码与凭据管理", "password manager", Icons.Outlined.Security),
                AppSpaceCategory("语音辅助", "语音助手与语音工具", "voice assistant speech", Icons.Outlined.Mic),
                AppSpaceCategory("AI 聊天", "AI 聊天与助手工具", "AI chat assistant", Icons.Outlined.Chat)
            )
        ),
        AppSpaceCategoryGroup(
            "文件和存储", "文件、图库、云同步和加密", Icons.Outlined.SdStorage,
            listOf(
                AppSpaceCategory("笔记", "笔记与文字记录", "notes notepad", Icons.Outlined.EditNote),
                AppSpaceCategory("书签", "书签与收藏管理", "bookmarks", Icons.Outlined.BookmarkBorder),
                AppSpaceCategory("文件传输", "文件分享与传输", "file transfer sharing", Icons.Outlined.FileUpload),
                AppSpaceCategory("文件管理器", "文件、目录与存储管理", "file manager", Icons.Outlined.Folder),
                AppSpaceCategory("存储分析", "存储空间与文件分析", "storage analyzer disk usage", Icons.Outlined.Storage),
                AppSpaceCategory("云存储和文件同步", "云端备份与同步", "cloud storage file sync", Icons.Outlined.Sync),
                AppSpaceCategory("图库", "图片与相册管理", "gallery photos image", Icons.Outlined.Image),
                AppSpaceCategory("压缩", "压缩包与归档工具", "archive zip compression", Icons.Outlined.Archive),
                AppSpaceCategory("加密", "文件加密与安全存储", "file encryption secure storage", Icons.Outlined.Security)
            )
        ),
        AppSpaceCategoryGroup(
            "杂项", "未分类", Icons.Outlined.Category,
            listOf(
                AppSpaceCategory("Travel & Local", "旅行与本地生活", "travel local", Icons.Outlined.Map)
            )
        ),
        AppSpaceCategoryGroup(
            "游戏", "棋盘、卡牌和派对游戏", Icons.Outlined.SportsEsports,
            listOf(
                AppSpaceCategory("策略游戏", "策略与战术游戏", "strategy game", Icons.Outlined.SportsEsports),
                AppSpaceCategory("SOS 单词游戏", "单词与文字游戏", "word puzzle game", Icons.Outlined.Extension),
                AppSpaceCategory("动作游戏", "动作与冒险游戏", "action adventure game", Icons.Outlined.SportsEsports),
                AppSpaceCategory("角色扮演游戏", "角色扮演与剧情游戏", "role playing game RPG", Icons.Outlined.Person),
                AppSpaceCategory("休闲游戏", "轻量休闲游戏", "casual game", Icons.Outlined.SportsEsports),
                AppSpaceCategory("模拟游戏", "模拟与经营游戏", "simulation game", Icons.Outlined.Dashboard),
                AppSpaceCategory("纸牌游戏", "纸牌与卡牌游戏", "card game solitaire", Icons.Outlined.Style),
                AppSpaceCategory("平台游戏", "平台跳跃游戏", "platform game platformer", Icons.Outlined.SportsEsports),
                AppSpaceCategory("射击游戏", "射击与弹幕游戏", "shooter game", Icons.Outlined.SportsEsports),
                AppSpaceCategory("棋盘游戏", "棋盘与桌面游戏", "board game chess", Icons.Outlined.GridOn),
                AppSpaceCategory("体育游戏", "体育与竞速游戏", "sports game racing", Icons.Outlined.FitnessCenter),
                AppSpaceCategory("骰子", "骰子与随机游戏", "dice game", Icons.Outlined.Casino),
                AppSpaceCategory("益智游戏", "益智与逻辑游戏", "puzzle game logic", Icons.Outlined.Extension),
                AppSpaceCategory("解谜游戏", "解谜与密室游戏", "adventure puzzle game", Icons.Outlined.Extension),
                AppSpaceCategory("派对游戏", "多人和派对游戏", "party game multiplayer", Icons.Outlined.SportsEsports),
                AppSpaceCategory("桌游", "桌面与聚会游戏", "tabletop board game", Icons.Outlined.Dashboard),
                AppSpaceCategory("游戏工具", "游戏辅助与记录工具", "game tools gaming utility", Icons.Outlined.Settings)
            )
        ),
        AppSpaceCategoryGroup(
            "生产力", "日历、任务、时钟和笔记", Icons.Outlined.Factory,
            listOf(
                AppSpaceCategory("计划", "计划与日程安排", "planner planning", Icons.Outlined.CalendarMonth),
                AppSpaceCategory("计时器", "计时与倒计时", "timer stopwatch", Icons.Outlined.Schedule),
                AppSpaceCategory("记录", "日志与记录工具", "log journal tracker", Icons.Outlined.EditNote),
                AppSpaceCategory("任务", "任务与待办清单", "task todo list", Icons.Outlined.CheckCircle),
                AppSpaceCategory("日历和日程", "日历、事件与日程", "calendar schedule events", Icons.Outlined.CalendarMonth),
                AppSpaceCategory("时间追踪工具", "时间统计与追踪", "time tracker", Icons.Outlined.Schedule),
                AppSpaceCategory("时钟", "时钟、闹钟与世界时间", "clock alarm", Icons.Outlined.Schedule),
                AppSpaceCategory("文本编辑器", "文本编辑与代码编辑", "text editor code editor", Icons.Outlined.EditNote),
                AppSpaceCategory("Code & Forge", "代码仓库与开发协作", "code forge git", Icons.Outlined.Code),
                AppSpaceCategory("记事本", "快速笔记与便笺", "notepad quick notes", Icons.Outlined.EditNote),
                AppSpaceCategory("待办清单", "待办与清单管理", "todo checklist", Icons.Outlined.CheckCircle)
            )
        ),
        AppSpaceCategoryGroup(
            "网络", "浏览器、VPN、代理和防火墙", Icons.Outlined.Wifi,
            listOf(
                AppSpaceCategory("防火墙", "防火墙与流量过滤", "firewall", Icons.Outlined.Security),
                AppSpaceCategory("连接", "连接与网络工具", "connectivity connection", Icons.Outlined.Wifi),
                AppSpaceCategory("浏览器", "网页浏览器", "browser web browser", Icons.Outlined.Language),
                AppSpaceCategory("网络", "网络状态与管理", "network", Icons.Outlined.Wifi),
                AppSpaceCategory("网络分析工具", "网络诊断与分析", "network analyzer diagnostic", Icons.Outlined.Analytics),
                AppSpaceCategory("下载", "下载管理与文件获取", "download manager", Icons.Outlined.Download),
                AppSpaceCategory("远程控制", "远程桌面与设备控制", "remote control remote desktop", Icons.Outlined.Smartphone),
                AppSpaceCategory("DNS & Hosts", "DNS 与 Hosts 管理", "DNS hosts", Icons.Outlined.Dns),
                AppSpaceCategory("VPN 客户端", "VPN 与隧道连接", "VPN client", Icons.Outlined.Security),
                AppSpaceCategory("代理", "代理与流量转发", "proxy client", Icons.Outlined.Wifi),
                AppSpaceCategory("隐私网络", "隐私与匿名网络", "privacy network anonymous", Icons.Outlined.Security)
            )
        ),
        AppSpaceCategoryGroup(
            "设备", "启动器、键盘、安全和主题", Icons.Outlined.Smartphone,
            listOf(
                AppSpaceCategory("安全", "设备安全与防护", "device security", Icons.Outlined.Security),
                AppSpaceCategory("壁纸", "壁纸与背景定制", "wallpaper", Icons.Outlined.Palette),
                AppSpaceCategory("电池", "电池状态与省电工具", "battery saver", Icons.Outlined.BatteryChargingFull),
                AppSpaceCategory("硬件信息分析", "设备硬件与系统信息", "hardware info device info", Icons.Outlined.Smartphone),
                AppSpaceCategory("启动器", "桌面与启动器", "launcher home screen", Icons.Outlined.Apps),
                AppSpaceCategory("键盘", "键盘与输入法", "keyboard input method", Icons.Outlined.Keyboard),
                AppSpaceCategory("音量控制", "音量与声音配置", "volume control", Icons.Outlined.VolumeUp),
                AppSpaceCategory("通知管理", "通知与提醒管理", "notification manager", Icons.Outlined.NotificationsNone),
                AppSpaceCategory("自动化", "自动化与快捷操作", "automation shortcuts", Icons.Outlined.Settings),
                AppSpaceCategory("快捷设置", "快捷设置与控制面板", "quick settings", Icons.Outlined.Settings),
                AppSpaceCategory("主题", "主题、样式与外观", "themes theming", Icons.Outlined.Palette),
                AppSpaceCategory("字体", "字体与文字显示", "font typography", Icons.Outlined.TextFields),
                AppSpaceCategory("小组件", "桌面小组件", "widgets", Icons.Outlined.Apps),
                AppSpaceCategory("传感器", "传感器与设备状态", "sensors", Icons.Outlined.Smartphone),
                AppSpaceCategory("相机", "相机与拍摄工具", "camera", Icons.Outlined.CameraAlt),
                AppSpaceCategory("应用管理", "应用与软件包工具", "app manager package manager", Icons.Outlined.Apps)
            )
        ),
        AppSpaceCategoryGroup(
            "财务和钱包", "财务、金钱和密码管理器", Icons.Outlined.AccountBalanceWallet,
            listOf(
                AppSpaceCategory("财务管理工具", "预算与个人财务", "finance management personal finance", Icons.Outlined.AccountBalanceWallet),
                AppSpaceCategory("记账", "开支、收入与账单", "expense tracker budget", Icons.Outlined.ReceiptLong),
                AppSpaceCategory("钱包", "钱包与支付工具", "wallet payment", Icons.Outlined.AccountBalanceWallet),
                AppSpaceCategory("市场分析", "行情、价格与市场跟踪", "market tracker stocks finance", Icons.Outlined.ShowChart),
                AppSpaceCategory("密码管理器", "密码与凭据安全管理", "password manager", Icons.Outlined.Security)
            )
        ),
        AppSpaceCategoryGroup(
            "通信", "电子邮件、消息收发和社交网络", Icons.Outlined.Forum,
            listOf(
                AppSpaceCategory("电子邮件", "邮件收发与邮件客户端", "email mail client", Icons.Outlined.Email),
                AppSpaceCategory("联系人", "联系人与通讯录", "contacts address book", Icons.Outlined.Contacts),
                AppSpaceCategory("电话", "拨号与通话管理", "phone dialer call", Icons.Outlined.Call),
                AppSpaceCategory("社交网络", "社交、社区与动态", "social network community", Icons.Outlined.People),
                AppSpaceCategory("消息收发", "即时消息与聊天", "messaging chat", Icons.Outlined.Chat),
                AppSpaceCategory("短信应用", "短信与彩信", "sms messaging", Icons.Outlined.Sms),
                AppSpaceCategory("即时通讯", "即时通讯客户端", "instant messenger", Icons.Outlined.Chat),
                AppSpaceCategory("音频和视频聊天", "语音、视频与网络电话", "audio video chat voip", Icons.Outlined.Call),
                AppSpaceCategory("社区", "社区论坛与讨论", "forum community", Icons.Outlined.Forum)
            )
        )
    )

    val expandedCategories = remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 110.dp)
    ) {
        Spacer(Modifier.height(18.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("AppSpace", color = MaterialTheme.colorScheme.onBackground, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("寻找合您心意的应用", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "设置",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(28.dp).clickable { onOpenSettings() }
            )
        }

        Spacer(Modifier.height(22.dp))
        Row(
            modifier = Modifier.fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onOpenSearch("") }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Search, contentDescription = "搜索", tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text("搜索应用", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp, modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(28.dp))
        SectionTitle("类别")
        Spacer(Modifier.height(14.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f), RoundedCornerShape(18.dp))
        ) {
            categories.forEachIndexed { index, category ->
                val expanded = category.title in expandedCategories.value
                val arrowRotation by animateFloatAsState(
                    targetValue = if (expanded) 180f else 0f,
                    animationSpec = tween(durationMillis = 220),
                    label = "categoryArrowRotation"
                )
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable {
                                expandedCategories.value = if (expanded) {
                                    expandedCategories.value - category.title
                                } else {
                                    expandedCategories.value + category.title
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(category.title, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(3.dp))
                            Text(category.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, maxLines = 2)
                        }
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowDown,
                            contentDescription = if (expanded) "折叠分类" else "展开分类",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp).graphicsLayer {
                                rotationZ = arrowRotation
                            }
                        )
                    }
                    AnimatedVisibility(
                        visible = expanded,
                        enter = expandVertically(
                            expandFrom = Alignment.Top,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = 420f
                            )
                        ) + fadeIn(animationSpec = tween(durationMillis = 170)),
                        exit = shrinkVertically(
                            shrinkTowards = Alignment.Top,
                            animationSpec = tween(durationMillis = 190)
                        ) + fadeOut(animationSpec = tween(durationMillis = 110))
                    ) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 12.dp, top = 0.dp, bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            category.subcategories.forEach { subcategory ->
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                        .clickable { onOpenCategory(subcategory) }
                                        .padding(horizontal = 9.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = subcategory.icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        text = subcategory.title,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
                if (index < categories.lastIndex) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun AppSpaceCategoryPage(
    category: AppSpaceCategory,
    onBack: () -> Unit,
    onOpenSearch: () -> Unit,
    onDetailPageVisibilityChange: (Boolean) -> Unit
) {
    var apps by remember(category.title) { mutableStateOf<List<FdroidApp>>(emptyList()) }
    var isLoading by remember(category.title) { mutableStateOf(true) }
    var errorMessage by remember(category.title) { mutableStateOf<String?>(null) }
    var selectedApp by remember(category.title) { mutableStateOf<FdroidApp?>(null) }

    BackHandler(enabled = selectedApp != null) {
        selectedApp = null
        onDetailPageVisibilityChange(false)
    }
    LaunchedEffect(category.query) {
        isLoading = true
        errorMessage = null
        apps = emptyList()

        // 分类关键词通常是一组相关词，直接把整串词作为一个查询时，
        // F-Droid 搜索可能要求多个词同时匹配，导致 0 条结果。
        // 改为并行搜索前三个有效关键词，再按包名去重合并。
        val searchTerms = category.query
            .split(Regex("[\\s&,/]+"))
            .map { it.trim() }
            .filter { it.length >= 2 && it.lowercase() !in setOf("and", "or", "the", "for", "with") }
            .distinctBy { it.lowercase() }
            .take(3)
            .ifEmpty { listOf(category.title) }

        var requestFailure: String? = null
        val responses: List<FdroidSearchResponse> = try {
            coroutineScope {
                searchTerms.map { term ->
                    async { FdroidSearchRepository.search(term) }
                }.awaitAll()
            }
        } catch (cancelled: kotlinx.coroutines.CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            requestFailure = error.message ?: "F-Droid 搜索服务暂时不可用。"
            emptyList()
        }

        // Store searches use one category keyword each; run them concurrently so a slow source does not block others.
        val categoryTerm = searchTerms.firstOrNull() ?: category.title
        val extraResponses = coroutineScope {
            val githubRequest = async {
                try { GithubReleaseRepository.search(categoryTerm) }
                catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
                catch (error: Exception) { GithubSearchResponse(emptyList(), error.message ?: "GitHub 搜索暂时不可用。") }
            }
            val huaweiRequest = async {
                try { HuaweiAppGalleryRepository.search(categoryTerm) }
                catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
                catch (error: Exception) { HuaweiSearchResponse(emptyList(), error.message ?: "华为应用市场搜索暂时不可用。") }
            }
            val vivoRequest = async {
                try { VivoAppStoreRepository.search(categoryTerm) }
                catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
                catch (error: Exception) { StoreCatalogSearchResponse(emptyList(), error.message ?: "vivo 应用商店搜索暂时不可用。") }
            }
            val uptodownRequest = async {
                try { UptodownRepository.search(categoryTerm) }
                catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
                catch (error: Exception) { StoreCatalogSearchResponse(emptyList(), error.message ?: "Uptodown 搜索暂时不可用。") }
            }
            Triple(
                githubRequest.await(),
                huaweiRequest.await(),
                vivoRequest.await() to uptodownRequest.await()
            )
        }
        val githubResponse = extraResponses.first
        val huaweiResponse = extraResponses.second
        val vivoResponse = extraResponses.third.first
        val uptodownResponse = extraResponses.third.second

        apps = AppSearchRanking.rankApps(
            categoryTerm,
            mergeCatalogApps(responses.flatMap { it.apps } + githubResponse.apps + huaweiResponse.apps + vivoResponse.apps + uptodownResponse.apps)
        ).take(50)

        errorMessage = if (apps.isNotEmpty()) null else {
            requestFailure
                ?: responses.firstNotNullOfOrNull { response ->
                    response.error?.takeUnless {
                        it.startsWith("接口请求成功（HTTP") && it.contains("解析出 0 条应用")
                    }
                }
                ?: githubResponse.error
                ?: huaweiResponse.error
                ?: vivoResponse.error
                ?: uptodownResponse.error
        }
        isLoading = false
    }

    if (selectedApp != null) {
        FdroidAppDetailPage(
            app = selectedApp!!,
            onBack = {
                selectedApp = null
                onDetailPageVisibilityChange(false)
            }
        )
    } else {
        Column(
            modifier = Modifier.fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(64.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "返回分类",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(28.dp).clickable { onBack() }
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    text = category.title,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "搜索应用",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(25.dp).clickable { onOpenSearch() }
                )
            }
            Box(
                modifier = Modifier.fillMaxWidth().height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f))
            )

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        androidx.compose.material3.CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                errorMessage != null && apps.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        EmptyState("暂时无法加载分类", errorMessage ?: "请检查网络后重试", Icons.Outlined.CloudDownload)
                    }
                }
                apps.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        EmptyState("这个分类暂时没有结果", "稍后可以重新打开分类试试。", Icons.Outlined.Apps)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentPadding = PaddingValues(bottom = 10.dp)
                    ) {
                        items(apps, key = { it.packageName }) { app ->
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable {
                                        selectedApp = app
                                        onDetailPageVisibilityChange(true)
                                    }
                                    .padding(horizontal = 4.dp, vertical = 20.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                CatalogAppIcon(app, 66)
                                Spacer(Modifier.width(17.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        app.name,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 23.sp
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    val realPackage = app.bestRealPackageName()
                                    val apkSize = formatCatalogSize(app.bestApkSizeBytes())
                                    Text(
                                        when {
                                            realPackage.isNotBlank() && apkSize != null -> "$realPackage · $apkSize"
                                            realPackage.isNotBlank() -> "$realPackage · 安装包大小暂未提供"
                                            apkSize != null -> "包名暂未提供 · $apkSize"
                                            else -> if (app.source in setOf(AppCatalogSource.VIVO_APPSTORE, AppCatalogSource.UPTODOWN, AppCatalogSource.HUAWEI_APPGALLERY)) "包名与安装包大小暂未返回" else "包名未由来源提供"
                                        },
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp,
                                        maxLines = 2
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        app.bestDeveloperName().ifBlank { app.summary.ifBlank { "来自 ${app.source.displayName}" } },
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp,
                                        maxLines = 1
                                    )
                                    Spacer(Modifier.height(5.dp))
                                    Text(
                                        app.availableSourceVariants().joinToString(" · ") { it.label },
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(start = 83.dp).height(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold
    )
}



@Composable
private fun AppSearchPage(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onDetailPageVisibilityChange: (Boolean) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("appspace_settings", Context.MODE_PRIVATE) }
    var rememberSearchHistory by remember { mutableStateOf(prefs.getBoolean("remember_search_history", true)) }
    val recentSearches = remember { mutableStateListOf<String>().apply {
        if (rememberSearchHistory) addAll(prefs.getString("search_history", "").orEmpty().split("\u001f").filter { it.isNotBlank() })
    } }
    var results by remember { mutableStateOf<List<FdroidApp>>(emptyList()) }
    var baseSearchLoading by remember { mutableStateOf(false) }
    var githubSearchLoading by remember { mutableStateOf(false) }
    var fdroidResponse by remember { mutableStateOf<FdroidSearchResponse?>(null) }
    var githubResponse by remember { mutableStateOf<GithubSearchResponse?>(null) }
    var huaweiResponse by remember { mutableStateOf<HuaweiSearchResponse?>(null) }
    var vivoResponse by remember { mutableStateOf<StoreCatalogSearchResponse?>(null) }
    var uptodownResponse by remember { mutableStateOf<StoreCatalogSearchResponse?>(null) }
    // Avoid repeatedly re-fetching detail metadata when the enrichment state itself changes.
    val vivoMetadataLookups = remember { mutableSetOf<String>() }
    val uptodownMetadataLookups = remember { mutableSetOf<String>() }
    val huaweiMetadataLookups = remember { mutableSetOf<String>() }
    // Covers the first Compose frame before LaunchedEffect starts, so a newly
    // entered query cannot briefly render the final "no results" empty state.
    // Keep this expression below the response state declarations; Kotlin local
    // declarations must exist before they're referenced.
    // Keep first-results state separate from background source fan-out. Once any catalog
    // responds, its results can render immediately while remaining catalogs continue.
    val isLoading = githubSearchLoading || (query.isNotBlank() && results.isEmpty() && baseSearchLoading)
    var searchSubmissionId by remember { mutableStateOf(0) }
    var submittedSearchQuery by remember { mutableStateOf("") }
    val latestQuery by rememberUpdatedState(query)
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedApp by remember { mutableStateOf<FdroidApp?>(null) }
    BackHandler(enabled = selectedApp != null) {
        selectedApp = null
        onDetailPageVisibilityChange(false)
    }

    // Live type-ahead uses the F-Droid and Huawei search paths. GitHub runs only
    // after explicit submission to avoid repeated network requests while typing.
    LaunchedEffect(query) {
        val term = query.trim()
        // Show a pending/searching state immediately, including during debounce.
        // Otherwise the empty-state branch briefly claims there are no results
        // before either F-Droid or Huawei has even been requested.
        baseSearchLoading = term.isNotBlank()
        githubSearchLoading = false
        results = emptyList()
        fdroidResponse = null
        githubResponse = null
        huaweiResponse = null
        vivoResponse = null
        uptodownResponse = null
        errorMessage = null

        if (term.isBlank()) {
            baseSearchLoading = false
            results = emptyList()
            errorMessage = null
            return@LaunchedEffect
        }

        kotlinx.coroutines.delay(250)
        if (query.trim() != term) return@LaunchedEffect
        try {
            coroutineScope {
                // Publish each lightweight catalog independently; cancel stale query jobs with this effect.
                val fdroidJob = launch {
                    val response = FdroidSearchRepository.search(term)
                    if (latestQuery.trim() == term) fdroidResponse = response
                }
                val huaweiJob = launch {
                    val response = HuaweiAppGalleryRepository.search(term)
                    if (latestQuery.trim() == term) huaweiResponse = response
                }
                val vivoJob = launch {
                    val response = VivoAppStoreRepository.search(term)
                    if (latestQuery.trim() == term) vivoResponse = response
                }
                val uptodownJob = launch {
                    val response = UptodownRepository.search(term)
                    if (latestQuery.trim() == term) uptodownResponse = response
                }
                fdroidJob.join()
                huaweiJob.join()
                vivoJob.join()
                uptodownJob.join()
            }
        } finally {
            if (query.trim() == term) baseSearchLoading = false
        }
        if (query.trim() == term && rememberSearchHistory) {
            recentSearches.remove(term)
            recentSearches.add(0, term)
            while (recentSearches.size > 12) recentSearches.removeAt(recentSearches.lastIndex)
            prefs.edit().putString("search_history", recentSearches.joinToString("\u001f")).apply()
        }
    }

    // GitHub search only runs for an explicit submission to avoid repeated repository API requests while typing.
    LaunchedEffect(searchSubmissionId) {
        val term = submittedSearchQuery.trim()
        if (term.isBlank() || searchSubmissionId == 0) return@LaunchedEffect
        githubSearchLoading = true
        githubResponse = null
        try {
            val response = GithubReleaseRepository.search(term)
            if (latestQuery.trim() == term) githubResponse = response
        } finally {
            if (latestQuery.trim() == term) githubSearchLoading = false
        }
    }

    // Enrich only the first few visible hits after the search response renders. This obtains real
    // package names and APK sizes without delaying the initial result list.
    LaunchedEffect(query, vivoResponse?.apps) {
        val term = query.trim()
        val response = vivoResponse ?: return@LaunchedEffect
        if (term.isBlank()) return@LaunchedEffect
        val candidates = response.apps.filter { app ->
            val key = "${term.lowercase()}|${app.sourceId.lowercase()}"
            val needsMetadata = app.bestRealPackageName().isBlank() || app.apkSizeBytes == null || app.developer.isBlank()
            needsMetadata && vivoMetadataLookups.add(key)
        }.take(2)
        if (candidates.isEmpty()) return@LaunchedEffect
        val enriched = VivoAppStoreRepository.enrichSearchApps(candidates, limit = candidates.size)
        if (latestQuery.trim() == term) {
            val byId = enriched.associateBy { it.sourceId }
            vivoResponse = response.copy(apps = response.apps.map { byId[it.sourceId] ?: it })
        }
    }

    LaunchedEffect(query, uptodownResponse?.apps) {
        val term = query.trim()
        val response = uptodownResponse ?: return@LaunchedEffect
        if (term.isBlank()) return@LaunchedEffect
        val candidates = response.apps.filter { app ->
            val key = "${term.lowercase()}|${app.sourceId.lowercase()}"
            val needsMetadata = app.bestRealPackageName().isBlank() || app.apkSizeBytes == null || app.developer.isBlank()
            needsMetadata && uptodownMetadataLookups.add(key)
        }.take(2)
        if (candidates.isEmpty()) return@LaunchedEffect
        val enriched = UptodownRepository.enrichSearchApps(candidates, limit = candidates.size)
        if (latestQuery.trim() == term) {
            val byUrl = enriched.associateBy { it.sourceUrl.ifBlank { it.sourceId } }
            uptodownResponse = response.copy(apps = response.apps.map { app -> byUrl[app.sourceUrl.ifBlank { app.sourceId }] ?: app })
        }
    }

    LaunchedEffect(query, huaweiResponse?.apps) {
        val term = query.trim()
        val response = huaweiResponse ?: return@LaunchedEffect
        if (term.isBlank()) return@LaunchedEffect
        val candidates = response.apps.filter { app ->
            val key = "${term.lowercase()}|${app.sourceId.lowercase()}"
            val needsMetadata = app.bestRealPackageName().isBlank() || app.apkSizeBytes == null || app.developer.isBlank()
            needsMetadata && huaweiMetadataLookups.add(key)
        }.take(2)
        if (candidates.isEmpty()) return@LaunchedEffect
        val enriched = HuaweiAppGalleryRepository.enrichSearchApps(candidates, limit = candidates.size)
        if (latestQuery.trim() == term) {
            val byId = enriched.associateBy { it.sourceId }
            huaweiResponse = response.copy(apps = response.apps.map { byId[it.sourceId] ?: it })
        }
    }

    // A single ranking pass is triggered by source responses. No source order is preferred.
    LaunchedEffect(query, fdroidResponse, githubResponse, huaweiResponse, vivoResponse, uptodownResponse, baseSearchLoading, githubSearchLoading) {
        val term = query.trim()
        if (term.isBlank()) {
            results = emptyList()
            errorMessage = null
        } else {
            results = AppSearchRanking.rankApps(
                term,
                mergeCatalogApps(
                    fdroidResponse?.apps.orEmpty() + githubResponse?.apps.orEmpty() +
                            huaweiResponse?.apps.orEmpty() + vivoResponse?.apps.orEmpty() + uptodownResponse?.apps.orEmpty()
                )
            ).take(60)
            val combinedErrors = listOfNotNull(
                fdroidResponse?.error?.takeUnless {
                    it.startsWith("接口请求成功（HTTP") && it.contains("解析出 0 条应用")
                },
                githubResponse?.error,
                huaweiResponse?.error,
                vivoResponse?.error,
                uptodownResponse?.error
            ).distinct().joinToString("；")
            errorMessage = if (results.isNotEmpty() || baseSearchLoading || githubSearchLoading) null else combinedErrors.takeIf { it.isNotBlank() }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    if (selectedApp != null) {
        FdroidAppDetailPage(
            app = selectedApp!!,
            onBack = {
                selectedApp = null
                onDetailPageVisibilityChange(false)
            }
        )
    } else Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = "返回",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(28.dp).clickable {
                    keyboardController?.hide()
                    onBack()
                }
            )
            Spacer(Modifier.width(12.dp))
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Search, contentDescription = "搜索", tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(10.dp))
                BasicTextField(
                    value = query,
                    onValueChange = { newValue -> onQueryChange(newValue) },
                    modifier = Modifier.weight(1f).height(24.dp).focusRequester(focusRequester),
                    singleLine = true,
                    cursorBrush = AppSpaceCursorBrush(),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        val term = query.trim()
                        if (term.isNotBlank()) {
                            submittedSearchQuery = term
                            searchSubmissionId += 1
                        }
                        keyboardController?.hide()
                    }),
                    decorationBox = { inner ->
                        Box(
                            modifier = Modifier.fillMaxWidth().height(24.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (query.isEmpty()) Text("搜索应用", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp, lineHeight = 22.sp)
                            inner()
                        }
                    }
                )
                if (query.isNotEmpty()) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = "清除搜索",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp).clickable { onQueryChange("") }
                    )
                }
            }
        }

        when {
            query.isBlank() -> Unit
            isLoading && results.isEmpty() -> {
                Spacer(Modifier.height(42.dp))
                Text("搜索中", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
            errorMessage != null && results.isEmpty() -> {
                Spacer(Modifier.height(32.dp))
                EmptyState("暂时无法获取搜索结果", errorMessage ?: "请检查网络后重试", Icons.Outlined.Search)
                Text(
                    "搜索服务暂时不可用时，稍后可重试。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            results.isEmpty() -> {
                Spacer(Modifier.height(56.dp))
                EmptyState("没有找到相关应用", "试试英文名称或包名", Icons.Outlined.Search)
            }
            else -> {
                if (baseSearchLoading || githubSearchLoading) {
                    Text("搜索中 · 正在查询其他来源", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Spacer(Modifier.height(6.dp))
                }
                Text("搜索结果", color = MaterialTheme.colorScheme.onBackground, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(results, key = { it.packageName }) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    selectedApp = item
                                    onDetailPageVisibilityChange(true)
                                }
                                .padding(vertical = 11.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CatalogAppIcon(item, 54)
                            Spacer(Modifier.width(13.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(3.dp))
                                val realPackage = item.bestRealPackageName()
                                val apkSize = formatCatalogSize(item.bestApkSizeBytes())
                                Text(
                                    when {
                                        realPackage.isNotBlank() && apkSize != null -> "$realPackage · $apkSize"
                                        realPackage.isNotBlank() -> "$realPackage · 安装包大小暂未提供"
                                        apkSize != null -> "包名暂未提供 · $apkSize"
                                        else -> if (item.source in setOf(AppCatalogSource.VIVO_APPSTORE, AppCatalogSource.UPTODOWN, AppCatalogSource.HUAWEI_APPGALLERY)) "包名与安装包大小暂未返回" else "包名未由来源提供"
                                    },
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    maxLines = 2
                                )
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    item.bestDeveloperName().ifBlank {
                                        item.summary.ifBlank {
                                            when (item.source) {
                                                AppCatalogSource.GITHUB -> "GitHub 公开项目"
                                                AppCatalogSource.HUAWEI_APPGALLERY -> "来自华为应用市场"
                                                AppCatalogSource.VIVO_APPSTORE -> "来自 vivo 应用商店"
                                                AppCatalogSource.UPTODOWN -> "来自 Uptodown"
                                                AppCatalogSource.F_DROID -> "来自 F-Droid 官方主仓库"
                                            }
                                        }
                                    },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.secondaryContainer)
                                    .padding(horizontal = 13.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    item.availableSourceVariants().joinToString(" · ") { it.label },
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppSourceSelector(
    sources: List<AppSourceVariant>,
    selectedSourceKey: String,
    onSourceSelected: (AppSourceVariant) -> Unit
) {
    if (sources.isEmpty()) return
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (sources.size > 1) "下载来源 · ${sources.size} 个" else "下载来源",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(7.dp))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            sources.forEach { source ->
                val selected = source.key == selectedSourceKey
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            width = 1.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(50)
                        )
                        .clickable { onSourceSelected(source) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = source.label,
                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun CatalogInstallButton(
    enabled: Boolean,
    busy: Boolean,
    progress: Float,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (busy) {
            androidx.compose.material3.CircularProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.size(22.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = "安装",
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun GithubAppDetailPage(
    app: FdroidApp,
    sourceOptions: List<AppSourceVariant>,
    selectedSourceKey: String,
    onSourceSelected: (AppSourceVariant) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var details by remember(app.sourceId) { mutableStateOf<GithubAppDetails?>(null) }
    var loading by remember(app.sourceId) { mutableStateOf(true) }
    var error by remember(app.sourceId) { mutableStateOf<String?>(null) }
    var historyExpanded by remember(app.sourceId) { mutableStateOf(false) }
    var downloadId by remember(app.sourceId) { mutableStateOf<Long?>(null) }
    var downloadProgress by remember(app.sourceId) { mutableFloatStateOf(0f) }
    var downloadStatus by remember(app.sourceId) { mutableStateOf("") }
    var downloadFinished by remember(app.sourceId) { mutableStateOf(false) }
    var installerLaunched by remember(app.sourceId) { mutableStateOf(false) }
    var releaseForAssetSelection by remember(app.sourceId) { mutableStateOf<GithubRelease?>(null) }

    LaunchedEffect(app.sourceId) {
        loading = true
        val result = GithubReleaseRepository.getAppDetails(app.sourceId)
        details = result.getOrNull()
        error = result.exceptionOrNull()?.message
        loading = false
    }

    fun queueGithubAsset(release: GithubRelease, asset: GithubReleaseAsset) {
        downloadProgress = 0f
        downloadStatus = "等待确认第三方 APK 下载…"
        downloadFinished = false
        installerLaunched = false
        showExternalApkDownloadConsent(
            context = context,
            sourceName = "GitHub Releases",
            onContinue = {
                downloadId = enqueueGithubApkDownload(context, app, release, asset)
                if (downloadId == null) downloadStatus = "无法开始下载，请稍后重试"
            },
            onCancel = { downloadStatus = "已取消从 GitHub Releases 下载 APK" }
        )
    }

    fun chooseOrQueue(release: GithubRelease) {
        when (release.apkAssets.size) {
            0 -> downloadStatus = "这个 Release 没有可直接安装的 APK 文件。"
            1 -> queueGithubAsset(release, release.apkAssets.first())
            else -> releaseForAssetSelection = release
        }
    }

    LaunchedEffect(downloadId) {
        val id = downloadId ?: return@LaunchedEffect
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        while (true) {
            val cursor = manager.query(DownloadManager.Query().setFilterById(id))
            var keepPolling = true
            cursor?.use {
                if (it.moveToFirst()) {
                    val statusIndex = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val bytesIndex = it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val totalIndex = it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    val status = if (statusIndex >= 0) it.getInt(statusIndex) else -1
                    val downloaded = if (bytesIndex >= 0) it.getLong(bytesIndex) else 0L
                    val total = if (totalIndex >= 0) it.getLong(totalIndex) else -1L
                    if (total > 0) downloadProgress = (downloaded.toFloat() / total).coerceIn(0f, 1f)
                    when (status) {
                        DownloadManager.STATUS_PENDING -> downloadStatus = "等待下载…"
                        DownloadManager.STATUS_RUNNING -> downloadStatus = if (total > 0) "正在下载 · ${(downloadProgress * 100).toInt()}%" else "正在下载…"
                        DownloadManager.STATUS_PAUSED -> downloadStatus = "下载暂停，等待网络恢复…"
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            downloadProgress = 1f
                            downloadFinished = true
                            downloadStatus = "下载完成，正在启动安装…"
                            if (!installerLaunched) {
                                val uri = runCatching { manager.getUriForDownloadedFile(id) }.getOrNull()
                                if (uri != null) {
                                    installerLaunched = true
                                    installApkUsingPreferences(context, uri, automatic = true) { downloadStatus = it }
                                } else downloadStatus = "下载完成，但无法读取 APK，请在下载中心重试。"
                            }
                            keepPolling = false
                        }
                        DownloadManager.STATUS_FAILED -> {
                            downloadStatus = "下载失败，请检查网络后重试。"
                            downloadFinished = false
                            keepPolling = false
                        }
                    }
                } else {
                    downloadStatus = "无法读取下载状态"
                    keepPolling = false
                }
            } ?: run {
                downloadStatus = "无法读取下载状态"
                keepPolling = false
            }
            if (!keepPolling) break
            kotlinx.coroutines.delay(500)
        }
    }

    val releaseList = details?.releases.orEmpty()
    val latest = releaseList.firstOrNull { !it.isPrerelease } ?: releaseList.firstOrNull()
    val versionText = latest?.tagName?.ifBlank { latest.title } ?: "暂无 Release"
    val infoTiles = buildList {
        if (latest != null) add("版本" to versionText)
        latest?.publishedAt?.take(10)?.takeIf { it.isNotBlank() }?.let { add("更新日期" to it) }
        details?.license?.takeIf { it.isNotBlank() }?.let { add("许可证" to it) }
        details?.stars?.takeIf { it > 0 }?.let { add("Stars" to it.toString()) }
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.ArrowBack, contentDescription = "返回", tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(28.dp).clickable(onClick = onBack))
            Spacer(Modifier.width(12.dp))
            Text("应用详情", color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            CatalogAppIcon(app, 82)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(app.name, color = MaterialTheme.colorScheme.onBackground, fontSize = 23.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp)
                Spacer(Modifier.height(5.dp))
                Text(details?.ownerLogin?.ifBlank { app.sourceId.substringBefore('/') } ?: app.sourceId.substringBefore('/'),
                    color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                Spacer(Modifier.height(7.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        AppSourceSelector(sourceOptions, selectedSourceKey, onSourceSelected)
        Spacer(Modifier.height(14.dp))
        Text(details?.description?.takeIf { it.isNotBlank() } ?: app.summary,
            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, lineHeight = 21.sp)
        Spacer(Modifier.height(16.dp))

        if (infoTiles.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                infoTiles.forEach { (label, value) ->
                    Column(Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 9.dp, vertical = 12.dp)) {
                        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        Spacer(Modifier.height(5.dp))
                        Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
        }

        CatalogInstallButton(
            enabled = latest != null && latest.apkAssets.isNotEmpty() &&
                    (downloadId == null || downloadFinished || downloadStatus.contains("失败")),
            busy = downloadId != null && !downloadFinished && !downloadStatus.contains("失败"),
            progress = downloadProgress,
            onClick = { latest?.let { chooseOrQueue(it) } }
        )
        if (loading) {
            Spacer(Modifier.height(10.dp))
            Text("正在加载 Release…", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        } else if (error != null && details == null) {
            Spacer(Modifier.height(10.dp))
            Text(error ?: "暂时无法加载 GitHub Release", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        } else if (latest == null || latest.apkAssets.isEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text("该项目暂时没有可直接安装的 APK Release。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
        if (downloadStatus.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            if (downloadId != null && !downloadFinished && !downloadStatus.contains("失败")) {
                androidx.compose.material3.LinearProgressIndicator(progress = { downloadProgress }, modifier = Modifier.fillMaxWidth().height(5.dp), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(Modifier.height(6.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    downloadStatus,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
                if (downloadId != null && !downloadFinished && !downloadStatus.contains("失败")) {
                    TextButton(onClick = {
                        val activeDownloadId = downloadId
                        if (activeDownloadId != null) {
                            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            manager.remove(activeDownloadId)
                            downloadId = null
                            downloadProgress = 0f
                            downloadFinished = false
                            installerLaunched = false
                            downloadStatus = "已取消下载"
                        }
                    }) {
                        Text("取消")
                    }
                }
            }
        }

        // GitHub README images are used as screenshots when a project documents its UI there.
        val readmeScreenshots = details?.readmeScreenshotUrls.orEmpty()
        if (readmeScreenshots.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            Text("应用截图", color = MaterialTheme.colorScheme.onBackground, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                readmeScreenshots.take(6).forEach { screenshotUrl -> FdroidRemoteScreenshot(screenshotUrl) }
            }
        }

        details?.homepage?.takeIf { it.startsWith("http") }?.let { url ->
            Spacer(Modifier.height(18.dp))
            Text("项目主页", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp,
                modifier = Modifier.clickable { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } })
        }
        details?.htmlUrl?.takeIf { it.startsWith("http") }?.let { url ->
            Spacer(Modifier.height(8.dp))
            Text("打开 GitHub 仓库", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp,
                modifier = Modifier.clickable { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } })
        }

        latest?.body?.takeIf { it.isNotBlank() }?.let { body ->
            Spacer(Modifier.height(24.dp))
            Text("更新内容", color = MaterialTheme.colorScheme.onBackground, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(9.dp))
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, lineHeight = 20.sp, maxLines = 15)
        }

        Spacer(Modifier.height(28.dp))
        val oldReleases = releaseList.filterNot { it == latest }
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable { historyExpanded = !historyExpanded }
                .padding(vertical = 12.dp, horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("历史版本", color = MaterialTheme.colorScheme.onBackground, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                Text(if (loading) "正在获取版本列表…" else if (oldReleases.isEmpty()) "暂无其他历史版本" else "${oldReleases.size} 个旧版本 · 点击展开",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Icon(if (historyExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                contentDescription = if (historyExpanded) "折叠历史版本" else "展开历史版本", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (historyExpanded) {
            oldReleases.forEach { old ->
                Column(Modifier.fillMaxWidth().padding(bottom = 10.dp).clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant).padding(13.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(old.tagName.ifBlank { old.title }, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(old.publishedAt.take(10), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        }
                        Text("下载", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp,
                            modifier = Modifier.clickable { chooseOrQueue(old) }.padding(horizontal = 10.dp, vertical = 8.dp))
                    }
                    if (old.apkAssets.size > 1) {
                        Text("包含 ${old.apkAssets.size} 个 APK 变体", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    releaseForAssetSelection?.let { release ->
        AlertDialog(
            onDismissRequest = { releaseForAssetSelection = null },
            title = { Text("选择安装包") },
            text = {
                Column {
                    Text("${release.tagName.ifBlank { release.title }} 包含多个 APK，请选择适合你设备的安装包。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    release.apkAssets.forEach { asset ->
                        TextButton(onClick = { releaseForAssetSelection = null; queueGithubAsset(release, asset) }) {
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(asset.name, color = MaterialTheme.colorScheme.primary)
                                Text(formatGithubAssetSize(asset.sizeBytes), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { releaseForAssetSelection = null }) { Text("取消") } }
        )
    }
}

private fun showExternalApkDownloadConsent(
    context: Context,
    sourceName: String,
    onContinue: () -> Unit,
    onCancel: () -> Unit
) {
    android.app.AlertDialog.Builder(context)
        .setTitle("下载来自 F-Droid 以外的 APK？")
        .setMessage(
            "这个安装包来自 $sourceName，不属于当前 F-Droid 仓库。F-Droid 未审核或从源码构建此 APK。" +
                "继续下载意味着你选择绕过 F-Droid 对该安装包的检查。请先确认来源和开发者可信；" +
                "你也可以取消并使用 F-Droid 官方仓库中的版本。"
        )
        .setNegativeButton("取消下载") { _, _ -> onCancel() }
        .setPositiveButton("继续下载") { _, _ -> onContinue() }
        .setOnCancelListener { onCancel() }
        .show()
}

private fun enqueueGithubApkDownload(
    context: Context,
    app: FdroidApp,
    release: GithubRelease,
    asset: GithubReleaseAsset
): Long? = try {
    val safeRepo = app.sourceId.replace(Regex("[^A-Za-z0-9._-]"), "_").take(35)
    val safeTag = release.tagName.ifBlank { release.title }.replace(Regex("[^A-Za-z0-9._-]"), "_").take(35)
    val rawAssetName = asset.name.substringAfterLast('/').replace(Regex("[^A-Za-z0-9._-]"), "_")
    val safeAssetStem = rawAssetName.removeSuffix(".apk").removeSuffix(".APK").take(70)
    val filename = "${safeRepo}_${safeTag}_${safeAssetStem}.apk"
    val request = DownloadManager.Request(Uri.parse(asset.downloadUrl)).apply {
        setTitle("${app.name} ${release.tagName.ifBlank { release.title }}")
        setDescription("正在从 GitHub Releases 下载 APK")
        setMimeType("application/vnd.android.package-archive")
        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        setAllowedOverMetered(true)
        setAllowedOverRoaming(false)
        setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
    }
    val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    manager.enqueue(request).also { Toast.makeText(context, "已开始下载 ${app.name}", Toast.LENGTH_SHORT).show() }
} catch (e: Exception) {
    Toast.makeText(context, "无法开始下载：${e.localizedMessage ?: "未知错误"}", Toast.LENGTH_LONG).show()
    null
}

private fun formatGithubAssetSize(bytes: Long): String = when {
    bytes >= 1024L * 1024L -> String.format(java.util.Locale.getDefault(), "%.1f MB", bytes / (1024f * 1024f))
    bytes >= 1024L -> String.format(java.util.Locale.getDefault(), "%.0f KB", bytes / 1024f)
    else -> "$bytes B"
}

@Composable
private fun StoreAppDetailPage(
    app: FdroidApp,
    sourceOptions: List<AppSourceVariant>,
    selectedSourceKey: String,
    onSourceSelected: (AppSourceVariant) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isDescriptionExpanded by remember(app.source, app.sourceId) { mutableStateOf(false) }
    var details by remember(app.source, app.sourceId) { mutableStateOf<StoreAppDetails?>(null) }
    var loading by remember(app.source, app.sourceId) { mutableStateOf(true) }
    var error by remember(app.source, app.sourceId) { mutableStateOf<String?>(null) }
    var downloadId by remember(app.source, app.sourceId) { mutableStateOf<Long?>(null) }
    var downloadProgress by remember(app.source, app.sourceId) { mutableFloatStateOf(0f) }
    var downloadStatus by remember(app.source, app.sourceId) { mutableStateOf("") }
    var resolvingDownloadUrl by remember(app.source, app.sourceId) { mutableStateOf(false) }
    val detailScope = rememberCoroutineScope()
    var downloadFinished by remember(app.source, app.sourceId) { mutableStateOf(false) }
    var installerLaunched by remember(app.source, app.sourceId) { mutableStateOf(false) }

    LaunchedEffect(app.source, app.sourceId) {
        loading = true
        error = null
        details = null
        val result = when (app.source) {
            AppCatalogSource.HUAWEI_APPGALLERY -> HuaweiAppGalleryRepository.getAppDetails(app.sourceId)
            AppCatalogSource.VIVO_APPSTORE -> VivoAppStoreRepository.getAppDetails(app.sourceId)
            AppCatalogSource.UPTODOWN -> UptodownRepository.getAppDetails(app.sourceUrl.ifBlank { app.sourceId })
            else -> Result.failure(IllegalArgumentException("不支持的应用商店来源"))
        }
        details = result.getOrNull()
        error = result.exceptionOrNull()?.message
        loading = false
    }

    fun enqueueStoreDownload(info: StoreAppDetails) {
        val uri = runCatching { Uri.parse(info.downloadUrl) }.getOrNull()
        if (uri == null || uri.scheme != "https" || info.downloadUrl.isBlank()) {
            downloadStatus = "商店没有提供可用的 HTTPS 安装包链接。"
            return
        }
        showExternalApkDownloadConsent(
            context = context,
            sourceName = app.source.displayName,
            onContinue = { startStoreDownload(info) },
            onCancel = { downloadStatus = "已取消从 ${app.source.displayName} 下载 APK" }
        )
    }

    fun startStoreDownload(info: StoreAppDetails) {
        val uri = runCatching { Uri.parse(info.downloadUrl) }.getOrNull()
        if (uri == null || uri.scheme != "https" || info.downloadUrl.isBlank()) {
            downloadStatus = "商店没有提供可用的 HTTPS 安装包链接。"
            return
        }
        try {
            val safeBaseName = info.packageName.takeIf { it.isNotBlank() && !it.startsWith("huawei:") && !it.startsWith("vivo:") && !it.startsWith("uptodown:") }
                ?: app.name.ifBlank { info.name }
            val safeName = safeBaseName.replace(Regex("[^A-Za-z0-9._-]"), "_").take(80).ifBlank { "appspace-app" }
            val safeVersion = info.versionName.replace(Regex("[^A-Za-z0-9._-]"), "_").take(48).ifBlank { "latest" }
            val sourceName = app.source.displayName
            val request = DownloadManager.Request(uri).apply {
                setTitle("${app.name.ifBlank { info.name }} ${info.versionName}".trim())
                setDescription("正在从 $sourceName 下载 APK")
                setMimeType(APK_MIME_TYPE)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(false)
                if (app.source == AppCatalogSource.HUAWEI_APPGALLERY) {
                    // AppGallery's public-client CDN may depend on the same UA as the metadata API.
                    addRequestHeader("User-Agent", HUAWEI_DOWNLOAD_USER_AGENT)
                    addRequestHeader("Accept", "application/vnd.android.package-archive, application/octet-stream, */*")
                }
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "${safeName}_${safeVersion}.apk"
                )
            }
            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadProgress = 0f
            downloadFinished = false
            installerLaunched = false
            downloadStatus = "已加入下载队列…"
            downloadId = manager.enqueue(request)
            Toast.makeText(context, "已开始下载 ${app.name.ifBlank { info.name }}", Toast.LENGTH_SHORT).show()
        } catch (failure: Exception) {
            downloadStatus = "无法开始下载：${failure.localizedMessage ?: "未知错误"}"
        }
    }

    LaunchedEffect(downloadId) {
        val id = downloadId ?: return@LaunchedEffect
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        while (true) {
            var keepPolling = true
            val cursor = runCatching { manager.query(DownloadManager.Query().setFilterById(id)) }.getOrNull()
            cursor?.use {
                if (!it.moveToFirst()) {
                    downloadStatus = "无法读取下载状态"
                    keepPolling = false
                } else {
                    val statusColumn = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val bytesColumn = it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val totalColumn = it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    val status = if (statusColumn >= 0) it.getInt(statusColumn) else -1
                    val downloaded = if (bytesColumn >= 0) it.getLong(bytesColumn) else 0L
                    val total = if (totalColumn >= 0) it.getLong(totalColumn) else -1L
                    if (total > 0L) downloadProgress = (downloaded.toFloat() / total).coerceIn(0f, 1f)
                    when (status) {
                        DownloadManager.STATUS_PENDING -> downloadStatus = "等待下载…"
                        DownloadManager.STATUS_RUNNING -> downloadStatus = if (total > 0) "正在下载 · ${(downloadProgress * 100).toInt()}%" else "正在下载…"
                        DownloadManager.STATUS_PAUSED -> downloadStatus = "下载暂停，等待网络恢复…"
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            downloadProgress = 1f
                            downloadFinished = true
                            keepPolling = false
                            val apkUri = runCatching { manager.getUriForDownloadedFile(id) }.getOrNull()
                            if (apkUri != null && !installerLaunched) {
                                installerLaunched = true
                                downloadStatus = "下载完成，正在启动安装…"
                                installApkUsingPreferences(
                                    context = context,
                                    uri = apkUri,
                                    automatic = true,
                                    expectedPackageName = details?.packageName
                                        ?.takeIf { packageName -> packageName.isNotBlank() }
                                        ?: app.appPackageName.takeIf { packageName -> packageName.isNotBlank() },
                                    onStatus = { statusMessage: String ->
                                        downloadStatus = statusMessage
                                    }
                                )
                            } else if (apkUri == null) {
                                downloadStatus = "下载完成，但无法读取 APK；请在下载中心重试。"
                            }
                        }
                        DownloadManager.STATUS_FAILED -> {
                            downloadStatus = "下载失败，请检查网络或地区限制后重试。"
                            keepPolling = false
                        }
                    }
                }
            } ?: run {
                downloadStatus = "无法读取下载状态"
                keepPolling = false
            }
            if (!keepPolling) break
            kotlinx.coroutines.delay(500)
        }
    }

    val info = details
    val shownApp = if (!info?.iconUrl.isNullOrBlank()) app.copy(iconUrl = info!!.iconUrl) else app
    val sourceName = app.source.displayName
    val displayName = app.name.ifBlank { info?.name.orEmpty() }
    val realPackageName = info?.packageName?.takeIf {
        it.isNotBlank() && !it.startsWith("huawei:") && !it.startsWith("vivo:") && !it.startsWith("uptodown:")
    }
    val publisher = info?.developer?.takeIf { it.isNotBlank() }
        ?: realPackageName
        ?: sourceName
    val description = info?.description?.takeIf { it.isNotBlank() } ?: app.summary
    // Match the F-Droid detail page's compact three-card information row.
    val infoTiles = buildList {
        info?.versionName?.takeIf { it.isNotBlank() }?.let { add("版本" to it) }
        info?.releaseDate?.takeIf { it.isNotBlank() }?.let { add("更新日期" to it.take(10)) }
        realPackageName?.let { add("包名" to it) }
        formatCatalogSize(info?.apkSizeBytes)?.let { add("安装包" to it) }
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.ArrowBack,
                contentDescription = "返回",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(28.dp).clickable(onClick = onBack)
            )
            Spacer(Modifier.width(12.dp))
            Text("应用详情", color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        }

        // Same header and source-selector arrangement as F-Droid.
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            CatalogAppIcon(shownApp, 82)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(displayName, color = MaterialTheme.colorScheme.onBackground, fontSize = 23.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp)
                Spacer(Modifier.height(5.dp))
                Text(publisher, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                Spacer(Modifier.height(7.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        AppSourceSelector(sourceOptions, selectedSourceKey, onSourceSelected)
        Spacer(Modifier.height(14.dp))
        if (app.summary.isNotBlank()) {
            Text(app.summary, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, lineHeight = 21.sp)
            Spacer(Modifier.height(16.dp))
        }

        if (infoTiles.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                infoTiles.forEach { (label, value) ->
                    Column(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 12.dp)
                    ) {
                        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        Spacer(Modifier.height(5.dp))
                        Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold, maxLines = 2)
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
        }

        CatalogInstallButton(
            enabled = !loading && (
                    info?.downloadUrl?.isNotBlank() == true ||
                            (app.source == AppCatalogSource.UPTODOWN && info?.downloadAvailable == true)
                    ) && !resolvingDownloadUrl &&
                    (downloadId == null || downloadFinished || downloadStatus.contains("失败")),
            busy = resolvingDownloadUrl || (downloadId != null && !downloadFinished && !downloadStatus.contains("失败")),
            progress = downloadProgress,
            onClick = {
                val currentInfo = info
                if (currentInfo != null && app.source == AppCatalogSource.UPTODOWN && currentInfo.downloadUrl.isBlank()) {
                    resolvingDownloadUrl = true
                    downloadStatus = "正在获取安全下载地址…"
                    detailScope.launch {
                        val resolution = UptodownRepository.resolveDownloadUrlForApp(app.sourceUrl.ifBlank { app.sourceId })
                        resolution.onSuccess { directUrl ->
                            val resolvedInfo = currentInfo.copy(downloadUrl = directUrl)
                            details = resolvedInfo
                            resolvingDownloadUrl = false
                            enqueueStoreDownload(resolvedInfo)
                        }.onFailure { failure ->
                            resolvingDownloadUrl = false
                            downloadStatus = "无法获取 Uptodown 下载地址：${failure.message ?: "请稍后重试"}"
                        }
                    }
                } else if (currentInfo != null && app.source == AppCatalogSource.HUAWEI_APPGALLERY) {
                    // Do not reuse the URL fetched during search-result enrichment; Huawei CDN URLs may expire.
                    resolvingDownloadUrl = true
                    downloadStatus = "正在刷新华为安装包地址…"
                    detailScope.launch {
                        HuaweiAppGalleryRepository.refreshDownloadDetails(app.sourceId, currentInfo)
                            .onSuccess { refreshedInfo ->
                                details = refreshedInfo
                                resolvingDownloadUrl = false
                                enqueueStoreDownload(refreshedInfo)
                            }
                            .onFailure { failure ->
                                resolvingDownloadUrl = false
                                downloadStatus = "无法刷新华为下载地址：${failure.message ?: "请稍后重试"}"
                            }
                    }
                } else currentInfo?.let(::enqueueStoreDownload)
            }
        )
        if (loading) {
            Spacer(Modifier.height(8.dp))
            Text("正在加载商店应用信息…", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        } else if (error != null && info == null) {
            Spacer(Modifier.height(8.dp))
            Text(error ?: "暂时无法获取应用信息", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, lineHeight = 18.sp)
        } else if (info?.downloadUrl.isNullOrBlank() && !(app.source == AppCatalogSource.UPTODOWN && info?.downloadAvailable == true)) {
            Spacer(Modifier.height(8.dp))
            Text("商店目前没有提供可用的匿名 APK 下载链接；这可能与地区、设备型号或商店策略有关。",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 18.sp)
        }
        if (downloadId != null || downloadStatus.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            if (downloadId != null && !downloadFinished && !downloadStatus.contains("失败")) {
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { downloadProgress }, modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(5.dp)),
                    color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(Modifier.height(5.dp))
            }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(downloadStatus, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp, lineHeight = 18.sp)
                if (downloadId != null && !downloadFinished && !downloadStatus.contains("失败")) {
                    TextButton(onClick = {
                        val activeDownloadId = downloadId
                        if (activeDownloadId != null) {
                            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            manager.remove(activeDownloadId)
                            downloadId = null
                            downloadProgress = 0f
                            downloadFinished = false
                            installerLaunched = false
                            resolvingDownloadUrl = false
                            downloadStatus = "已取消下载"
                        }
                    }) { Text("取消") }
                }
            }
        }

        // Same screenshot section placement and horizontal presentation as F-Droid.
        val screenshots = info?.screenshotUrls.orEmpty().distinct()
        if (screenshots.isNotEmpty()) {
            Spacer(Modifier.height(25.dp))
            Text("应用截图", color = MaterialTheme.colorScheme.onBackground, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                screenshots.take(6).forEach { screenshotUrl ->
                    FdroidRemoteScreenshot(
                        screenshotUrl,
                        info?.sourceUrl?.takeIf { it.isNotBlank() } ?: app.sourceUrl
                    )
                }
            }
        }

        if (description.isNotBlank()) {
            Spacer(Modifier.height(26.dp))
            Text("关于此应用", color = MaterialTheme.colorScheme.onBackground, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(9.dp))
            Text(
                description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 6
            )
            if (description.length > 260) {
                Text(
                    if (isDescriptionExpanded) "收起" else "展开",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { isDescriptionExpanded = !isDescriptionExpanded }.padding(top = 8.dp)
                )
            }
            val sourceUrl = info?.sourceUrl?.takeIf { it.startsWith("https://") } ?: app.sourceUrl
            if (sourceUrl.startsWith("https://")) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "应用市场页面",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        modifier = Modifier.clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                            .clickable {
                                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(sourceUrl))) }
                                    .onFailure { Toast.makeText(context, "无法打开链接", Toast.LENGTH_SHORT).show() }
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(22.dp))
    }
}

@Composable
private fun FdroidAppDetailPage(app: FdroidApp, onBack: () -> Unit) {
    val sourceOptions = remember(app.packageName, app.source, app.sourceId, app.alternateSources) {
        app.availableSourceVariants()
    }
    var selectedSourceKey by remember(app.packageName, app.source, app.sourceId, app.alternateSources) {
        mutableStateOf(app.asSourceVariant().key)
    }
    val selectedVariant = sourceOptions.firstOrNull { it.key == selectedSourceKey } ?: sourceOptions.first()
    val selectedApp = app.forSourceVariant(selectedVariant)
    val onSourceSelected: (AppSourceVariant) -> Unit = { selectedSourceKey = it.key }

    if (selectedApp.source == AppCatalogSource.HUAWEI_APPGALLERY ||
        selectedApp.source == AppCatalogSource.VIVO_APPSTORE ||
        selectedApp.source == AppCatalogSource.UPTODOWN) {
        StoreAppDetailPage(
            app = selectedApp,
            sourceOptions = sourceOptions,
            selectedSourceKey = selectedVariant.key,
            onSourceSelected = onSourceSelected,
            onBack = onBack
        )
    } else if (selectedApp.source == AppCatalogSource.GITHUB) {
        GithubAppDetailPage(
            app = selectedApp,
            sourceOptions = sourceOptions,
            selectedSourceKey = selectedVariant.key,
            onSourceSelected = onSourceSelected,
            onBack = onBack
        )
    } else {
        FdroidAppDetailContent(
            app = selectedApp,
            sourceOptions = sourceOptions,
            selectedSourceKey = selectedVariant.key,
            onSourceSelected = onSourceSelected,
            onBack = onBack
        )
    }
}

@Composable
private fun FdroidAppDetailContent(
    app: FdroidApp,
    sourceOptions: List<AppSourceVariant>,
    selectedSourceKey: String,
    onSourceSelected: (AppSourceVariant) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isHistoryExpanded by remember(app.packageName) { mutableStateOf(false) }
    var isDescriptionExpanded by remember(app.packageName) { mutableStateOf(false) }
    var details by remember(app.packageName) { mutableStateOf<FdroidAppDetails?>(null) }
    var pageMetadata by remember(app.packageName) { mutableStateOf<FdroidAppPageMetadata?>(null) }
    var loading by remember(app.packageName) { mutableStateOf(true) }
    var error by remember(app.packageName) { mutableStateOf<String?>(null) }
    var downloadId by remember(app.packageName) { mutableStateOf<Long?>(null) }
    var downloadRelease by remember(app.packageName) { mutableStateOf<FdroidRelease?>(null) }
    var downloadProgress by remember(app.packageName) { mutableFloatStateOf(0f) }
    var downloadStatus by remember(app.packageName) { mutableStateOf("") }
    var downloadFinished by remember(app.packageName) { mutableStateOf(false) }
    var installerLaunched by remember(app.packageName) { mutableStateOf(false) }

    LaunchedEffect(app.packageName) {
        loading = true
        error = null
        val releaseRequest = async { FdroidSearchRepository.getAppDetails(app.packageName) }
        val metadataRequest = async { FdroidSearchRepository.getAppPageMetadata(app.packageName) }
        val releaseResult = releaseRequest.await()
        releaseResult.onSuccess { details = it }
            .onFailure { error = it.message ?: "暂时无法获取版本信息" }
        pageMetadata = metadataRequest.await().getOrNull()
        loading = false
    }

    LaunchedEffect(downloadId) {
        val id = downloadId ?: return@LaunchedEffect
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        while (true) {
            val query = DownloadManager.Query().setFilterById(id)
            val cursor = manager.query(query)
            var keepPolling = true
            cursor?.use {
                if (it.moveToFirst()) {
                    val statusIndex = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val downloadedIndex = it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val totalIndex = it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    val status = if (statusIndex >= 0) it.getInt(statusIndex) else -1
                    val downloaded = if (downloadedIndex >= 0) it.getLong(downloadedIndex) else 0L
                    val total = if (totalIndex >= 0) it.getLong(totalIndex) else -1L
                    if (total > 0L) downloadProgress = (downloaded.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                    when (status) {
                        DownloadManager.STATUS_PENDING -> downloadStatus = "等待下载…"
                        DownloadManager.STATUS_RUNNING -> downloadStatus = if (total > 0L) "正在下载 · ${(downloadProgress * 100).toInt()}%" else "正在下载…"
                        DownloadManager.STATUS_PAUSED -> downloadStatus = "下载暂停，等待网络恢复…"
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            downloadProgress = 1f
                            downloadStatus = "下载完成，正在启动安装…"
                            downloadFinished = true
                            if (!installerLaunched) {
                                val apkUri = try { manager.getUriForDownloadedFile(id) } catch (_: Exception) { null }
                                if (apkUri != null) {
                                    installerLaunched = true
                                    installApkUsingPreferences(
                                        context, apkUri, automatic = true,
                                        expectedPackageName = app.packageName
                                    ) { message ->
                                        downloadStatus = message
                                    }
                                } else {
                                    downloadStatus = "下载完成，但无法读取 APK 文件，请到下载中心重试安装。"
                                }
                            }
                            keepPolling = false
                        }
                        DownloadManager.STATUS_FAILED -> {
                            downloadStatus = "下载失败，请检查网络后重试"
                            downloadFinished = false
                            keepPolling = false
                        }
                    }
                } else {
                    downloadStatus = "无法读取下载状态"
                    keepPolling = false
                }
            } ?: run {
                downloadStatus = "无法读取下载状态"
                keepPolling = false
            }
            if (!keepPolling) break
            kotlinx.coroutines.delay(500)
        }
    }

    val selectedRelease = details?.releases?.firstOrNull { it.isSuggested }
        ?: details?.releases?.firstOrNull()

    fun queueReleaseDownload(release: FdroidRelease) {
        downloadProgress = 0f
        downloadStatus = "正在加入下载队列…"
        downloadFinished = false
        installerLaunched = false
        downloadRelease = release
        downloadId = enqueueFdroidApkDownload(context, app, release)
        if (downloadId == null) downloadStatus = "无法开始下载，请稍后重试"
    }

    val description = pageMetadata?.description.orEmpty().ifBlank { app.summary }
    val metadata = pageMetadata
    val infoTiles = buildList {
        selectedRelease?.let { add("版本" to it.versionName) }
        metadata?.addedOn?.takeIf { it.isNotBlank() }?.let { add("更新日期" to it) }
        metadata?.license?.takeIf { it.isNotBlank() }?.let { add("许可证" to it) }
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.ArrowBack,
                contentDescription = "返回",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(28.dp).clickable(onClick = onBack)
            )
            Spacer(Modifier.width(12.dp))
            Text("应用详情", color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        }

        // Header: icon, app title, publisher and source.
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            CatalogAppIcon(app, 82)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(app.name, color = MaterialTheme.colorScheme.onBackground, fontSize = 23.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp)
                Spacer(Modifier.height(5.dp))
                Text(metadata?.authorName?.ifBlank { app.packageName } ?: app.packageName,
                    color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                Spacer(Modifier.height(7.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        AppSourceSelector(sourceOptions, selectedSourceKey, onSourceSelected)
        Spacer(Modifier.height(14.dp))
        if (app.summary.isNotBlank()) {
            Text(app.summary, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, lineHeight = 21.sp)
            Spacer(Modifier.height(16.dp))
        }

        // Compact Play-style fact row. Fields that aren't provided by F-Droid are omitted.
        if (infoTiles.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                infoTiles.forEach { (label, value) ->
                    Column(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 12.dp)
                    ) {
                        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        Spacer(Modifier.height(5.dp))
                        Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold, maxLines = 2)
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
        }

        // The primary action appears before screenshots and is always labelled only "安装".
        CatalogInstallButton(
            enabled = selectedRelease != null &&
                    (downloadId == null || downloadFinished || downloadStatus.contains("失败")),
            busy = downloadId != null && !downloadFinished && !downloadStatus.contains("失败"),
            progress = downloadProgress,
            onClick = { selectedRelease?.let { queueReleaseDownload(it) } }
        )
        if (loading) {
            Spacer(Modifier.height(8.dp))
            Text("正在加载版本信息…", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        } else if (error != null && selectedRelease == null) {
            Spacer(Modifier.height(8.dp))
            Text(error ?: "暂时无法获取版本信息", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        } else if (selectedRelease == null) {
            Spacer(Modifier.height(8.dp))
            Text("暂时没有可用安装版本", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
        if (downloadId != null || downloadStatus.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            if (downloadId != null && !downloadFinished && !downloadStatus.contains("失败")) {
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { downloadProgress },
                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(5.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(Modifier.height(5.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    downloadStatus,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
                if (downloadId != null && !downloadFinished && !downloadStatus.contains("失败")) {
                    TextButton(onClick = {
                        val activeDownloadId = downloadId
                        if (activeDownloadId != null) {
                            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            manager.remove(activeDownloadId)
                            downloadId = null
                            downloadProgress = 0f
                            downloadFinished = false
                            installerLaunched = false
                            downloadStatus = "已取消下载"
                        }
                    }) {
                        Text("取消")
                    }
                }
            }
        }

        // Screenshots are only shown when the official package page provides them.
        if (!metadata?.screenshotUrls.isNullOrEmpty()) {
            Spacer(Modifier.height(25.dp))
            Text("应用截图", color = MaterialTheme.colorScheme.onBackground, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                metadata!!.screenshotUrls.take(6).forEach { screenshotUrl ->
                    FdroidRemoteScreenshot(screenshotUrl, metadata?.sourceCodeUrl)
                }
            }
        }

        if (description.isNotBlank()) {
            Spacer(Modifier.height(26.dp))
            Text("关于此应用", color = MaterialTheme.colorScheme.onBackground, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(9.dp))
            Text(
                description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 6
            )
            if (description.length > 260) {
                Text(
                    if (isDescriptionExpanded) "收起" else "展开",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { isDescriptionExpanded = !isDescriptionExpanded }.padding(top = 8.dp)
                )
            }
            val links = listOfNotNull(
                metadata?.websiteUrl?.takeIf { it.startsWith("http") }?.let { "官方网站" to it },
                metadata?.sourceCodeUrl?.takeIf { it.startsWith("http") }?.let { "源代码" to it },
                metadata?.issueTrackerUrl?.takeIf { it.startsWith("http") }?.let { "问题反馈" to it },
                metadata?.changelogUrl?.takeIf { it.startsWith("http") }?.let { "更新日志" to it }
            ).distinctBy { it.second }
            if (links.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    links.forEach { (label, url) ->
                        Text(
                            label,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            modifier = Modifier.clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                                .clickable {
                                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                                        .onFailure { Toast.makeText(context, "无法打开链接", Toast.LENGTH_SHORT).show() }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        if (!metadata?.whatsNew.isNullOrBlank()) {
            Spacer(Modifier.height(24.dp))
            Text("更新内容", color = MaterialTheme.colorScheme.onBackground, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(9.dp))
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant).padding(15.dp)
            ) {
                metadata!!.whatsNew.lines().filter { it.isNotBlank() }.forEach { line ->
                    Text(line, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, lineHeight = 20.sp)
                }
            }
        }

        // Historical versions are intentionally the final section and collapsed by default.
        Spacer(Modifier.height(28.dp))
        val history = details?.releases.orEmpty().filterNot { it.versionCode == selectedRelease?.versionCode }
        Row(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { isHistoryExpanded = !isHistoryExpanded }
                .padding(vertical = 12.dp, horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("历史版本", color = MaterialTheme.colorScheme.onBackground, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    when {
                        loading -> "正在获取版本列表…"
                        history.isEmpty() -> "暂无其他历史版本"
                        else -> "${history.size} 个旧版本 · 点击展开"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
            Icon(
                imageVector = if (isHistoryExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                contentDescription = if (isHistoryExpanded) "折叠历史版本" else "展开历史版本",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isHistoryExpanded) {
            if (history.isEmpty()) {
                Text("F-Droid 暂未返回其他历史版本。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            } else {
                history.forEach { oldRelease ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(start = 14.dp, end = 10.dp, top = 12.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(oldRelease.versionName, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(3.dp))
                            Text("Version code ${oldRelease.versionCode}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        }
                        Text(
                            "下载",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clip(RoundedCornerShape(24.dp))
                                .clickable(enabled = downloadId == null || downloadFinished || downloadStatus.contains("失败")) {
                                    queueReleaseDownload(oldRelease)
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(22.dp))
    }
}

private object RemoteScreenshotBitmapCache {
    private val cache = object : android.util.LruCache<String, Bitmap>(16 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = (value.byteCount / 1024).coerceAtLeast(1)
    }
    @Synchronized fun get(url: String): Bitmap? = cache.get(url)
    @Synchronized fun put(url: String, bitmap: Bitmap) { cache.put(url, bitmap) }
}

private val screenshotNetworkSlots = Semaphore(3)

private suspend fun loadRemoteScreenshotBitmap(imageUrl: String, sourcePageUrl: String?): Bitmap? =
    screenshotNetworkSlots.withPermit {
        withContext(Dispatchers.IO) {
            loadRemoteScreenshotBitmapBlocking(imageUrl, sourcePageUrl)
        }
    }

private fun loadRemoteScreenshotBitmapBlocking(imageUrl: String, sourcePageUrl: String?): Bitmap? {
    var connection: java.net.HttpURLConnection? = null
    try {
        val url = java.net.URL(imageUrl.trim())
        if (!url.protocol.equals("https", true)) return null
        connection = (url.openConnection() as java.net.HttpURLConnection).apply {
            connectTimeout = 5500
            readTimeout = 5500
            instanceFollowRedirects = true
            requestMethod = "GET"
            setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android ${Build.VERSION.RELEASE}) AppleWebKit/537.36 Chrome/131.0 Mobile Safari/537.36")
            setRequestProperty("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8")
            setRequestProperty("Accept-Encoding", "identity")
            val fallbackReferrer = when {
                url.host.contains("huawei", true) || url.host.contains("dbankcdn", true) || url.host.contains("hispace", true) -> "https://appgallery.huawei.com/"
                else -> null
            }
            val referer = sourcePageUrl?.takeIf { it.startsWith("https://", true) } ?: fallbackReferrer
            if (!referer.isNullOrBlank()) setRequestProperty("Referer", referer)
            setRequestProperty("Cache-Control", "max-age=3600")
        }
        val status = connection.responseCode
        if (status !in 200..299) return null
        val contentType = connection.contentType.orEmpty().lowercase(java.util.Locale.ROOT)
        if (contentType.contains("text/html") || contentType.contains("application/json")) return null
        val declaredLength = connection.getHeaderFieldLong("Content-Length", -1L)
        if (declaredLength > 16L * 1024L * 1024L) return null
        val bytes = connection.inputStream.use { input ->
            val output = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            var total = 0
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                total += count
                if (total > 16 * 1024 * 1024) return null
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        }
        if (bytes.isEmpty()) return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (bounds.outWidth / sample > 700 || bounds.outHeight / sample > 1200) sample *= 2
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = false
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    } catch (_: Exception) {
        return null
    } finally {
        try { connection?.disconnect() } catch (_: Exception) { }
    }
}

@Composable
private fun FdroidRemoteScreenshot(imageUrl: String, sourcePageUrl: String? = null) {
    var bitmap by remember(imageUrl) { mutableStateOf(RemoteScreenshotBitmapCache.get(imageUrl)) }
    var failed by remember(imageUrl) { mutableStateOf(false) }
    LaunchedEffect(imageUrl, sourcePageUrl) {
        bitmap = RemoteScreenshotBitmapCache.get(imageUrl)
        failed = false
        if (bitmap == null) {
            val loadedBitmap = loadRemoteScreenshotBitmap(imageUrl, sourcePageUrl)
            bitmap = loadedBitmap
            if (loadedBitmap != null) RemoteScreenshotBitmapCache.put(imageUrl, loadedBitmap)
            failed = loadedBitmap == null
        }
    }
    Box(
        modifier = Modifier.width(152.dp).height(268.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        when {
            bitmap != null -> Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "应用截图",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            failed -> Text(
                "截图暂不可用",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            else -> androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CatalogAppIcon(app: FdroidApp, sizeDp: Int) {
    val context = LocalContext.current
    var resolvedIconUrl by remember(
        context,
        app.source,
        app.sourceId,
        app.iconUrl,
        app.sourceDefaultBranch,
        app.sourceIconDownloadUrl,
        app.sourceIconVersionKey
    ) {
        mutableStateOf(
            if (app.source == AppCatalogSource.GITHUB) {
                // Display any previously cached icon immediately while a changed release is refreshed.
                GithubReleaseRepository.getExistingCachedAppIconPath(context, app.sourceId)
            } else app.iconUrl
        )
    }

    LaunchedEffect(
        app.source,
        app.sourceId,
        app.iconUrl,
        app.sourceDefaultBranch,
        app.sourceIconDownloadUrl,
        app.sourceIconVersionKey,
        app.sourceIconAssetSizeBytes
    ) {
        resolvedIconUrl = if (app.source == AppCatalogSource.GITHUB) {
            val extractedIcon = GithubReleaseRepository.getCachedApkIconPath(
                context = context,
                fullName = app.sourceId,
                assetUrl = app.sourceIconDownloadUrl,
                versionKey = app.sourceIconVersionKey,
                expectedSizeBytes = app.sourceIconAssetSizeBytes
            )
            when {
                extractedIcon.isNotBlank() -> extractedIcon
                resolvedIconUrl.isNotBlank() -> resolvedIconUrl
                else -> GithubReleaseRepository.getRepositoryIconUrl(app.sourceId, app.sourceDefaultBranch)
            }
        } else {
            app.iconUrl
        }
    }
    FdroidRemoteIcon(resolvedIconUrl, sizeDp)
}

@Composable
private fun FdroidRemoteIcon(iconUrl: String, sizeDp: Int) {
    var bitmap by remember(iconUrl) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(iconUrl) {
        bitmap = null
        if (iconUrl.isNotBlank()) {
            bitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    // A local path with a cache-version fragment means the PNG was extracted from an APK.
                    val source = iconUrl.substringBefore('#')
                    val localFile = File(source)
                    if (localFile.isFile) {
                        BitmapFactory.decodeFile(localFile.absolutePath)
                    } else {
                        val connection = java.net.URL(source).openConnection().apply {
                            connectTimeout = 8000
                            readTimeout = 8000
                            setRequestProperty("User-Agent", "AppSpace/1.0")
                        }
                        connection.getInputStream().use { BitmapFactory.decodeStream(it) }
                    }
                } catch (_: Exception) { null }
            }
        }
    }
    Box(
        modifier = Modifier.size(sizeDp.dp).clip(RoundedCornerShape((sizeDp * 0.28f).dp))
            .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.surfaceVariant))),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(bitmap = bitmap!!.asImageBitmap(), contentDescription = "应用图标",
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
        } else {
            Icon(Icons.Outlined.Apps, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size((sizeDp * 0.5f).dp))
        }
    }
}

private fun enqueueFdroidApkDownload(context: Context, app: FdroidApp, release: FdroidRelease): Long? {
    return try {
        val fileName = "${app.packageName}_${release.versionCode}.apk"
        val apkUrl = "https://f-droid.org/repo/$fileName"
        val request = DownloadManager.Request(Uri.parse(apkUrl)).apply {
            setTitle("${app.name} ${release.versionName}")
            setDescription("正在从 F-Droid 官方仓库下载 APK")
            setMimeType("application/vnd.android.package-archive")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(false)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        }
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request).also {
            Toast.makeText(context, "已开始下载 ${app.name}", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "无法开始下载：${e.localizedMessage ?: "未知错误"}", Toast.LENGTH_LONG).show()
        null
    }
}

@Composable
private fun AppSpaceCursorBrush() = SolidColor(
    if (isSystemInDarkTheme()) Color.White else Color.Black
)

private data class RealDownloadRecord(
    val id: Long,
    val title: String,
    val description: String,
    val status: Int,
    val downloadedBytes: Long,
    val totalBytes: Long,
    val reason: Int,
    val uri: Uri?
) {
    val progress: Float get() = if (totalBytes > 0L) (downloadedBytes.toFloat() / totalBytes).coerceIn(0f, 1f) else 0f
    val isComplete: Boolean get() = status == DownloadManager.STATUS_SUCCESSFUL
    val isActive: Boolean get() = status == DownloadManager.STATUS_PENDING || status == DownloadManager.STATUS_RUNNING || status == DownloadManager.STATUS_PAUSED
}

private fun readRealDownloads(context: Context): List<RealDownloadRecord> {
    val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val records = mutableListOf<RealDownloadRecord>()
    val cursor = manager.query(DownloadManager.Query()) ?: return emptyList()
    cursor.use {
        while (it.moveToNext()) {
            fun longColumn(name: String, fallback: Long = 0L): Long {
                val index = it.getColumnIndex(name)
                return if (index >= 0) it.getLong(index) else fallback
            }
            fun stringColumn(name: String): String {
                val index = it.getColumnIndex(name)
                return if (index >= 0) it.getString(index).orEmpty() else ""
            }
            val id = longColumn(DownloadManager.COLUMN_ID)
            val status = longColumn(DownloadManager.COLUMN_STATUS, -1L).toInt()
            val uri = try { manager.getUriForDownloadedFile(id) } catch (_: Exception) { null }
            records += RealDownloadRecord(
                id = id,
                title = stringColumn(DownloadManager.COLUMN_TITLE).ifBlank { "APK 下载任务 #$id" },
                description = stringColumn(DownloadManager.COLUMN_DESCRIPTION),
                status = status,
                downloadedBytes = longColumn(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR),
                totalBytes = longColumn(DownloadManager.COLUMN_TOTAL_SIZE_BYTES, -1L),
                reason = longColumn(DownloadManager.COLUMN_REASON).toInt(),
                uri = uri
            )
        }
    }
    return records.sortedByDescending { it.id }
}

private fun launchApkInstaller(context: Context, uri: Uri): Boolean {
    return try {
        val candidate = findNativeInstallerIntent(context, uri)
            ?: throw IllegalStateException("找不到可用的系统安装器；请确认系统安装器未被停用")
        candidate.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (candidate.action == Intent.ACTION_INSTALL_PACKAGE) {
            candidate.putExtra(Intent.EXTRA_RETURN_RESULT, true)
        }
        if (context is MainActivity) {
            context.launchNativeInstallerForResult(candidate, uri)
        } else {
            context.startActivity(candidate)
        }
        pendingInstalledAppsRefresh = true
        true
    } catch (e: Exception) {
        false
    }
}

private fun findNativeInstallerIntent(context: Context, uri: Uri): Intent? {
    val actions = listOf(Intent.ACTION_INSTALL_PACKAGE, Intent.ACTION_VIEW)
    for (action in actions) {
        val queryIntent = Intent(action).apply {
            setDataAndType(uri, APK_MIME_TYPE)
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val candidates = runCatching {
            @Suppress("DEPRECATION")
            context.packageManager.queryIntentActivities(queryIntent, PackageManager.MATCH_ALL)
        }.getOrDefault(emptyList())
            .mapNotNull { resolve ->
                val info = resolve.activityInfo ?: return@mapNotNull null
                if (info.packageName == context.packageName) return@mapNotNull null
                val packageName = info.packageName.lowercase()
                val activityName = info.name.lowercase()
                val looksLikeInstaller = packageName.contains("packageinstaller") ||
                        packageName.contains("package.installer") ||
                        activityName.contains("packageinstaller") ||
                        activityName.contains("installstart")
                if (looksLikeInstaller) info else null
            }
        val selected = candidates.sortedBy { info ->
            when (info.packageName) {
                "com.google.android.packageinstaller" -> 0
                "com.android.packageinstaller" -> 1
                "com.samsung.android.packageinstaller" -> 2
                else -> 3
            }
        }.firstOrNull()
        if (selected != null) {
            return Intent(action).apply {
                component = ComponentName(selected.packageName, selected.name)
                setDataAndType(uri, APK_MIME_TYPE)
                addCategory(Intent.CATEGORY_DEFAULT)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
    }
    return null
}

/** Remove only APKs that belong to AppSpace's own DownloadManager tasks, never arbitrary external files. */
private fun deleteDownloadedApkAfterSuccess(context: Context, uri: Uri): Boolean {
    val prefs = context.getSharedPreferences("appspace_settings", Context.MODE_PRIVATE)
    if (!prefs.getBoolean("auto_delete_apk_after_install", true)) return false
    val authority = uri.authority.orEmpty().lowercase()
    if (uri.scheme != "content" || !authority.startsWith("downloads")) return false
    val downloadId = uri.lastPathSegment?.toLongOrNull() ?: return false
    val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager ?: return false
    val successfulDownload = runCatching {
        manager.query(DownloadManager.Query().setFilterById(downloadId))?.use { cursor ->
            if (!cursor.moveToFirst()) false else {
                val index = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                index >= 0 && cursor.getInt(index) == DownloadManager.STATUS_SUCCESSFUL
            }
        } ?: false
    }.getOrDefault(false)
    if (!successfulDownload) return false
    return runCatching { manager.remove(downloadId) > 0 }.getOrDefault(false)
}

private fun resolveApkDisplayName(context: Context, uri: Uri): String {
    val queriedName = runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) cursor.getString(index)?.takeIf { it.isNotBlank() } else null
            } else null
        }
    }.getOrNull()
    return queriedName?.takeIf { it.isNotBlank() }
        ?: uri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() }
        ?: "应用安装包.apk"
}

private data class ApkPreflightResult(
    val installable: Boolean,
    val message: String? = null
)

// Keep APK preflight self-contained so MainActivity does not depend on a helper that may
// not exist in older StoreCatalogModels.kt copies. Store IDs such as "huawei:123" are rejected.
private val preflightAndroidPackageNamePattern = Regex("[A-Za-z][A-Za-z0-9_]*(?:\\.[A-Za-z0-9_]+)+")

private fun preflightNormalizedPackageName(value: String?): String = value.orEmpty().trim()
    .takeIf { it.length <= 255 && preflightAndroidPackageNamePattern.matches(it) }
    .orEmpty()

/**
 * Reject common false-positive download successes before launching an installer:
 * HTML/error pages saved as .apk, truncated files, APK bundles/split APKs, a package
 * identity mismatch, or a package requiring a newer Android version.
 * This runs off the main thread. It uses DownloadManager's local file path when available;
 * for arbitrary content URIs it falls back to a lightweight ZIP/manifest check.
 */
private suspend fun preflightDownloadedApk(
    context: Context,
    uri: Uri,
    expectedPackageName: String? = null
): ApkPreflightResult = withContext(kotlinx.coroutines.Dispatchers.IO) {
    val expected = preflightNormalizedPackageName(expectedPackageName)
    val localPath = runCatching {
        if (uri.scheme == "file") uri.path else null
    }.getOrNull() ?: runCatching {
        context.contentResolver.query(uri, arrayOf("_data"), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex("_data")
                if (index >= 0) cursor.getString(index)?.takeIf { it.isNotBlank() } else null
            } else null
        }
    }.getOrNull()
    val localFile = localPath?.let(::File)?.takeIf { it.isFile && it.canRead() }

    if (localFile != null) {
        // Use the long-supported flags overload. The newer PackageInfoFlags overload is
        // unnecessary here and can appear unresolved when IDE SDK indexes are stale.
        @Suppress("DEPRECATION")
        val archiveInfo = runCatching {
            context.packageManager.getPackageArchiveInfo(localFile.absolutePath, 0)
        }.getOrNull() ?: return@withContext ApkPreflightResult(
            false,
            "下载完成，但 Android 无法解析这个安装包。文件可能不完整，或华为商店返回了错误页/非独立 APK；已阻止启动安装。请重新下载，或切换到同一应用的其他来源。"
        )

        val actualPackage = preflightNormalizedPackageName(archiveInfo.packageName)
        if (expected.isNotBlank() && actualPackage.isNotBlank() && actualPackage != expected) {
            return@withContext ApkPreflightResult(
                false,
                "安装包包名与商店详情不一致（详情：$expected；文件：$actualPackage），已阻止安装，避免装错应用。请尝试其他来源。"
            )
        }
        val minSdk = archiveInfo.applicationInfo?.minSdkVersion ?: 1
        if (minSdk > Build.VERSION.SDK_INT) {
            return@withContext ApkPreflightResult(
                false,
                "这个安装包要求 Android $minSdk 或更高版本，当前设备为 Android ${Build.VERSION.SDK_INT}，无法安装。"
            )
        }
        // Read split metadata reflectively so this check stays compatible with different
        // Android SDK stub versions while still detecting standalone split APK files.
        val appSplitName = runCatching {
            val appInfo = archiveInfo.applicationInfo ?: return@runCatching ""
            appInfo.javaClass.getField("splitName").get(appInfo) as? String
        }.getOrNull().orEmpty()
        val packageSplitName = runCatching {
            val rawSplitNames = archiveInfo.javaClass.getField("splitNames").get(archiveInfo)
            (rawSplitNames as? Array<*>)?.firstOrNull()?.toString().orEmpty()
        }.getOrNull().orEmpty()
        val splitName = appSplitName.ifBlank { packageSplitName }
        if (splitName.isNotBlank()) {
            return@withContext ApkPreflightResult(
                false,
                "商店返回的是分包 APK（$splitName），不是可单独安装的完整 APK；已阻止启动安装。请尝试该应用的其他来源。"
            )
        }
        return@withContext ApkPreflightResult(true)
    }

    // Content-provider URIs do not always expose a filesystem path. At least verify that
    // the downloaded content is a ZIP/APK with a root AndroidManifest.xml, not HTML or XAPK/APKS.
    val fallbackCheck = runCatching {
        val raw = context.contentResolver.openInputStream(uri)
            ?: return@runCatching ApkPreflightResult(false, "无法打开下载的安装包文件。")
        BufferedInputStream(raw).use { buffered ->
            buffered.mark(4)
            val magic = ByteArray(4)
            val count = buffered.read(magic)
            buffered.reset()
            val isZip = count == 4 && magic[0] == 0x50.toByte() && magic[1] == 0x4b.toByte() &&
                    ((magic[2] == 0x03.toByte() && magic[3] == 0x04.toByte()) ||
                            (magic[2] == 0x05.toByte() && magic[3] == 0x06.toByte()) ||
                            (magic[2] == 0x07.toByte() && magic[3] == 0x08.toByte()))
            if (!isZip) {
                ApkPreflightResult(false, "下载成功，但文件内容不是 APK（可能是商店错误页或失效链接）；已停止安装。请重新下载或切换来源。")
            } else {
                var manifestFound = false
                var inspected = 0
                ZipInputStream(buffered).use { zip ->
                    while (inspected++ < 48) {
                        val entry = zip.nextEntry ?: break
                        if (!entry.isDirectory && entry.name == "AndroidManifest.xml") {
                            manifestFound = true
                            break
                        }
                        zip.closeEntry()
                    }
                }
                if (manifestFound) ApkPreflightResult(true)
                else ApkPreflightResult(false, "下载内容不是可独立安装的 APK，可能是分包集合、损坏文件或商店错误页；已停止安装。请尝试该应用的其他来源。")
            }
        }
    }.getOrElse {
        ApkPreflightResult(false, "无法验证下载文件是否为有效 APK：${it.message ?: "文件读取失败"}。已停止安装。")
    }
    fallbackCheck
}

/**
 * If the user opted in and Shizuku is authorized, install the downloaded APK using
 * a shell-identity UserService. Otherwise follow the existing system-installer setting.
 * automatic=true is used after a download finishes; manual clicks always try the configured
 * method and fall back to the Android installer if Shizuku fails.
 */
private suspend fun installApkUsingPreferences(
    context: Context,
    uri: Uri,
    automatic: Boolean,
    expectedPackageName: String? = null,
    onStatus: (String) -> Unit
): Boolean {
    onStatus("正在检查安装包完整性…")
    val preflight = preflightDownloadedApk(context, uri, expectedPackageName)
    if (!preflight.installable) {
        val reason = preflight.message ?: "安装包验证失败。"
        onStatus(reason)
        if (automatic) Toast.makeText(context, reason, Toast.LENGTH_LONG).show()
        return false
    }
    val prefs = context.getSharedPreferences("appspace_settings", Context.MODE_PRIVATE)
    val preferShizuku = prefs.getBoolean("use_shizuku_installer", false)
    val autoOpenInstaller = prefs.getBoolean("auto_open_installer", true)

    if (preferShizuku) {
        val state = ShizukuIntegration.getState()
        if (state == ShizukuIntegration.State.GRANTED) {
            onStatus("下载完成，正在通过 Shizuku / ADB 安装…")
            val result = ShizukuInstaller.installFromUri(context.applicationContext, uri)
            if (result.success) {
                pendingInstalledAppsRefresh = true
                val apkRemoved = deleteDownloadedApkAfterSuccess(context, uri)
                onStatus(if (apkRemoved) "安装成功 · Shizuku / ADB · 已清理 APK" else "安装成功 · Shizuku / ADB")
                if (automatic) {
                    Toast.makeText(context, "APK 已通过 Shizuku / ADB 安装", Toast.LENGTH_LONG).show()
                }
                return true
            }
            val detail = result.message.take(180)
            onStatus("Shizuku 安装失败：$detail")
            if (automatic && !autoOpenInstaller) {
                onStatus("Shizuku 安装失败：$detail。系统安装器自动打开已关闭，可在下载中心手动重试。")
                return false
            }
        } else {
            val stateMessage = when (state) {
                ShizukuIntegration.State.NOT_RUNNING -> "Shizuku 服务未启动"
                ShizukuIntegration.State.NEED_PERMISSION -> "AppSpace 尚未获得 Shizuku 授权"
                ShizukuIntegration.State.UNSUPPORTED -> "当前 Shizuku 版本不受支持"
                ShizukuIntegration.State.GRANTED -> "已授权"
            }
            onStatus("无法通过 Shizuku 安装：$stateMessage")
            if (automatic && !autoOpenInstaller) {
                onStatus("下载完成，但 $stateMessage。可在设置中授权，或在下载中心手动打开系统安装器。")
                return false
            }
        }
    }

    val shouldOpenSystemInstaller = !automatic || autoOpenInstaller
    if (shouldOpenSystemInstaller) {
        onStatus("正在打开 Android 系统安装确认界面…")
        val opened = launchApkInstaller(context, uri)
        onStatus(if (opened) "已打开系统安装界面，请确认安装" else "无法打开系统安装器，请到下载中心重试")
        return opened
    }

    onStatus("下载完成，已保存到 Download 文件夹")
    return false
}

@Composable
private fun DownloadsPage() {
    val context = LocalContext.current
    val installScope = rememberCoroutineScope()
    var filter by remember { mutableStateOf("全部") }
    val prefs = remember { context.getSharedPreferences("appspace_settings", Context.MODE_PRIVATE) }
    var showDownloadHistory by remember { mutableStateOf(prefs.getBoolean("show_download_history", true)) }
    var records by remember { mutableStateOf(readRealDownloads(context)) }
    LaunchedEffect(Unit) {
        while (true) {
            records = readRealDownloads(context)
            kotlinx.coroutines.delay(900)
        }
    }
    val visible = records.filter { record ->
        if (!showDownloadHistory && !record.isActive) return@filter false
        when (filter) {
            "下载中" -> record.isActive
            "已完成" -> record.isComplete
            "失败" -> record.status == DownloadManager.STATUS_FAILED
            else -> true
        }
    }
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 110.dp)
    ) {
        Spacer(Modifier.height(22.dp))
        Text("下载中心", color = MaterialTheme.colorScheme.onBackground, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text("您已下载过的应用", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("全部", "下载中", "已完成", "失败").forEach { option ->
                FilterPill(option, filter == option) { filter = option }
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("下载任务", color = MaterialTheme.colorScheme.onBackground, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text("${records.count { it.isComplete }} 项已完成", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Spacer(Modifier.width(10.dp))
            Text("清除历史", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, modifier = Modifier.clickable {
                val oldIds = records.filter { !it.isActive }.map { it.id }.toLongArray()
                if (oldIds.isNotEmpty()) (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).remove(*oldIds)
                records = readRealDownloads(context)
            })
        }
        Spacer(Modifier.height(12.dp))
        if (visible.isEmpty()) {
            EmptyState("暂无下载任务", "从发现页搜索应用并下载 APK，任务会自动显示在这里。", Icons.Outlined.CloudDownload)
        } else visible.forEach { record ->
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant).padding(15.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppIconTile(Icons.Outlined.Apps)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(record.title, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            when (record.status) {
                                DownloadManager.STATUS_PENDING -> "等待下载"
                                DownloadManager.STATUS_RUNNING -> "正在下载 · ${(record.progress * 100).toInt()}%"
                                DownloadManager.STATUS_PAUSED -> "下载暂停"
                                DownloadManager.STATUS_SUCCESSFUL -> "下载完成${if (record.totalBytes > 0) " · ${android.text.format.Formatter.formatFileSize(context, record.totalBytes)}" else ""}"
                                DownloadManager.STATUS_FAILED -> "下载失败（错误码 ${record.reason}）"
                                else -> record.description.ifBlank { "下载任务" }
                            }, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp
                        )
                    }
                }
                if (record.isActive) {
                    Spacer(Modifier.height(12.dp))
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { if (record.totalBytes > 0L) record.progress else 0f },
                        modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
                if (record.isComplete && record.uri != null) {
                    Spacer(Modifier.height(10.dp))
                    Text("安装 APK", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable {
                            installScope.launch {
                                installApkUsingPreferences(context, record.uri, automatic = false) { message ->
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                }
                            }
                        }.padding(vertical = 5.dp))
                } else if (record.status == DownloadManager.STATUS_FAILED) {
                    Spacer(Modifier.height(6.dp))
                    Text("可返回应用详情重新下载", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun SettingsPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("appspace_settings", Context.MODE_PRIVATE) }
    var rememberSearchHistory by remember { mutableStateOf(prefs.getBoolean("remember_search_history", true)) }
    var showDownloadHistory by remember { mutableStateOf(prefs.getBoolean("show_download_history", true)) }
    var autoOpenInstaller by remember { mutableStateOf(prefs.getBoolean("auto_open_installer", true)) }
    var useMonetColors by remember { mutableStateOf(prefs.getBoolean("use_monet_colors", false)) }
    var autoDeleteApk by remember { mutableStateOf(prefs.getBoolean("auto_delete_apk_after_install", true)) }
    var defaultInstallerLocked by remember { mutableStateOf(prefs.getBoolean("default_installer_locked", false)) }
    var useShizukuInstaller by remember { mutableStateOf(prefs.getBoolean("use_shizuku_installer", false)) }
    var shizukuState by remember { mutableStateOf(ShizukuIntegration.getState()) }
    var selectedSettingsCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var updateDialogMessage by remember { mutableStateOf("点击检查更新后，将查询 AppSpace 的 GitHub Releases。") }
    var updateReleaseUrl by remember { mutableStateOf<String?>(null) }
    val settingsScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    val appVersionName = remember(context) {
        runCatching {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "未知"
        }.getOrDefault("未知")
    }

    DisposableEffect(lifecycleOwner) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                shizukuState = ShizukuIntegration.getState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        val permissionListener = ShizukuIntegration.createPermissionResultListener { requestCode, _ ->
            if (requestCode == ShizukuIntegration.REQUEST_CODE) {
                shizukuState = ShizukuIntegration.getState()
            }
        }
        Shizuku.addRequestPermissionResultListener(permissionListener)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            Shizuku.removeRequestPermissionResultListener(permissionListener)
        }
    }

    val shizukuStateLabel = when (shizukuState) {
        ShizukuIntegration.State.NOT_RUNNING -> "未运行：请先启动 Shizuku 服务"
        ShizukuIntegration.State.NEED_PERMISSION -> "服务已运行，但 AppSpace 尚未获授权"
        ShizukuIntegration.State.GRANTED -> "已连接并获得授权，可执行 ADB 安装"
        ShizukuIntegration.State.UNSUPPORTED -> "Shizuku 版本过旧，请升级到 v11 或更高版本"
    }

    BackHandler(enabled = selectedSettingsCategory != null) {
        selectedSettingsCategory = null
    }

    AnimatedContent(
        targetState = selectedSettingsCategory,
        label = "设置分类页面切换",
        transitionSpec = {
            if (targetState != null) {
                (slideInHorizontally(tween(260)) { it / 7 } + fadeIn(tween(220))) togetherWith
                        (slideOutHorizontally(tween(190)) { -it / 12 } + fadeOut(tween(170)))
            } else {
                (slideInHorizontally(tween(260)) { -it / 7 } + fadeIn(tween(220))) togetherWith
                        (slideOutHorizontally(tween(190)) { it / 12 } + fadeOut(tween(170)))
            }
        }
    ) { category ->
        if (category == null) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 30.dp)
            ) {
                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.ArrowBack,
                        contentDescription = "返回",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(26.dp).clickable { onBack() }
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "设置",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "按类别管理 AppSpace",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(22.dp))
                SettingsHomeCategoryButton(
                    title = "外观与颜色",
                    description = "主题颜色与系统壁纸动态取色",
                    icon = Icons.Outlined.Palette,
                    onClick = { selectedSettingsCategory = "外观与颜色" }
                )
                Spacer(Modifier.height(12.dp))
                SettingsHomeCategoryButton(
                    title = "搜索与隐私",
                    description = "搜索历史与本地记录管理",
                    icon = Icons.Outlined.History,
                    onClick = { selectedSettingsCategory = "搜索与隐私" }
                )
                Spacer(Modifier.height(12.dp))
                SettingsHomeCategoryButton(
                    title = "下载管理",
                    description = "下载历史显示与记录清理",
                    icon = Icons.Outlined.Download,
                    onClick = { selectedSettingsCategory = "下载管理" }
                )
                Spacer(Modifier.height(12.dp))
                SettingsHomeCategoryButton(
                    title = "安装与权限",
                    description = "安装方式、Shizuku / ADB 与默认 APK 处理器",
                    icon = Icons.Outlined.Security,
                    onClick = { selectedSettingsCategory = "安装与权限" }
                )
                Spacer(Modifier.height(12.dp))
                SettingsHomeCategoryButton(
                    title = "关于 AppSpace",
                    description = "应用版本、检查更新、说明与隐私政策",
                    icon = Icons.Outlined.Info,
                    onClick = { selectedSettingsCategory = "关于 AppSpace" }
                )
                Spacer(Modifier.height(20.dp))
            }
        } else {
            SettingsSubpageScaffold(
                title = category,
                onBack = { selectedSettingsCategory = null }
            ) {
                when (category) {
                    "外观与颜色" -> SettingsSection(title = "外观与颜色", icon = Icons.Outlined.Palette) {
                        SettingsToggleRow(
                            "跟随系统壁纸取色（Monet）",
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                                "开启后使用系统壁纸生成的 Material 3 动态配色；关闭时使用 AppSpace 蓝色主题"
                            else
                                "需要 Android 12 或更高版本；当前设备将使用 AppSpace 蓝色主题",
                            useMonetColors
                        ) { enabled ->
                            if (enabled && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                                Toast.makeText(context, "Monet 动态取色需要 Android 12 或更高版本", Toast.LENGTH_LONG).show()
                            } else {
                                useMonetColors = enabled
                                prefs.edit().putBoolean("use_monet_colors", enabled).apply()
                                monetColorsEnabled.value = enabled
                            }
                        }
                    }

                    "搜索与隐私" -> SettingsSection(title = "搜索与隐私", icon = Icons.Outlined.History) {
                        SettingsToggleRow(
                            "保存搜索历史",
                            "关闭后不再保存新的搜索记录",
                            rememberSearchHistory
                        ) {
                            rememberSearchHistory = it
                            prefs.edit().putBoolean("remember_search_history", it).apply()
                            if (!it) prefs.edit().remove("search_history").apply()
                        }
                        SettingsSectionDivider()
                        SettingsActionRow("删除搜索历史", "清除 AppSpace 保存的所有搜索词") {
                            prefs.edit().remove("search_history").apply()
                            Toast.makeText(context, "搜索历史已清除", Toast.LENGTH_SHORT).show()
                        }
                    }

                    "下载管理" -> SettingsSection(title = "下载管理", icon = Icons.Outlined.Download) {
                        SettingsToggleRow(
                            "显示下载历史",
                            "关闭后下载中心只显示进行中的任务；不会自动删除系统下载记录",
                            showDownloadHistory
                        ) {
                            showDownloadHistory = it
                            prefs.edit().putBoolean("show_download_history", it).apply()
                        }
                        SettingsSectionDivider()
                        SettingsActionRow("删除已完成/失败的下载记录", "从 Android 下载管理器移除非活动任务") {
                            val records = readRealDownloads(context).filter { !it.isActive }
                            if (records.isNotEmpty()) {
                                val ids = records.map { it.id }.toLongArray()
                                (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).remove(*ids)
                            }
                            Toast.makeText(context, "已清理非活动下载记录", Toast.LENGTH_SHORT).show()
                        }
                    }

                    "安装与权限" -> {
                        SettingsSection(title = "安装选项", icon = Icons.Outlined.Security) {
                            SettingsToggleRow(
                                "下载完成后打开安装器",
                                "打开系统安装确认页；关闭后需在下载中心手动点击安装",
                                autoOpenInstaller
                            ) {
                                autoOpenInstaller = it
                                prefs.edit().putBoolean("auto_open_installer", it).apply()
                            }
                            SettingsSectionDivider()
                            SettingsToggleRow(
                                "安装成功后自动删除 APK",
                                "只清理 AppSpace 通过 Android 下载管理器下载的 APK；安装失败或取消时保留文件",
                                autoDeleteApk
                            ) {
                                autoDeleteApk = it
                                prefs.edit().putBoolean("auto_delete_apk_after_install", it).apply()
                            }
                            SettingsSectionDivider()
                            SettingsToggleRow(
                                "优先使用 Shizuku / ADB 安装",
                                "授权后尝试通过 shell 执行 pm install；失败时按上面的系统安装器选项决定是否自动回退",
                                useShizukuInstaller
                            ) { enabled ->
                                if (!enabled) {
                                    useShizukuInstaller = false
                                    prefs.edit().putBoolean("use_shizuku_installer", false).apply()
                                } else {
                                    when (ShizukuIntegration.getState()) {
                                        ShizukuIntegration.State.UNSUPPORTED -> {
                                            Toast.makeText(context, "当前 Shizuku 版本过旧，请先升级", Toast.LENGTH_LONG).show()
                                            shizukuState = ShizukuIntegration.getState()
                                        }
                                        else -> {
                                            useShizukuInstaller = true
                                            prefs.edit().putBoolean("use_shizuku_installer", true).apply()
                                            when (ShizukuIntegration.getState()) {
                                                ShizukuIntegration.State.NEED_PERMISSION -> runCatching {
                                                    ShizukuIntegration.requestPermission()
                                                }.onFailure {
                                                    Toast.makeText(context, it.message ?: "无法请求 Shizuku 授权", Toast.LENGTH_LONG).show()
                                                }
                                                ShizukuIntegration.State.NOT_RUNNING -> {
                                                    val launchIntent = context.packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
                                                    if (launchIntent != null) {
                                                        context.startActivity(launchIntent)
                                                        Toast.makeText(context, "请在 Shizuku 中启动服务，然后返回 AppSpace", Toast.LENGTH_LONG).show()
                                                    } else {
                                                        runCatching {
                                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app/")))
                                                        }
                                                        Toast.makeText(context, "请先安装并启动 Shizuku", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                                else -> Unit
                                            }
                                            shizukuState = ShizukuIntegration.getState()
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        SettingsSection(title = "Shizuku / ADB 状态", icon = Icons.Outlined.Security) {
                            Text(
                                "当前状态：$shizukuStateLabel",
                                color = if (shizukuState == ShizukuIntegration.State.GRANTED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                            Spacer(Modifier.height(12.dp))
                            SettingsActionRow(
                                if (defaultInstallerLocked) "解除 AppSpace 默认 APK 安装器" else "设为默认 APK 安装器",
                                "使用 Shizuku 尝试接管 APK 的打开/安装请求。部分厂商系统可能限制；系统可能保留或重新询问默认应用选择。"
                            ) {
                                if (ShizukuIntegration.getState() != ShizukuIntegration.State.GRANTED) {
                                    Toast.makeText(context, "请先启动 Shizuku 并授予 AppSpace 权限", Toast.LENGTH_LONG).show()
                                } else {
                                    settingsScope.launch {
                                        val targetLock = !defaultInstallerLocked
                                        val result = ShizukuInstaller.setDefaultInstaller(context, targetLock)
                                        if (result.success) {
                                            defaultInstallerLocked = targetLock
                                            prefs.edit().putBoolean("default_installer_locked", targetLock).apply()
                                        }
                                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            val shizukuActionLabel = when (shizukuState) {
                                ShizukuIntegration.State.NOT_RUNNING -> "打开 Shizuku / 启动服务"
                                ShizukuIntegration.State.NEED_PERMISSION -> "授予 AppSpace 权限"
                                ShizukuIntegration.State.GRANTED -> "刷新授权状态"
                                ShizukuIntegration.State.UNSUPPORTED -> "查看 Shizuku 官方说明"
                            }
                            Text(
                                shizukuActionLabel,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable {
                                    shizukuState = ShizukuIntegration.getState()
                                    when (shizukuState) {
                                        ShizukuIntegration.State.NOT_RUNNING -> {
                                            val launchIntent = context.packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
                                            if (launchIntent != null) {
                                                context.startActivity(launchIntent)
                                                Toast.makeText(context, "请在 Shizuku 中启动服务，然后返回 AppSpace", Toast.LENGTH_LONG).show()
                                            } else {
                                                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app/"))) }
                                            }
                                        }
                                        ShizukuIntegration.State.NEED_PERMISSION -> runCatching {
                                            ShizukuIntegration.requestPermission()
                                        }.onFailure {
                                            Toast.makeText(context, it.message ?: "无法请求 Shizuku 授权", Toast.LENGTH_LONG).show()
                                        }
                                        ShizukuIntegration.State.GRANTED -> Toast.makeText(context, "Shizuku 已授权", Toast.LENGTH_SHORT).show()
                                        ShizukuIntegration.State.UNSUPPORTED -> runCatching {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app/")))
                                        }
                                    }
                                }.padding(vertical = 5.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "安装时 AppSpace 会把下载的 APK 通过 Binder 传给 Shizuku shell 服务，再执行 pm install -r。安装成功后应用管理列表会在下次刷新时更新。",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Shizuku 官方说明",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp,
                                modifier = Modifier.clickable {
                                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app/"))) }
                                }
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "静默安装只会在 Shizuku / ADB 权限已启用且系统允许时尝试。未授权或不支持时，应回退到 Android 系统安装确认界面。",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }

                    "关于 AppSpace" -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(Modifier.height(6.dp))
                            // Use Android ImageView for the launcher icon because ic_launcher
                            // may be an AdaptiveIconDrawable; Compose painterResource only supports
                            // raster/vector painters and can crash when loading adaptive icons.
                            AndroidView(
                                factory = { androidContext ->
                                    android.widget.ImageView(androidContext).apply {
                                        setImageResource(R.mipmap.ic_launcher)
                                        scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                                        contentDescription = "AppSpace 图标"
                                    }
                                },
                                modifier = Modifier.size(92.dp).clip(RoundedCornerShape(24.dp))
                            )
                            Spacer(Modifier.height(12.dp))
                            Text("AppSpace", color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(5.dp))
                            Text("版本 $appVersionName", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            Spacer(Modifier.height(16.dp))
                            androidx.compose.material3.Button(
                                onClick = {
                                    showUpdateDialog = true
                                    isCheckingUpdate = true
                                    updateDialogMessage = "正在检查 GitHub Releases…"
                                    updateReleaseUrl = null
                                    settingsScope.launch {
                                        val result = checkAppSpaceLatestRelease(appVersionName)
                                        updateDialogMessage = result.message
                                        updateReleaseUrl = result.releaseUrl
                                        isCheckingUpdate = false
                                    }
                                },
                                enabled = !isCheckingUpdate,
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(if (isCheckingUpdate) "检查中…" else "检查更新")
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        SettingsSection(title = "说明", icon = Icons.Outlined.Info) {
                            Text(
                                "AppSpace 将多个应用来源整合到同一套发现、搜索、详情与下载体验中。来源提供的数据不同，因此版本、截图或许可证等信息可能因应用而异。",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        SettingsSection(title = "隐私政策", icon = Icons.Outlined.Security) {
                            Text(
                                "应用搜索会连接到所选的公开应用数据源（例如 F-Droid 和 GitHub）获取应用信息。开启搜索历史时，搜索词保存在本机；下载任务由 Android 下载管理器管理。AppSpace 不会因为展示应用列表而要求你创建账号。",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = { Text(if (isCheckingUpdate) "正在检查更新" else "检查更新") },
            text = { Text("当前版本：$appVersionName\n\n$updateDialogMessage") },
            confirmButton = {
                Row {
                    updateReleaseUrl?.let { releaseUrl ->
                        TextButton(onClick = {
                            showUpdateDialog = false
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(releaseUrl)))
                            }.onFailure {
                                Toast.makeText(context, "无法打开 GitHub Release 页面", Toast.LENGTH_SHORT).show()
                            }
                        }) { Text("查看 Release") }
                    }
                    TextButton(onClick = { showUpdateDialog = false }) { Text("关闭") }
                }
            }
        )
    }
}

@Composable
private fun SettingsHomeCategoryButton(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(42.dp).clip(RoundedCornerShape(13.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 17.sp)
        }
        Spacer(Modifier.width(8.dp))
        Icon(Icons.Outlined.KeyboardArrowRight, contentDescription = "进入$title", tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingsSubpageScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 30.dp)
    ) {
        Spacer(Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.ArrowBack,
                contentDescription = "返回设置",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(26.dp).clickable(onClick = onBack)
            )
            Spacer(Modifier.width(12.dp))
            Text(title, color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(22.dp))
        content()
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier = Modifier.fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f), shape)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(34.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.width(11.dp))
            Text(title, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(7.dp))
        content()
    }
}

@Composable
private fun SettingsSectionDivider() {
    Box(
        Modifier.fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f))
    )
}

@Composable
private fun SettingsGroupTitle(text: String) {
    Text(text, color = MaterialTheme.colorScheme.onBackground, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 10.dp))
}

@Composable
private fun SettingsToggleRow(title: String, description: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(3.dp))
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun SettingsActionRow(title: String, description: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable { onClick() }.padding(vertical = 12.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(3.dp))
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
        Icon(Icons.Outlined.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
    }
}

private data class InstalledLauncherApp(
    val label: String,
    val packageName: String,
    val versionName: String,
    val icon: android.graphics.drawable.Drawable,
    val launchIntent: Intent?
)

@Suppress("DEPRECATION")
private fun loadInstalledLauncherApps(context: Context): List<InstalledLauncherApp> {
    val pm = context.packageManager
    val launcherIntent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
    val activities = if (android.os.Build.VERSION.SDK_INT >= 33) {
        pm.queryIntentActivities(launcherIntent, PackageManager.ResolveInfoFlags.of(0L))
    } else {
        pm.queryIntentActivities(launcherIntent, 0)
    }
    return activities.mapNotNull { resolve ->
        val activityInfo = resolve.activityInfo ?: return@mapNotNull null
        val packageName = activityInfo.packageName
        if (packageName == context.packageName) return@mapNotNull null
        try {
            val appInfo = if (android.os.Build.VERSION.SDK_INT >= 33) {
                pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0L))
            } else pm.getApplicationInfo(packageName, 0)
            val packageInfo = if (android.os.Build.VERSION.SDK_INT >= 33) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0L))
            } else pm.getPackageInfo(packageName, 0)
            InstalledLauncherApp(
                label = pm.getApplicationLabel(appInfo).toString(),
                packageName = packageName,
                versionName = packageInfo.versionName ?: "未知版本",
                icon = pm.getApplicationIcon(appInfo),
                launchIntent = pm.getLaunchIntentForPackage(packageName)
            )
        } catch (_: Exception) { null }
    }.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
}

@Composable
private fun AppManagementPage() {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var installedApps by remember { mutableStateOf<List<InstalledLauncherApp>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    val refreshApps: () -> Unit = {
        if (!isLoading) {
            scope.launch {
                isLoading = true
                installedApps = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    loadInstalledLauncherApps(context.applicationContext)
                }
                isLoading = false
                pendingInstalledAppsRefresh = false
            }
        }
    }

    // 页面首次显示时加载；如果刚从本应用启动的系统安装器返回，则额外刷新一次。
    LaunchedEffect(Unit) {
        installedApps = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            loadInstalledLauncherApps(context.applicationContext)
        }
        isLoading = false
        pendingInstalledAppsRefresh = false
    }

    val currentRefreshApps by rememberUpdatedState(refreshApps)
    val currentIsLoading by rememberUpdatedState(isLoading)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && pendingInstalledAppsRefresh && !currentIsLoading) {
                currentRefreshApps()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val visible = remember(installedApps, query) {
        if (query.isBlank()) installedApps
        else installedApps.filter {
            it.label.contains(query, ignoreCase = true) ||
                    it.packageName.contains(query, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 110.dp)
    ) {
        item {
            Spacer(Modifier.height(22.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("应用管理", color = MaterialTheme.colorScheme.onBackground, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("已安装的应用", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }
                androidx.compose.material3.IconButton(
                    onClick = { refreshApps() },
                    enabled = !isLoading
                ) {
                    Icon(Icons.Outlined.Refresh, contentDescription = "刷新应用列表",
                        tint = if (isLoading) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Search, contentDescription = "搜索", tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(10.dp))
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.weight(1f).height(24.dp),
                    singleLine = true,
                    cursorBrush = AppSpaceCursorBrush(),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    ),
                    decorationBox = { inner ->
                        Box(
                            modifier = Modifier.fillMaxWidth().height(24.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (query.isEmpty()) Text("搜索已安装应用或包名", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, lineHeight = 20.sp)
                            inner()
                        }
                    }
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(if (isLoading) "…" else "${installedApps.size}", "已安装应用", Modifier.weight(1f))
                StatCard(if (isLoading) "…" else "${visible.size}", "筛选结果", Modifier.weight(1f))
            }
            Spacer(Modifier.height(20.dp))
            Text("已安装应用", color = MaterialTheme.colorScheme.onBackground, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
        }

        if (isLoading) {
            item { Text("正在读取已安装应用…", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 20.dp)) }
        } else if (visible.isEmpty()) {
            item { EmptyState("没有找到应用", "请更换搜索词；这里只列出系统允许 AppSpace 查询的桌面应用。",
                Icons.Outlined.Smartphone) }
        } else {
            items(visible, key = { it.packageName }) { app ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        .clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { app.launchIntent?.let { context.startActivity(it) } }.padding(13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
                        AndroidView(factory = { androidContext ->
                            android.widget.ImageView(androidContext).apply {
                                setImageDrawable(app.icon)
                                scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                            }
                        }, modifier = Modifier.fillMaxSize())
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(app.label, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text("${app.versionName} · ${app.packageName}", color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp, maxLines = 2)
                    }
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "打开应用", tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp))
                }
            }
        }

        item {
            Spacer(Modifier.height(10.dp))
            Text("点击应用可打开；长按系统应用管理等操作可在 Android 系统设置中完成。",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
    }
}

@Composable
private fun FilterPill(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.clip(CircleShape)
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.20f) else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick).padding(horizontal = 15.dp, vertical = 10.dp)
    ) {
        Text(text, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
private fun AppIconTile(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(15.dp))
            .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.secondaryContainer))),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(25.dp))
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(13.dp)) {
        Text(value, color = MaterialTheme.colorScheme.onBackground, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
    }
}

@Composable
private fun EmptyState(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(38.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(5.dp))
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    }
}


// Shizuku 授权状态和安装调用合并在本文件中，避免额外的 Integration / Installer 文件。
/**
 * AppSpace 的 Shizuku API 基础接入层。
 * 负责检测 Shizuku Binder、检查/申请授权；实际 APK 安装服务在下一步实现。
 */
private object ShizukuIntegration {
    const val REQUEST_CODE = 7301

    enum class State {
        NOT_RUNNING,
        NEED_PERMISSION,
        GRANTED,
        UNSUPPORTED
    }

    fun getState(): State {
        return try {
            if (!Shizuku.pingBinder()) {
                State.NOT_RUNNING
            } else if (Shizuku.isPreV11()) {
                State.UNSUPPORTED
            } else if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                State.GRANTED
            } else {
                State.NEED_PERMISSION
            }
        } catch (_: Throwable) {
            State.NOT_RUNNING
        }
    }

    fun requestPermission() {
        if (!Shizuku.pingBinder()) {
            throw IllegalStateException("Shizuku 尚未启动，请先启动 Shizuku 服务")
        }
        if (Shizuku.isPreV11()) {
            throw IllegalStateException("当前 Shizuku 版本过旧，请升级到 v11 或更高版本")
        }
        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            Shizuku.requestPermission(REQUEST_CODE)
        }
    }

    fun createPermissionResultListener(
        onResult: (requestCode: Int, grantResult: Int) -> Unit
    ): Shizuku.OnRequestPermissionResultListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            onResult(requestCode, grantResult)
        }

    fun hasPermission(): Boolean = getState() == State.GRANTED
}

/** Connects to a Shizuku UserService and installs APKs using the ADB shell identity. */
private object ShizukuInstaller {
    data class InstallResult(val success: Boolean, val message: String)

    suspend fun setDefaultInstaller(context: Context, lock: Boolean): InstallResult {
        val state = ShizukuIntegration.getState()
        if (state != ShizukuIntegration.State.GRANTED) {
            return InstallResult(false, when (state) {
                ShizukuIntegration.State.NOT_RUNNING -> "Shizuku 服务未启动"
                ShizukuIntegration.State.NEED_PERMISSION -> "AppSpace 尚未获得 Shizuku 授权"
                ShizukuIntegration.State.UNSUPPORTED -> "Shizuku 版本过旧"
                ShizukuIntegration.State.GRANTED -> "已授权"
            })
        }
        val appContext = context.applicationContext
        val args = Shizuku.UserServiceArgs(ComponentName(appContext, ShizukuInstallService::class.java))
            .tag("appspace-apk-installer").version(1).processNameSuffix("apk_installer").daemon(false)
        var connection: ServiceConnection? = null
        try {
            val remoteBinder = withContext(Dispatchers.Main.immediate) {
                suspendCancellableCoroutine<IBinder> { continuation ->
                    val serviceConnection = object : ServiceConnection {
                        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                            if (!continuation.isActive) return
                            if (binder == null) continuation.resumeWith(kotlin.Result.failure(IllegalStateException("Shizuku 服务返回空 Binder")))
                            else continuation.resumeWith(kotlin.Result.success(binder))
                        }
                        override fun onServiceDisconnected(name: ComponentName?) {
                            if (continuation.isActive) continuation.resumeWith(kotlin.Result.failure(IllegalStateException("Shizuku 安装服务连接断开")))
                        }
                    }
                    connection = serviceConnection
                    try { Shizuku.bindUserService(args, serviceConnection) }
                    catch (t: Throwable) { if (continuation.isActive) continuation.resumeWith(kotlin.Result.failure(t)) }
                    continuation.invokeOnCancellation { runCatching { Shizuku.unbindUserService(args, serviceConnection, true) } }
                }
            }
            val output = withContext(Dispatchers.IO) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(ShizukuInstallService.DESCRIPTOR)
                    data.writeInt(if (lock) 1 else 0)
                    val handled = remoteBinder.transact(ShizukuInstallService.TRANSACTION_SET_DEFAULT_INSTALLER, data, reply, 0)
                    if (!handled) throw IllegalStateException("Shizuku 服务不支持默认安装器设置")
                    reply.readException()
                    reply.readString() ?: throw IllegalStateException("安装服务没有返回结果")
                } finally { data.recycle(); reply.recycle() }
            }
            val split = output.indexOf('|')
            val status = if (split >= 0) output.substring(0, split) else "ERROR"
            val message = if (split >= 0) output.substring(split + 1) else output
            return InstallResult(status == "SUCCESS", message.ifBlank { "默认安装器操作失败" })
        } catch (t: Throwable) {
            return InstallResult(false, t.message ?: t.javaClass.simpleName)
        } finally {
            connection?.let { conn ->
                withContext(Dispatchers.Main.immediate) { runCatching { Shizuku.unbindUserService(args, conn, true) } }
            }
        }
    }

    suspend fun installFromUri(context: Context, uri: Uri): InstallResult {
        val state = ShizukuIntegration.getState()
        if (state != ShizukuIntegration.State.GRANTED) {
            return InstallResult(false, when (state) {
                ShizukuIntegration.State.NOT_RUNNING -> "Shizuku 服务未启动"
                ShizukuIntegration.State.NEED_PERMISSION -> "AppSpace 尚未获得 Shizuku 授权"
                ShizukuIntegration.State.UNSUPPORTED -> "Shizuku 版本过旧"
                ShizukuIntegration.State.GRANTED -> "已授权"
            })
        }

        val appContext = context.applicationContext
        val args = Shizuku.UserServiceArgs(
            ComponentName(appContext, ShizukuInstallService::class.java)
        )
            .tag("appspace-apk-installer")
            .version(1)
            .processNameSuffix("apk_installer")
            .daemon(false)

        var connection: ServiceConnection? = null
        try {
            val remoteBinder = withContext(Dispatchers.Main.immediate) {
                suspendCancellableCoroutine<IBinder> { continuation ->
                    val serviceConnection = object : ServiceConnection {
                        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                            if (binder == null) {
                                if (continuation.isActive) continuation.resumeWith(kotlin.Result.failure(IllegalStateException("Shizuku 服务返回空 Binder")))
                                return
                            }
                            if (continuation.isActive) {
                                continuation.resumeWith(kotlin.Result.success(binder))
                            }
                        }

                        override fun onServiceDisconnected(name: ComponentName?) {
                            if (continuation.isActive) {
                                continuation.resumeWith(kotlin.Result.failure(IllegalStateException("Shizuku 安装服务连接断开")))
                            }
                        }
                    }
                    connection = serviceConnection
                    try {
                        Shizuku.bindUserService(args, serviceConnection)
                    } catch (t: Throwable) {
                        if (continuation.isActive) continuation.resumeWith(kotlin.Result.failure(t))
                    }
                    continuation.invokeOnCancellation {
                        runCatching { Shizuku.unbindUserService(args, serviceConnection, true) }
                    }
                }
            }

            val output = withContext(Dispatchers.IO) {
                val fd = appContext.contentResolver.openFileDescriptor(uri, "r")
                    ?: throw IllegalStateException("无法读取下载的 APK 文件")
                fd.use { apkFd ->
                    val data = Parcel.obtain()
                    val reply = Parcel.obtain()
                    try {
                        data.writeInterfaceToken(ShizukuInstallService.DESCRIPTOR)
                        data.writeTypedObject(apkFd, 0)
                        val handled = remoteBinder.transact(
                            ShizukuInstallService.TRANSACTION_INSTALL_APK,
                            data,
                            reply,
                            0
                        )
                        if (!handled) throw IllegalStateException("Shizuku 安装服务不支持安装请求")
                        reply.readException()
                        reply.readString() ?: throw IllegalStateException("安装服务没有返回结果")
                    } finally {
                        data.recycle()
                        reply.recycle()
                    }
                }
            }
            val delimiter = output.indexOf('|')
            val status = if (delimiter >= 0) output.substring(0, delimiter) else "ERROR"
            val message = if (delimiter >= 0) output.substring(delimiter + 1) else output
            return InstallResult(status == "SUCCESS", message.ifBlank { if (status == "SUCCESS") "安装成功" else "安装失败" })
        } catch (t: Throwable) {
            return InstallResult(false, t.message ?: t.javaClass.simpleName)
        } finally {
            val conn = connection
            if (conn != null) {
                withContext(Dispatchers.Main.immediate) {
                    runCatching { Shizuku.unbindUserService(args, conn, true) }
                }
            }
        }
    }
}

private data class AppSpaceReleaseCheckResult(
    val message: String,
    val releaseUrl: String? = null
)

/** Queries this project's public latest GitHub Release. Handles the empty-repository/no-release state. */
private suspend fun checkAppSpaceLatestRelease(currentVersion: String): AppSpaceReleaseCheckResult =
    withContext(Dispatchers.IO) {
        val releasesPage = "https://github.com/SteveYuanlong9321/AppSpace/releases"
        var connection: java.net.HttpURLConnection? = null
        try {
            val conn = (java.net.URL("https://api.github.com/repos/SteveYuanlong9321/AppSpace/releases/latest")
                .openConnection() as java.net.HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
                setRequestProperty("User-Agent", "AppSpace-Android")
            }
            connection = conn

            when (val status = conn.responseCode) {
                200 -> {
                    val jsonText = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    val json = org.json.JSONObject(jsonText)
                    val tagName = json.optString("tag_name").ifBlank { json.optString("name") }.ifBlank { "未知版本" }
                    val releaseUrl = json.optString("html_url").ifBlank { releasesPage }
                    val latestVersion = tagName.removePrefix("v").removePrefix("V")
                    val comparison = compareAppSpaceVersions(latestVersion, currentVersion)
                    val assets = json.optJSONArray("assets")
                    val hasApk = assets != null && (0 until assets.length()).any { index ->
                        assets.optJSONObject(index)?.optString("name")?.endsWith(".apk", ignoreCase = true) == true
                    }
                    val message = when {
                        comparison == null -> "已获取最新 Release：$tagName\n无法可靠识别版本号的先后关系，请打开发布页面确认。"
                        comparison > 0 -> buildString {
                            append("发现新版本：$tagName")
                            append("\n当前版本：$currentVersion")
                            if (!hasApk) append("\n\n该 Release 暂未附带 APK 文件，请在发布页面查看资产。")
                            else append("\n\n此 Release 包含 APK 安装包。")
                        }
                        else -> "当前已是最新版本。\nGitHub 最新 Release：$tagName"
                    }
                    AppSpaceReleaseCheckResult(message, releaseUrl)
                }
                404 -> AppSpaceReleaseCheckResult(
                    "仓库已经配置，但目前还没有发布正式 Release。\n创建并发布第一个 AppSpace Release 后，这里就能读取版本信息。",
                    releasesPage
                )
                403, 429 -> AppSpaceReleaseCheckResult(
                    "GitHub 暂时限制了 API 请求，请稍后再试，也可以直接打开 Releases 页面查看。",
                    releasesPage
                )
                else -> AppSpaceReleaseCheckResult(
                    "GitHub 返回 HTTP $status，暂时无法获取版本信息。请稍后重试或打开 Releases 页面。",
                    releasesPage
                )
            }
        } catch (e: Exception) {
            AppSpaceReleaseCheckResult(
                "检查更新失败：${e.message ?: "网络连接不可用"}\n请检查网络后重试。",
                releasesPage
            )
        } finally {
            connection?.disconnect()
        }
    }

private fun compareAppSpaceVersions(latest: String, current: String): Int? {
    fun parts(value: String): List<Int> = Regex("\\d+")
        .findAll(value)
        .mapNotNull { it.value.toIntOrNull() }
        .toList()

    val latestParts = parts(latest)
    val currentParts = parts(current)
    if (latestParts.isEmpty() || currentParts.isEmpty()) return null
    for (index in 0 until maxOf(latestParts.size, currentParts.size)) {
        val left = latestParts.getOrElse(index) { 0 }
        val right = currentParts.getOrElse(index) { 0 }
        if (left != right) return left.compareTo(right)
    }
    return 0
}