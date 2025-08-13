package com.nezuko.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import kotlin.math.min

@Composable
fun CollapsingTopBarByFraction(
    collapseFraction: Float, // 0..1 (0 = expanded, 1 = collapsed)
    expandedHeight: Dp = 160.dp,
    collapsedHeight: Dp = 56.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    expandedContent: @Composable (progress: Float) -> Unit,
    collapsedContent: @Composable (progress: Float) -> Unit
) {
    // анимируем параметры визуально (плавно)
    val height by animateDpAsState(targetValue = lerp(expandedHeight, collapsedHeight, collapseFraction))
    val expandedAlpha by animateFloatAsState(targetValue = (1f - collapseFraction).coerceIn(0f, 1f))
    val collapsedAlpha by animateFloatAsState(targetValue = collapseFraction.coerceIn(0f, 1f))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(backgroundColor),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .alpha(expandedAlpha),
            contentAlignment = Alignment.CenterStart
        ) {
            expandedContent(collapseFraction)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .alpha(collapsedAlpha),
            contentAlignment = Alignment.CenterStart
        ) {
            collapsedContent(collapseFraction)
        }
    }
}

class CollapsingTopBarState internal constructor(
    internal val maxCollapsePx: Float,
    internal val offsetPxState: MutableState<Float>,
    val connection: NestedScrollConnection
) {
    val offsetPx: Float get() = offsetPxState.value
    val collapseFraction: Float get() = if (maxCollapsePx <= 0f) 1f else (offsetPxState.value / maxCollapsePx).coerceIn(0f, 1f)

    fun snapToCollapsed() { offsetPxState.value = maxCollapsePx }
    fun snapToExpanded() { offsetPxState.value = 0f }
}

@Composable
fun rememberCollapsingTopBarState(
    expandedHeight: Dp,
    collapsedHeight: Dp
): CollapsingTopBarState {
    val density = LocalDensity.current
    val maxCollapsePx = with(density) { (expandedHeight - collapsedHeight).coerceAtLeast(0.dp).toPx() }

    val offsetPxState = remember { mutableStateOf(0f) } // 0..maxCollapsePx

    // nested scroll connection: consumes scroll to update offset (collapse/expand)
    val connection = remember(maxCollapsePx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // available.y > 0 : scrolling down (pulling content down) -> expand (reduce offset)
                // available.y < 0 : scrolling up -> collapse (increase offset)
                val dy = available.y
                if (dy < 0f) {
                    // collapse - increase offset
                    val toConsume = min(-dy, maxCollapsePx - offsetPxState.value)
                    if (toConsume > 0f) {
                        offsetPxState.value += toConsume
                        return Offset(0f, -toConsume)
                    }
                } else if (dy > 0f) {
                    // expand - decrease offset
                    val toConsume = min(dy, offsetPxState.value)
                    if (toConsume > 0f) {
                        offsetPxState.value -= toConsume
                        return Offset(0f, toConsume)
                    }
                }
                return Offset.Zero
            }

            // optionally handle fling so toolbar snaps; simple version: consume fling if offset present
            override suspend fun onPreFling(available: androidx.compose.ui.unit.Velocity): androidx.compose.ui.unit.Velocity {
                // if fling upward (available.y < 0) and not fully collapsed -> finish collapse
                // if fling downward (available.y > 0) and not fully expanded -> finish expand
                val vy = available.y
                if (vy < 0f && offsetPxState.value < maxCollapsePx / 2f) {
                    // user flung up: snap to collapsed
                    offsetPxState.value = maxCollapsePx
                } else if (vy > 0f && offsetPxState.value > maxCollapsePx / 2f) {
                    // user flung down: snap to expanded
                    offsetPxState.value = 0f
                }
                // do not consume the fling motion (return Velocity.Zero) so list still scrolls
                return androidx.compose.ui.unit.Velocity.Zero
            }
        }
    }

    return remember(maxCollapsePx, offsetPxState, connection) {
        CollapsingTopBarState(maxCollapsePx, offsetPxState, connection)
    }
}
