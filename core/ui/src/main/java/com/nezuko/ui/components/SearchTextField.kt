package com.nezuko.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SearchTextField(
    modifier: Modifier = Modifier,
    value: String = "",
    placeholder: String = "Поиск...",
    onValueChange: (String) -> Unit = {},
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    height: Dp = 48.dp,
    shouldUseLeadingIcon: Boolean = true,
    onSearchClick: () -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    borderContainerColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    hintTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    leadingIcon: @Composable () -> Unit = {
        Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
    },
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // remember-кеширование модификаторов и размеров, зависящих от параметров
    val baseModifier = remember(modifier, height, containerColor, borderContainerColor) {
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(10.dp))
            .background(containerColor)
            .border(width = 1.dp, color = borderContainerColor, shape = RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp)
    }

    val clickableModifier = remember(onClick) {
        if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    }

    val iconSize = remember(height) { (height * 0.6f).coerceAtLeast(24.dp) }
    val textStyle = LocalTextStyle.current.copy(color = textColor)

    Box(
        modifier = baseModifier.then(clickableModifier),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (shouldUseLeadingIcon) {
                LeadingIconSlot(leadingIcon = leadingIcon, size = iconSize)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterStart
            ) {
                KeyedBasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    placeholder = placeholder,
                    hintTextColor = hintTextColor,
                    textStyle = textStyle,
                    keyboardController = keyboardController,
                    focusManager = focusManager,
                    onSearchClick = onSearchClick,
                )
            }

            if (value.isNotEmpty() && enabled && onClick == null) {
                ClearButton(onClear = { onValueChange("") }, size = iconSize, tint = hintTextColor)
            }
        }
    }
}

@Composable
private fun LeadingIconSlot(leadingIcon: @Composable () -> Unit, size: Dp) {
    val m = remember(size) {
        Modifier
            .size(size)
            .padding(end = 8.dp)
    }
    Box(modifier = m, contentAlignment = Alignment.Center) { leadingIcon() }
}

@Composable
private fun ClearButton(onClear: () -> Unit, size: Dp, tint: Color) {
    IconButton(onClick = onClear, modifier = remember(size) { Modifier.size(size) }) {
        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = tint)
    }
}

@Composable
private fun KeyedBasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    placeholder: String,
    hintTextColor: Color,
    textStyle: TextStyle,
    keyboardController: SoftwareKeyboardController?,
    focusManager: FocusManager,
    onSearchClick: () -> Unit
) {
    // создаём внутренние лямбды и запоминаем их, но они обновятся автоматически, если внешние колбэки поменялись
    val onInnerValueChange =
        remember(onValueChange) { { newText: String -> onValueChange(newText) } }

    val keyboardActions = remember(onSearchClick) {
        KeyboardActions(onSearch = {
            onSearchClick()
            keyboardController?.hide()
            focusManager.clearFocus()
        })
    }

    BasicTextField(
        value = value,
        onValueChange = onInnerValueChange,
        singleLine = true,
        textStyle = textStyle,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
        keyboardActions = keyboardActions,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp),
        decorationBox = { innerTextField ->
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = textStyle,
                    color = hintTextColor,
                    maxLines = 1
                )
            }
            innerTextField()
        }
    )
}

@Preview
@Composable
private fun Prev() {
    SearchTextField()
}