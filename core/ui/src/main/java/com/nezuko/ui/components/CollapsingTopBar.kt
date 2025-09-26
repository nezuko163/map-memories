package com.nezuko.ui.components

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Velocity
import com.nezuko.ui.utils.lerpFloat
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val TAG = "CollapsingTopBar"

@Stable
class CollapsingTopBarState internal constructor(
    internal val maxCollapsePxState: MutableFloatState,
    internal val offsetPxState: MutableFloatState,
    val connection: NestedScrollConnection,
) {
    var maxCollapsePx: Float
        get() = maxCollapsePxState.floatValue
        set(v) {
            maxCollapsePxState.floatValue = v
        }

    var offsetPx: Float
        get() = offsetPxState.floatValue
        set(v) {
            offsetPxState.floatValue = v
        }

    val collapseFraction: Float
        get() = if (maxCollapsePx <= 0f) 1f else (offsetPx / maxCollapsePx).coerceIn(0f, 1f)

}

@Composable
fun rememberCollapsingTopBarState(): CollapsingTopBarState {
    val maxCollapsePxState = rememberSaveable { mutableFloatStateOf(0f) }
    val offsetPxState = rememberSaveable { mutableFloatStateOf(0f) }

    val connection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val dy = available.y
                val maxCollapse = maxCollapsePxState.floatValue
                val currentOffset = offsetPxState.floatValue

                if (dy < 0f) {
                    // collapse (scroll up) — increase offset, consume what we can
                    val toConsume = min(-dy, max(0f, maxCollapse - currentOffset))
                    if (toConsume > 0f) {
                        offsetPxState.floatValue = currentOffset + toConsume
                        // no snapTo here — keep Animatable untouched during gesture
                        return Offset(0f, -toConsume)
                    }
                } else if (dy > 0f) {
                    // expand (scroll down) — reduce offset
                    val toConsume = min(dy, currentOffset)
                    if (toConsume > 0f) {
                        offsetPxState.floatValue = currentOffset - toConsume
                        // no snapTo here
                        return Offset(0f, toConsume)
                    }
                }
                return Offset.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                val dy = available.y
                val maxCollapse = maxCollapsePxState.floatValue
                val current = offsetPxState.floatValue

                if (dy < 0f) {
                    val toConsume = min(-dy, max(0f, maxCollapse - current))
                    if (toConsume > 0f) {
                        offsetPxState.floatValue = current + toConsume
                        return Offset(0f, -toConsume)
                    }
                } else if (dy > 0f) {
                    val toConsume = min(dy, current)
                    if (toConsume > 0f) {
                        offsetPxState.floatValue = current - toConsume
                        return Offset(0f, toConsume)
                    }
                }
                return Offset.Zero
            }

//            override suspend fun onPreFling(available: Velocity): Velocity {
//                val cur = offsetPxState.floatValue
//                val max = maxCollapsePxState.floatValue
//                if (max <= 0f) return Velocity.Zero
//
//                val target = if (cur > max / 2f) max else 0f
//                // синхронизируем animatable с текущим значением и анимируем
//                offsetAnim.snapTo(cur)
//                offsetAnim.animateTo(target, animationSpec = spring())
//                offsetPxState.floatValue = offsetAnim.value
//                // мы обработали fling — возвращаем входящую скорость (т.е. потребили её)
//                return available
//            }
        }
    }

    return remember {
        CollapsingTopBarState(
            maxCollapsePxState,
            offsetPxState,
            connection
        )
    }
}

@Composable
fun CollapsingTopBar(
    modifier: Modifier = Modifier,
    state: CollapsingTopBarState = rememberCollapsingTopBarState(),
    background: Color = MaterialTheme.colorScheme.primaryContainer,
    content: @Composable () -> Unit
) {
    SubcomposeLayout(modifier.background(color = background)) { constraints ->
        val looseConstraints = constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)

        val expandedMeasurables = subcompose("expanded") {
            content() // полностью раскрытое состояние
        }
        val expandedPlaceable = expandedMeasurables
            .map { it.measure(looseConstraints) }
            .firstOrNull()

        val collapsedH = 0
        val expandedH = (expandedPlaceable?.height ?: collapsedH)
            .coerceIn(collapsedH, constraints.maxHeight)

        val newMaxCollapse = (expandedH - collapsedH).toFloat().coerceAtLeast(0f)
        if (abs(state.maxCollapsePx - newMaxCollapse) > 0.5f) {
            // internal поле maxCollapsePxState есть в твоём state — обновляем
            state.maxCollapsePx = newMaxCollapse
        }

        state.offsetPx = state.offsetPx.coerceIn(0f, state.maxCollapsePx)

        val contentMeasurables = subcompose("content_draw") {
            content()
        }

        val height = lerpFloat(expandedH.toFloat(), collapsedH.toFloat(), state.collapseFraction)
            .roundToInt()
            .coerceIn(0, expandedH)

        val contentConstraints = constraints.copy(
            minHeight = height,
            maxHeight = height
        )

        val contentPlaceable = contentMeasurables
            .map { it.measure(contentConstraints) }
            .firstOrNull()

        layout(contentConstraints.maxWidth, height) {
            contentPlaceable?.place(0, 0)
        }
    }
}