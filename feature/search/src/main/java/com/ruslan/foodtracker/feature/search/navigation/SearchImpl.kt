package com.ruslan.foodtracker.feature.search.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.ruslan.foodtracker.core.navigation.NavRoutes
import com.ruslan.foodtracker.feature.search.presenter.SearchScreen
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchImpl @Inject constructor() : SearchApi {

    override val baseRoute = "search"

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController
    ) {
        navGraphBuilder.composable<NavRoutes.Search> {
            SearchScreen(
                onNavigateToProduct = { productId ->
                    navController.navigate(NavRoutes.ProductDetail(productId))
                },
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
    }
}
