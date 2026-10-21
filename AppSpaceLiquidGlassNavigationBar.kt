package com.example.appspace

import android.content.Context
import android.os.SystemClock
import android.view.MotionEvent
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.qmdeve.liquidglass.widget.LiquidGlassView
import kotlin.math.roundToInt

private val NavPrimaryText = Color(0xFFF5F5F8)
private val NavSecondaryText = Color(0xFFB7B7C2)
internal val AppSpaceNavigationAccentColor = mutableStateOf(Color(0xFF8AB4F8))

/**
 * AppSpace 双层玻璃导航栏。
 * 下层是低折射、低色散的磨砂模糊底板；上层只有选中胶囊使用明显液态折射。
 * 支持点击切换，也支持直接拖动胶囊左右切换页面。
 */
class AppSpaceLiquidGlassNavigationBar(
    context: Context,
    contentContainer: ViewGroup,
    private val tabs: List<Triple<String, ImageVector, String>>,
    private val selectedTabState: MutableState<String>,
    private val onTabSelected: (String) -> Unit
) : FrameLayout(context) {

    private val capsuleView: LiquidGlassView
    private val capsuleHost = FrameLayout(context)
    private val capsuleDensity = context.resources.displayMetrics.density
    private val sideInsetPx = (4f * capsuleDensity).roundToInt()
    private var isDraggingCapsule = false
    private var dragTranslationPx = 0f
    private var dragStretch = 0f

    init {
        clipChildren = false
        clipToPadding = false
        setBackgroundColor(android.graphics.Color.TRANSPARENT)

        val density = capsuleDensity

        // 底层：整条普通磨砂玻璃底板，不使用明显折射或色散。
        // LiquidGlassView 在这里仅承担背景模糊；液态折射只留给上层胶囊。
        val blurBase = LiquidGlassView(context).apply {
            tag = "appspace_nav_blur_base"
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            setCornerRadius(42f * density)
            setRefractionHeight(0f)
            setRefractionOffset(0f)
            setDispersion(0f)
            setBlurRadius(18f)
            setTintAlpha(0.16f)
            setTintColorRed(0.20f)
            setTintColorGreen(0.20f)
            setTintColorBlue(0.22f)
            setTouchEffectEnabled(false)
            bind(contentContainer)
        }
        addView(
            blurBase,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )

        // 上层：唯一的真正液态玻璃胶囊。
        capsuleView = LiquidGlassView(context).apply {
            tag = "appspace_nav_selected_capsule"
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            setCornerRadius(36f * density)
            setRefractionHeight(18f * density)
            setRefractionOffset(18f * density)
            setDispersion(0.42f)
            // Preserve the transparent liquid-glass look: let refraction show the background.
            setBlurRadius(0.01f)
            setTintAlpha(0.10f)
            setTintColorRed(1f)
            setTintColorGreen(1f)
            setTintColorBlue(1f)
            // Built-in iOS-style press glow from AndroidLiquidGlassView.
            setTouchEffectEnabled(true)
            bind(contentContainer)
        }
        capsuleHost.apply {
            clipChildren = false
            clipToPadding = false
            addView(
                capsuleView,
                FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )
            )
        }
        addView(
            capsuleHost,
            LayoutParams(1, (66f * density).roundToInt()).apply {
                gravity = Gravity.TOP or Gravity.START
                leftMargin = sideInsetPx
                topMargin = (5f * density).roundToInt()
            }
        )

        val navCompose = ComposeView(context).apply {
            tag = "appspace_nav_items"
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                val currentSelectedTab = selectedTabState.value
                LaunchedEffect(currentSelectedTab) {
                    if (!isDraggingCapsule) post { animateCapsuleToSelected() }
                }
                GlassNavigationItems(
                    selectedTab = currentSelectedTab,
                    selectedAccentColor = AppSpaceNavigationAccentColor.value,
                    tabs = tabs,
                    onTabSelected = onTabSelected,
                    onDragStart = { beginCapsuleDrag() },
                    onDrag = { delta -> dragCapsuleBy(delta) },
                    onDragEnd = { finishCapsuleDrag() },
                    onCapsuleTouch = { action, x, y ->
                        forwardTouchToCapsule(action, x, y)
                    }
                )
            }
        }
        addView(
            navCompose,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )
        post { updateSelection() }
    }

    // The Compose navigation items are intentionally above the glass view so icons/text stay clear.
    // Forward touch events underneath to activate the library's built-in radial press glow.
    private var capsuleTouchDownTime = 0L

    private fun forwardTouchToCapsule(action: Int, xInNav: Float, yInNav: Float) {
        val now = SystemClock.uptimeMillis()
        if (action == MotionEvent.ACTION_DOWN || capsuleTouchDownTime == 0L) {
            capsuleTouchDownTime = now
            capsuleHost.animate().cancel()
            capsuleHost.animate()
                .scaleX(1.16f)
                .scaleY(1.18f)
                .setDuration(120L)
                .start()
        }
        // Clamp synthetic touch coordinates to the glass bounds so the library's
        // radial press glow stays inside the capsule even when a neighbouring item is touched.
        val localX = (xInNav - capsuleHost.x).coerceIn(0f, capsuleView.width.toFloat().coerceAtLeast(1f))
        val localY = (yInNav - capsuleHost.y).coerceIn(0f, capsuleView.height.toFloat().coerceAtLeast(1f))
        val event = MotionEvent.obtain(
            capsuleTouchDownTime,
            now,
            action,
            localX,
            localY,
            0
        )
        capsuleView.onTouchEvent(event)
        event.recycle()

        val pressed = action != MotionEvent.ACTION_UP && action != MotionEvent.ACTION_CANCEL

        if (!pressed) {
            capsuleTouchDownTime = 0L
            if (!isDraggingCapsule) {
                capsuleHost.animate().cancel()
                capsuleHost.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(160L)
                    .start()
            }
        }
    }

    private fun tabWidthPx(): Int {
        val count = tabs.size.coerceAtLeast(1)
        return ((width - sideInsetPx * 2).coerceAtLeast(0)) / count
    }

    fun updateSelection() {
        if (width <= 0 || tabs.isEmpty()) return
        if (!isDraggingCapsule) animateCapsuleToSelected()
    }

    private fun selectedIndex(): Int =
        tabs.indexOfFirst { it.first == selectedTabState.value }.coerceAtLeast(0)

    private fun targetTranslationForIndex(index: Int): Float =
        (index.coerceIn(0, tabs.lastIndex.coerceAtLeast(0)) * tabWidthPx()).toFloat()

    private fun ensureCapsuleWidth() {
        val targetWidth = tabWidthPx()
        val params = capsuleHost.layoutParams as? FrameLayout.LayoutParams ?: return
        if (targetWidth > 0 && params.width != targetWidth) {
            params.width = targetWidth
            capsuleHost.layoutParams = params
        }
    }

    private fun animateCapsuleToSelected() {
        if (width <= 0 || tabs.isEmpty()) return
        ensureCapsuleWidth()
        dragTranslationPx = targetTranslationForIndex(selectedIndex())
        capsuleHost.animate().cancel()
        capsuleHost.animate()
            .translationX(dragTranslationPx)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(230L)
            .start()
    }

    private fun beginCapsuleDrag() {
        if (width <= 0 || tabs.isEmpty()) return
        isDraggingCapsule = true
        capsuleHost.animate().cancel()
        dragTranslationPx = capsuleHost.translationX
        dragStretch = 0f
        capsuleHost.animate()
            .scaleX(1.16f)
            .scaleY(1.18f)
            .setDuration(120L)
            .start()
        // 按下时增强一点折射与色散，让胶囊看起来像被手指托起。
        capsuleView.setRefractionHeight(21f * capsuleDensity)
        capsuleView.setDispersion(0.48f)
        capsuleView.setTintAlpha(0.13f)
    }

    private fun dragCapsuleBy(deltaX: Float) {
        if (!isDraggingCapsule || tabs.isEmpty()) return
        ensureCapsuleWidth()
        val maxTranslation = (tabWidthPx() * (tabs.size - 1)).toFloat()
        dragTranslationPx = (dragTranslationPx + deltaX).coerceIn(0f, maxTranslation)
        capsuleHost.translationX = dragTranslationPx

        // 根据当前拖动速度近似值制造水滴拉伸：横向拉长、纵向略微压扁。
        // 使用平滑插值，避免每一帧因手指微抖而突然跳变。
        val movement = (kotlin.math.abs(deltaX) / (12f * capsuleDensity))
            .coerceIn(0f, 1f)
        dragStretch = (dragStretch * 0.55f + movement * 0.45f).coerceIn(0f, 1f)
        capsuleHost.scaleX = 1.16f + dragStretch * 0.10f
        capsuleHost.scaleY = 1.18f - dragStretch * 0.075f
        capsuleView.setRefractionHeight((21f + dragStretch * 7f) * capsuleDensity)
        capsuleView.setRefractionOffset((18f + dragStretch * 9f) * capsuleDensity)
        capsuleView.setDispersion(0.48f + dragStretch * 0.12f)
    }

    private fun finishCapsuleDrag() {
        if (!isDraggingCapsule || tabs.isEmpty()) return

        // Snap to the nearest tab center. Keep the drag state active while the
        // snap animation runs so LaunchedEffect(selectedTab) cannot start a
        // second animation and leave the capsule between tabs.
        val itemWidth = tabWidthPx().coerceAtLeast(1)
        val nearestIndex = (dragTranslationPx / itemWidth)
            .roundToInt()
            .coerceIn(0, tabs.lastIndex)
        val selectedTab = tabs[nearestIndex].first
        val targetX = targetTranslationForIndex(nearestIndex)

        dragTranslationPx = targetX
        onTabSelected(selectedTab)
        selectedTabState.value = selectedTab

        capsuleHost.animate().cancel()
        capsuleHost.animate()
            .translationX(targetX)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(220L)
            // Use a non-overshooting interpolator so the capsule settles
            // directly on the tab center without appearing to stop in-between.
            .setInterpolator(android.view.animation.DecelerateInterpolator(1.7f))
            .withEndAction {
                capsuleView.setRefractionHeight(18f * capsuleDensity)
                capsuleView.setRefractionOffset(18f * capsuleDensity)
                capsuleView.setDispersion(0.42f)
                capsuleView.setTintAlpha(0.10f)
                isDraggingCapsule = false
            }
            .start()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        post { updateSelection() }
    }
}

