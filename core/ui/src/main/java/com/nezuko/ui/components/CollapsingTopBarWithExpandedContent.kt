package com.nezuko.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import com.nezuko.ui.utils.lerpFloat
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * State holder for collapsing top bar.
 */
@Stable
class CollapsingTopBarWithExpandedContentState internal constructor(
    internal val maxCollapsePxState: MutableState<Float>,
    internal val offsetPxState: MutableState<Float>,
    internal val connection: NestedScrollConnection,
) {
    var maxCollapsePx: Float
        get() = maxCollapsePxState.value
        set(v) { maxCollapsePxState.value = v }

    var offsetPx: Float
        get() = offsetPxState.value
        set(v) { offsetPxState.value = v }

    val collapseFraction: Float
        get() = if (maxCollapsePx <= 0f) 1f else (offsetPx / maxCollapsePx).coerceIn(0f, 1f)

    // expose nested scroll connection for convenience
    val nestedScrollConnection: NestedScrollConnection get() = connection
}

/**
 * remember helper — returns state with nested scroll connection and animatable backing.
 */
@Composable
fun rememberCollapsingTopBarWithExpandedContentState(): CollapsingTopBarWithExpandedContentState {
    val maxCollapsePxState = rememberSaveable { mutableStateOf(0f) }
    val offsetPxState = rememberSaveable { mutableStateOf(0f) }

    // NestedScrollConnection must read latest mutable state objects (we capture the MutableState instances,
    // so reading .value is fresh inside callbacks).
    val connection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val dy = available.y
                val maxCollapse = maxCollapsePxState.value
                val currentOffset = offsetPxState.value

                if (dy < 0f) {
                    // collapse (scroll up) — increase offset
                    val toConsume = min(-dy, max(0f, maxCollapse - currentOffset))
                    if (toConsume > 0f) {
                        offsetPxState.value = currentOffset + toConsume
                        return Offset(0f, -toConsume)
                    }
                } else if (dy > 0f) {
                    // expand (scroll down) — reduce offset
                    val toConsume = min(dy, currentOffset)
                    if (toConsume > 0f) {
                        offsetPxState.value = currentOffset - toConsume
                        return Offset(0f, toConsume)
                    }
                }
                return Offset.Zero
            }
        }
    }

    return remember {
        CollapsingTopBarWithExpandedContentState(
            maxCollapsePxState = maxCollapsePxState,
            offsetPxState = offsetPxState,
            connection = connection
        )
    }
}

/**
 * CollapsingTopBar composable — measures expanded & collapsed slots and cross-fades them depending on state.
 *
 * - state: rememberCollapsingTopBarState()
 * - Pass Modifier.nestedScroll(state.nestedScrollConnection) to the parent that should intercept gestures.
 */
@Composable
fun CollapsingTopBarWithExpandedContent(
    state: CollapsingTopBarState,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
    collapsedContent: @Composable (progress: Float) -> Unit = {},
    expandedContent: @Composable (progress: Float) -> Unit
) {
    SubcomposeLayout(modifier = modifier.fillMaxWidth()) { constraints ->
        // Measure in "loose" vertical constraints so expanded can be its full measured height
        val loose = constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)

        // Measure expanded content (full width)
        val expandedPlaceablesForMeasure = subcompose("expanded_measure") {
            Box(modifier = Modifier.fillMaxWidth()) { expandedContent(0f) }
        }.map { it.measure(loose) }

        val expandedHeightPx = expandedPlaceablesForMeasure.maxOfOrNull { it.height } ?: 0

        // Measure collapsed content
        val collapsedPlaceablesForMeasure = subcompose("collapsed_measure") {
            Box(modifier = Modifier.fillMaxWidth()) { collapsedContent(1f) }
        }.map { it.measure(loose) }

        val collapsedHeightPx = collapsedPlaceablesForMeasure.maxOfOrNull { it.height } ?: 0

        // update max collapse in state if changed
        val computedMaxCollapse = maxOf(0f, (expandedHeightPx - collapsedHeightPx).toFloat())
        if (state.maxCollapsePx != computedMaxCollapse) {
            state.maxCollapsePx = computedMaxCollapse
            // clamp offset when max changes
            state.offsetPx = state.offsetPx.coerceIn(0f, computedMaxCollapse)
        }

        val frac = state.collapseFraction.coerceIn(0f, 1f)
        val animatedHeightPx = lerpFloat(expandedHeightPx.toFloat(), collapsedHeightPx.toFloat(), frac).roundToInt()

        // compute alpha for cross-fade
        val expandedAlpha = (1f - frac).coerceIn(0f, 1f)
        val collapsedAlpha = frac.coerceIn(0f, 1f)

        // Compose draw pass constrained to final height
        val expandedPlaceables = subcompose("expanded_draw") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(expandedAlpha)
            ) {
                expandedContent(frac)
            }
        }.map { it.measure(constraints.copy(maxHeight = animatedHeightPx)) }

        val collapsedPlaceables = subcompose("collapsed_draw") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(collapsedAlpha)
            ) {
                collapsedContent(frac)
            }
        }.map { it.measure(constraints.copy(maxHeight = animatedHeightPx)) }

        val layoutWidth = constraints.maxWidth
        val layoutHeight = animatedHeightPx.coerceAtLeast(0)

        layout(layoutWidth, layoutHeight) {
            // place expanded and collapsed content bottom-aligned (so they overlap nicely)
            expandedPlaceables.forEach { p ->
                val x = 0
                val y = layoutHeight - p.height
                p.placeRelative(x, y)
            }
            collapsedPlaceables.forEach { p ->
                val x = 0
                val y = layoutHeight - p.height
                p.placeRelative(x, y)
            }
        }
    }
}