package com.ruslan.foodtracker.feature.stats.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.ruslan.foodtracker.core.navigation.NavRoutes
import com.ruslan.foodtracker.feature.stats.presenter.StatsScreen
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsImpl @Inject constructor() : StatsApi {

    override val baseRoute = "stats"

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController
    ) {
        navGraphBuilder.composable<NavRoutes.Stats> {
            StatsScreen()
        }
    }
}