@Composable
private fun GlassNavigationItems(
    selectedTab: String,
    selectedAccentColor: Color,
    tabs: List<Triple<String, ImageVector, String>>,
    onTabSelected: (String) -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onCapsuleTouch: (Int, Float, Float) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, tab ->
            val selected = selectedTab == tab.first
            val selectedColor by animateColorAsState(
                targetValue = if (selected) selectedAccentColor else NavSecondaryText,
                label = "navigationIconColor"
            )
            val selectedTextColor by animateColorAsState(
                targetValue = if (selected) NavPrimaryText else NavSecondaryText,
                label = "navigationTextColor"
            )
            val verticalPadding by animateDpAsState(
                targetValue = if (selected) 9.dp else 7.dp,
                label = "navigationSelectedPadding"
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(24.dp))
                    // Track press/move/release without consuming events, forwarding them to the
                    // glass underneath so its built-in glow renders behind the icons and text.
                    .pointerInput(tab.first) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            var lastX = down.position.x
                            var lastY = down.position.y
                            var finished = false
                            val insetX = 4.dp.toPx()
                            val insetY = 5.dp.toPx()
                            try {
                                onCapsuleTouch(
                                    MotionEvent.ACTION_DOWN,
                                    insetX + index * size.width + lastX,
                                    insetY + lastY
                                )
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id }
                                        ?: break
                                    lastX = change.position.x
                                    lastY = change.position.y
                                    val action = if (change.pressed) {
                                        MotionEvent.ACTION_MOVE
                                    } else {
                                        MotionEvent.ACTION_UP
                                    }
                                    onCapsuleTouch(
                                        action,
                                        insetX + index * size.width + lastX,
                                        insetY + lastY
                                    )
                                    if (!change.pressed) {
                                        finished = true
                                        break
                                    }
                                }
                            } finally {
                                if (!finished) {
                                    onCapsuleTouch(
                                        MotionEvent.ACTION_CANCEL,
                                        insetX + index * size.width + lastX,
                                        insetY + lastY
                                    )
                                }
                            }
                        }
                    }
                    // Click and drag navigation behavior remains in the original handlers.
                    .pointerInput(tab.first) {
                        detectDragGestures(
                            onDragStart = { onDragStart() },
                            onDragEnd = { onDragEnd() },
                            onDragCancel = { onDragEnd() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDrag(dragAmount.x)
                            }
                        )
                    }
                    .clickable { onTabSelected(tab.first) }
                    .padding(vertical = verticalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = tab.second,
                    contentDescription = tab.third,
                    tint = selectedColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = tab.first,
                    color = selectedTextColor,
                    fontSize = 10.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
    }
}