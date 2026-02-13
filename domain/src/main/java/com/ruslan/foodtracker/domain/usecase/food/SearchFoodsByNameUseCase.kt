package com.ruslan.foodtracker.domain.usecase.food

import com.ruslan.foodtracker.domain.model.Food
import com.ruslan.foodtracker.domain.model.NetworkResult
import com.ruslan.foodtracker.domain.model.andThen
import com.ruslan.foodtracker.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * Use Case для поиска продуктов по названию через Open Food Facts API
 *
 * Выполняет remote-first поиск с кэшированием:
 * 1. Поиск через API
 * 2. Кэширование успешных результатов в БД (бизнес-логика)
 * 3. Fallback на локальный кэш при ошибке сети (в Repository)
 */
class SearchFoodsByNameUseCase @Inject constructor(
    private val repository: FoodRepository
) {
    /**
     * Поиск продуктов по текстовому запросу
     *
     * @param query Поисковый запрос (название продукта, бренд и т.д.)
     * @return Flow<NetworkResult> со списком найденных продуктов
     */
    operator fun invoke(query: String): Flow<NetworkResult<List<Food>>> {
        // Валидация запроса
        if (query.isBlank()) {
            return flowOf(NetworkResult.Error("Поисковый запрос не может быть пустым"))
        }

        // Минимальная длина запроса для эффективного поиска
        if (query.length < 2) {
            return flowOf(NetworkResult.Error("Введите минимум 2 символа для поиска"))
        }

        // Remote-first поиск с кэшированием через andThen
        return repository.searchFoodsByNameRemote(query)
            .andThen { foods ->
                // Бизнес-логика кэширования (только продукты с barcode)
                foods
                    .filter { it.barcode != null }
                    .forEach { food ->
                        // Игнорируем ошибки (дубликаты и т.д.)
                        repository.insertFood(food)
                    }

                // Возвращаем оригинальный список продуктов
                flowOf(NetworkResult.Success(foods))
            }
    }
}