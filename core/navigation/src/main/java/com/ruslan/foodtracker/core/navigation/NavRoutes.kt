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
     * - Строка поиска
     * - Табы: Поиск, Недавние, Избранное, Рецепты
     * - Список продуктов (ProductCard)
     * - Сканер штрихкода
     */
    @Serializable
    data object Search : NavRoutes

    /**
     * Детальная информация о продукте
     * @param productId идентификатор продукта (barcode или local id)
     * - Кольцевая диаграмма калорий
     * - Выбор порции (вес, единицы)
     * - Макро и микронутриенты
     * - Кнопка "Добавить в приём пищи"
     */
    @Serializable
    data class ProductDetail(val productId: String) : NavRoutes

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
