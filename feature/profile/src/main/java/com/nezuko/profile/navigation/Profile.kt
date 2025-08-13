package com.nezuko.profile.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.nezuko.profile.ProfileRoute
import kotlinx.serialization.Serializable

@Serializable
object Profile

fun NavController.navigateToProfile(
    navOptions: NavOptionsBuilder.() -> Unit = {}
) = navigate(Profile, navOptions)

fun NavGraphBuilder.profileScreen() = composable<Profile> { backStackEntry ->
    ProfileRoute()
}