package com.nezuko.main.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.nezuko.main.MainRoute
import kotlinx.serialization.Serializable

@Serializable
object Main

fun NavController.navigateToMain(
    navOptions: NavOptionsBuilder.() -> Unit = {}
) = navigate(Main, navOptions)

fun NavGraphBuilder.mainScreen(
    navigateToSearch: (text: String) -> Unit,
    navigateToDetails: (memory: Int) -> Unit
) = composable<Main> { backStackEntry ->
    MainRoute(navigateToSearch = navigateToSearch, navigateToDetails = navigateToDetails)
}