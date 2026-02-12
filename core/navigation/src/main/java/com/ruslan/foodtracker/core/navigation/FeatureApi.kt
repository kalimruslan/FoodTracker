package com.ruslan.foodtracker.core.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

/**
 * Базовый интерфейс для всех feature модулей.
 * Каждый feature модуль предоставляет свою реализацию для регистрации навигационного графа.
 */
interface FeatureApi {
    /**
     * Базовый маршрут для feature модуля (например, "home", "search")
     */
    val baseRoute: String

    /**
     * Регистрация навигационного графа feature модуля.
     *
     * @param navGraphBuilder NavGraphBuilder для добавления composable экранов
     * @param navController NavHostController для навигации между экранами
     */
    fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController
    )
}
