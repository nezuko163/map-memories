package com.nezuko.ui.components

import android.R.attr.translationY
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import com.nezuko.ui.utils.lerpFloat
import kotlin.math.roundToInt


@Composable
fun CollapsingTopBarWithExpandedContent(
    state: CollapsingTopBarState,
    modifier: Modifier = Modifier,
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
        val animatedHeightPx =
            lerpFloat(expandedHeightPx.toFloat(), collapsedHeightPx.toFloat(), frac).roundToInt()

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

@Composable
fun SlidingTopBar(
    modifier: Modifier = Modifier,
    state: CollapsingTopBarState = rememberCollapsingTopBarState(),
    content: @Composable (progress: Float) -> Unit
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        // измеряем тулбар в "expanded" состоянии, чтобы получить его высоту
        val looseConstraints = constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)

        val expandedPlaceable = subcompose("expanded") {
            // рендерим контент как "expanded" (progress=1f) — это только для измерения
            content(1f)
        }.map { it.measure(looseConstraints) }
            .firstOrNull()

        val topHeightPx = (expandedPlaceable?.height ?: 0)
            .coerceIn(0, constraints.maxHeight)

        // обновляем state.maxCollapsePx (px) — только при существенном изменении
        val newMax = topHeightPx.toFloat()
        if (kotlin.math.abs(state.maxCollapsePx - newMax) > 0.5f) {
            state.maxCollapsePx = newMax
        }

        // капаем offset в корректный диапазон
        state.offsetPx = state.offsetPx.coerceIn(0f, state.maxCollapsePx)

        // progress 0..1: 1 = полностью виден, 0 = полностью спрятан
        val progress = if (state.maxCollapsePx <= 0f) 1f
        else ((state.maxCollapsePx - state.offsetPx) / state.maxCollapsePx).coerceIn(0f, 1f)

        // теперь сабкомпонуем реальный контент тулбара, с модификатором, который задаёт translationY
        val contentMeasurables = subcompose("content") {
            // применяем graphicsLayer с translationY в пикселях (state.offsetPx хранится в px)
            Box(modifier = Modifier.graphicsLayer {
                translationY = -state.offsetPx
            }) {
                content(progress)
            }
        }

        // измеряем под фиксированную высоту topHeightPx
        val contentConstraints = constraints.copy(minHeight = topHeightPx, maxHeight = topHeightPx)
        val contentPlaceable = contentMeasurables.map { it.measure(contentConstraints) }.firstOrNull()

        // layout имеет ширину от родителей и высоту topHeightPx
        val width = constraints.maxWidth
        val height = topHeightPx
        layout(width, height) {
            contentPlaceable?.place(0, 0)
        }
    }
}