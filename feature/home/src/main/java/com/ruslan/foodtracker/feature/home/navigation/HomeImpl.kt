package com.ruslan.foodtracker.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.ruslan.foodtracker.core.navigation.NavRoutes
import com.ruslan.foodtracker.feature.home.presenter.AddFoodEntryScreen
import com.ruslan.foodtracker.feature.home.presenter.HomeScreen
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация навигации для feature:home модуля.
 */
@Singleton
class HomeImpl @Inject constructor() : HomeApi {

    override val baseRoute = "home"

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController
    ) {
        navGraphBuilder.composable<NavRoutes.Home> {
            HomeScreen(
                onNavigateToSearch = { mealType, date ->
                    navController.navigate(NavRoutes.Search(mealType = mealType, date = date))
                }
            )
        }

        navGraphBuilder.composable<NavRoutes.AddFoodEntry> { backStackEntry ->
            val route = backStackEntry.toRoute<NavRoutes.AddFoodEntry>()
            AddFoodEntryScreen(
                route = route,
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}
