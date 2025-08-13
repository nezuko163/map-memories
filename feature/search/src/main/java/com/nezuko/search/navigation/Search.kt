package com.nezuko.search.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.nezuko.search.SearchRoute
import kotlinx.serialization.Serializable

@Serializable
data class Search(val text: String)

fun NavController.navigateToSearch(
    text: String,
    navOptions: NavOptionsBuilder.() -> Unit = {}
) = navigate(Search(text), navOptions)

fun NavGraphBuilder.searchScreen(
    navigateBack: () -> Unit
) = composable<Search> { backStackEntry ->
    val route: Search = backStackEntry.toRoute()
    SearchRoute(
        initText = route.text,
        navigateBack = navigateBack
    )
}