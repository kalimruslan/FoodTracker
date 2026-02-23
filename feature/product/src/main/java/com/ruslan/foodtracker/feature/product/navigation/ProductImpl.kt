package com.ruslan.foodtracker.feature.product.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.ruslan.foodtracker.core.navigation.NavRoutes
import com.ruslan.foodtracker.feature.product.presenter.ProductDetailScreen
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductImpl
    @Inject
    constructor() : ProductApi {
        override val baseRoute = "product"

        override fun registerGraph(
            navGraphBuilder: NavGraphBuilder,
            navController: NavHostController
        ) {
            navGraphBuilder.composable<NavRoutes.ProductDetail> {
                ProductDetailScreen(
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            }
        }
    }
