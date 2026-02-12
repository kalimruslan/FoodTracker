package com.ruslan.foodtracker.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ruslan.foodtracker.ui.home.HomeScreen
import com.ruslan.foodtracker.ui.product.ProductDetailScreen
import com.ruslan.foodtracker.ui.profile.ProfileScreen
import com.ruslan.foodtracker.ui.search.SearchScreen
import com.ruslan.foodtracker.ui.stats.StatsScreen

/**
 * Главный навигационный граф Food Tracker
 * Интегрирует Bottom Navigation Bar и все экраны приложения
 */
@Composable
fun FoodNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // Получаем текущий маршрут для определения активного таба
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route?.let { route ->
        when {
            route.contains("Home") -> NavRoutes.Home
            route.contains("Search") -> NavRoutes.Search
            route.contains("Stats") -> NavRoutes.Stats
            route.contains("Profile") -> NavRoutes.Profile
            route.contains("ProductDetail") -> {
                // Для ProductDetail нужно извлечь productId из route
                // Пока возвращаем null, так как Bottom Bar всё равно будет скрыт
                null
            }
            else -> null
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            // Показываем Bottom Navigation Bar только на основных экранах
            if (shouldShowBottomBar(currentRoute)) {
                FoodTrackerBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // Избегаем множественных копий одного экрана в back stack
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
            // Главная страница (Дневник питания)
            composable<NavRoutes.Home> {
                HomeScreen(
                    onNavigateToSearch = {
                        navController.navigate(NavRoutes.Search)
                    }
                )
            }

            // Поиск продуктов
            composable<NavRoutes.Search> {
                SearchScreen(
                    onNavigateToProduct = { productId ->
                        navController.navigate(NavRoutes.ProductDetail(productId))
                    },
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            }

            // Детальная информация о продукте
            composable<NavRoutes.ProductDetail> {
                ProductDetailScreen(
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            }

            // Статистика
            composable<NavRoutes.Stats> {
                StatsScreen()
            }

            // Профиль
            composable<NavRoutes.Profile> {
                ProfileScreen()
            }
        }
    }
}
