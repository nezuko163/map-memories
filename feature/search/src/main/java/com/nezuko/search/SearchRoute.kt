package com.nezuko.search

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.nezuko.ui.components.SearchTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchRoute(
    initText: String,
    navigateBack: () -> Unit,
    vm: SearchViewModel = hiltViewModel()
) {
    val text by vm.query.collectAsState(initText)
    val suggestions by vm.suggestions.collectAsState(null)

    LaunchedEffect(Unit) {
        vm.changeText(initText)
    }

    SearchTextField(
        value = text,
        onValueChange = vm::changeText,
        onSearchClick = {
            vm.setQueryToArgumentsHolder()
            navigateBack()
        }
    )

    if (suggestions != null) {
        Column {
            suggestions!!.forEach { suggestion ->
                Column {
                    Text(suggestion)
                }
            }
        }
    }
}