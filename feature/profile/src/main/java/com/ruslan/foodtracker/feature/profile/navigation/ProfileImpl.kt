package com.ruslan.foodtracker.feature.profile.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.ruslan.foodtracker.core.navigation.NavRoutes
import com.ruslan.foodtracker.feature.profile.presenter.ProfileScreen
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileImpl @Inject constructor() : ProfileApi {

    override val baseRoute = "profile"

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController
    ) {
        navGraphBuilder.composable<NavRoutes.Profile> {
            ProfileScreen()
        }
    }
}
