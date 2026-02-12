package com.ruslan.foodtracker.domain.usecase.food

import com.ruslan.foodtracker.domain.model.Food
import com.ruslan.foodtracker.domain.model.NetworkResult
import com.ruslan.foodtracker.domain.repository.FoodRepository
import javax.inject.Inject

/**
 * Use Case для поиска продуктов по названию через Open Food Facts API
 *
 * Выполняет remote-first поиск:
 * 1. Поиск через API
 * 2. Кэширование результатов
 * 3. Fallback на локальный кэш при ошибке сети
 */
class SearchFoodsByNameUseCase @Inject constructor(
    private val repository: FoodRepository
) {
    /**
     * Поиск продуктов по текстовому запросу
     *
     * @param query Поисковый запрос (название продукта, бренд и т.д.)
     * @return NetworkResult со списком найденных продуктов
     */
    suspend operator fun invoke(query: String): NetworkResult<List<Food>> {
        // Валидация запроса
        if (query.isBlank()) {
            return NetworkResult.Error("Поисковый запрос не может быть пустым")
        }

        // Минимальная длина запроса для эффективного поиска
        if (query.length < 2) {
            return NetworkResult.Error("Введите минимум 2 символа для поиска")
        }

        return repository.searchFoodsByNameRemote(query)
    }
}