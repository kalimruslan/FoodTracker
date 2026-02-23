package com.ruslan.foodtracker.feature.search.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.ruslan.foodtracker.core.navigation.NavRoutes
import com.ruslan.foodtracker.feature.search.presenter.SearchScreen
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchImpl
    @Inject
    constructor() : SearchApi {
        override val baseRoute = "search"

        override fun registerGraph(
            navGraphBuilder: NavGraphBuilder,
            navController: NavHostController
        ) {
            navGraphBuilder.composable<NavRoutes.Search> { backStackEntry ->
                val route = backStackEntry.toRoute<NavRoutes.Search>()
                val isSelectionMode = route.mealType.isNotEmpty()

                SearchScreen(
                    onProductClick = { product ->
                        if (isSelectionMode) {
                            navController.navigate(
                                NavRoutes.AddFoodEntry(
                                    foodId = product.localFoodId,
                                    foodName = product.name,
                                    brand = product.brand ?: "",
                                    caloriesPerServing = product.calories,
                                    proteinPerServing = product.protein.toDouble(),
                                    carbsPerServing = product.carbs.toDouble(),
                                    fatPerServing = product.fat.toDouble(),
                                    servingSize = product.servingSizeGrams,
                                    servingUnit = product.servingUnit,
                                    mealType = route.mealType,
                                    date = route.date,
                                    isFavorite = product.isFavorite
                                )
                            )
                        } else {
                            navController.navigate(NavRoutes.ProductDetail(product.id))
                        }
                    },
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            }
        }
    }
