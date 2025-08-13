package com.nezuko.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun UnderlinedTextWithGap(
    text: String,
    gap: Dp = 6.dp,
    lineThickness: Dp = 1.dp,
    color: Color = MaterialTheme.colorScheme.onSurface,
    style: TextStyle = LocalTextStyle.current
) {
    val density = LocalDensity.current
    var textWidthPx by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier.wrapContentWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = text,
            style = style,
            color = color,
            onTextLayout = { result ->
                textWidthPx = result.size.width
            }
        )

        Spacer(modifier = Modifier.height(gap))

        val lineWidthDp = with(density) { textWidthPx.toDp() }
        Box(
            modifier = Modifier
                .width(lineWidthDp)
                .height(lineThickness)
                .background(color)
        )
    }
}
