package com.nezuko.map.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.nezuko.map.MapRoute
import kotlinx.serialization.Serializable

@Serializable
object Map

fun NavController.navigateToMap(
    navOptions: NavOptionsBuilder.() -> Unit = {}
) = navigate(Map, navOptions)

fun NavGraphBuilder.mapScreen() = composable<Map> { backStackEntry ->
    MapRoute()
}