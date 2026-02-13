package com.ruslan.foodtracker.domain.usecase.food

import com.ruslan.foodtracker.domain.model.Food
import com.ruslan.foodtracker.domain.model.NetworkResult
import com.ruslan.foodtracker.domain.model.andThen
import com.ruslan.foodtracker.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use Case для поиска продуктов по названию через Open Food Facts API
 *
 * Выполняет remote-first поиск с кэшированием и fallback:
 * 1. Поиск через API
 * 2. Кэширование успешных результатов в БД (бизнес-логика)
 * 3. При ошибке сети - fallback на локальный кэш (бизнес-логика)
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

        // Remote-first поиск с кэшированием и fallback
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
            .catch { exception ->
                // Бизнес-логика fallback: при ошибке сети ищем в локальном кэше
                emitAll(
                    repository.searchFoods(query)
                        .map { result ->
                            when (result) {
                                is NetworkResult.Success -> {
                                    if (result.data.isNotEmpty()) {
                                        result
                                    } else {
                                        NetworkResult.Error(
                                            message = "Нет подключения к интернету и нет кэшированных данных",
                                            exception = exception
                                        )
                                    }
                                }
                                is NetworkResult.Error -> {
                                    NetworkResult.Error(
                                        message = "Нет подключения к интернету и нет кэшированных данных",
                                        exception = exception
                                    )
                                }
                                is NetworkResult.Loading -> NetworkResult.Loading
                                is NetworkResult.Empty -> NetworkResult.Error(
                                    message = "Нет подключения к интернету и нет кэшированных данных",
                                    exception = exception
                                )
                            }
                        }
                )
            }
    }
}