package com.nezuko.memorydetails.navigation

import android.R.attr.text
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.nezuko.memorydetails.MemoryDetailsRoute
import kotlinx.serialization.Serializable

@Serializable
data class MemoryDetails(val id: Int)

fun NavController.navigateToMemoryDetails(
    id: Int,
    navOptions: NavOptionsBuilder.() -> Unit = {}
) = navigate(MemoryDetails(id), navOptions)

fun NavGraphBuilder.memoryDetailsScreen(
    navigateBack: () -> Unit
) = composable<MemoryDetails> { backStackEntry ->
    val route: MemoryDetails = backStackEntry.toRoute()
    MemoryDetailsRoute(
        id = route.id,
        navigateBack = navigateBack
    )
}
