package com.nezuko.map_memories

import android.util.Log
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nezuko.main.navigation.Main
import com.nezuko.main.navigation.mainScreen
import com.nezuko.map.navigation.mapScreen
import com.nezuko.memorydetails.navigation.memoryDetailsScreen
import com.nezuko.memorydetails.navigation.navigateToMemoryDetails
import com.nezuko.profile.navigation.profileScreen
import com.nezuko.search.navigation.navigateToSearch
import com.nezuko.search.navigation.searchScreen

private const val TAG = "Application"

@Composable
fun Application(
    modifier: Modifier = Modifier,
    startDestination: Any = Main,
    navController: NavHostController = rememberNavController()
) {
    var currentRoute by remember {
        mutableStateOf(startDestination)
    }
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRouteReal = currentBackStackEntry?.destination?.route
    Log.i(TAG, "Application: $currentRouteReal")
    val showBottomBar = when (currentRouteReal) {
        "com.nezuko.main.navigation.Main" -> true
        "com.nezuko.map.navigation.Map" -> true
        "com.nezuko.profile.navigation.Profile" -> true
        else -> false
    }
    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
        bottomBar = {
            if (showBottomBar)
                BottomNavigationBar(
                    navController,
                    currentRoute,
                    { currentRoute = it }
                )
        }
    ) {
        NavHost(
            popExitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            modifier = modifier
                .padding(it),
            navController = navController,
            startDestination = startDestination

        ) {
            mainScreen(
                navigateToSearch = navController::navigateToSearch,
                navigateToDetails = navController::navigateToMemoryDetails
            )
            mapScreen()
            profileScreen()
            searchScreen(navigateBack = navController::popBackStack)
            memoryDetailsScreen(navigateBack = navController::popBackStack)
        }
    }
}