package com.ruslan.foodtracker.core.navigation

import kotlinx.serialization.Serializable

/**
 * Навигационные маршруты Food Tracker
 * Используется type-safe navigation с Kotlin Serialization
 */
sealed interface NavRoutes {
    /**
     * Главная страница (Дневник питания)
     * - Кольцевая диаграмма калорий
     * - Макронутриенты (Б/Ж/У/Клетчатка)
     * - Приёмы пищи за выбранный день
     * - Трекер воды
     */
    @Serializable
    data object Home : NavRoutes

    /**
     * Поиск продуктов
     * @param mealType MealType.name — если непустая, Search в режиме выбора (добавление в дневник)
     * @param date LocalDate.toString() — дата записи (используется в режиме выбора)
     * - Строка поиска
     * - Табы: Поиск, Недавние, Избранное, Рецепты
     * - Список продуктов (ProductCard)
     * - Сканер штрихкода
     */
    @Serializable
    data class Search(
        val mealType: String = "",
        val date: String = ""
    ) : NavRoutes

    /**
     * Детальная информация о продукте
     * @param productId идентификатор продукта (barcode или local id)
     * - Кольцевая диаграмма калорий
     * - Выбор порции (вес, единицы)
     * - Макро и микронутриенты
     * - Кнопка "Добавить в приём пищи"
     */
    @Serializable
    data class ProductDetail(
        val productId: String
    ) : NavRoutes

    /**
     * Экран настройки порции перед добавлением продукта в дневник питания
     * Все данные о продукте передаются через параметры навигации
     */
    @Serializable
    data class AddFoodEntry(
        val foodId: Long = 0L,
        val foodName: String,
        val brand: String = "",
        val caloriesPerServing: Int,
        val proteinPerServing: Double,
        val carbsPerServing: Double,
        val fatPerServing: Double,
        val servingSize: Double,
        val servingUnit: String,
        val mealType: String,
        val date: String,
        val isFavorite: Boolean = false
    ) : NavRoutes

    /**
     * Статистика за период
     * - Столбчатая диаграмма калорий (неделя/месяц/3мес)
     * - Средние показатели (калории, Б/Ж/У)
     * - Распределение БЖУ
     */
    @Serializable
    data object Stats : NavRoutes

    /**
     * Профиль пользователя
     * - Цель (похудеть/поддержать/набрать)
     * - Параметры тела (вес, рост, возраст, пол, активность, темп)
     * - Расчёт калорий (BMR, TDEE, целевые калории)
     * - Динамика веса
     * - Фото прогресса
     * - Напоминания
     */
    @Serializable
    data object Profile : NavRoutes
}
