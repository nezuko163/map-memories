package com.nezuko.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SearchTextField(
    modifier: Modifier = Modifier,
    value: String = "",
    placeholder: String = "Поиск...",
    onValueChange: (String) -> Unit = {},
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
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
    TextField(
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, borderContainerColor, RoundedCornerShape(10.dp))
            .then(
                Modifier
                    .then(
                        if (onClick != null) {
                            Modifier.clickable { onClick() }
                        } else {
                            Modifier
                        })),
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = hintTextColor) },
        leadingIcon = if (shouldUseLeadingIcon) {
            { leadingIcon() }
        } else {
            null
        },
        trailingIcon = {
            if (value.isNotEmpty() && enabled) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = hintTextColor
                    )
                }
            }
        },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            cursorColor = textColor,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,

            disabledContainerColor = containerColor,
            disabledTextColor = textColor,
            disabledIndicatorColor = Color.Transparent
        ),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            onSearchClick()
            keyboardController?.hide()
            focusManager.clearFocus()
        })
    )
}


@Preview
@Composable
private fun Prev() {
    SearchTextField()
}