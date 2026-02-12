package com.ruslan.foodtracker.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ruslan.foodtracker.core.navigation.FeatureApi
import com.ruslan.foodtracker.core.navigation.NavRoutes

/**
 * Главный навигационный граф Food Tracker.
 * Автоматически регистрирует все feature модули через FeatureApi.
 */
@Composable
fun FoodNavHost(
    navController: NavHostController,
    featureApis: Set<FeatureApi>,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route?.let { route ->
        when {
            route.contains("Home") -> NavRoutes.Home
            route.contains("Search") -> NavRoutes.Search
            route.contains("Stats") -> NavRoutes.Stats
            route.contains("Profile") -> NavRoutes.Profile
            route.contains("ProductDetail") -> null // Скрываем Bottom Bar
            else -> null
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (shouldShowBottomBar(currentRoute)) {
                FoodTrackerBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(NavRoutes.Home) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.Home,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Автоматическая регистрация всех feature графов
            featureApis.forEach { api ->
                api.registerGraph(this, navController)
            }
        }
    }
}
