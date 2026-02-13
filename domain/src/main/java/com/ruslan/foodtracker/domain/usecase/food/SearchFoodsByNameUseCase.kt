package com.ruslan.foodtracker.domain.usecase.food

import com.ruslan.foodtracker.domain.model.Food
import com.ruslan.foodtracker.domain.model.NetworkResult
import com.ruslan.foodtracker.domain.repository.FoodRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
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
@OptIn(ExperimentalCoroutinesApi::class)
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
            .flatMapLatest { remoteResult ->
                when (remoteResult) {
                    is NetworkResult.Success -> {
                        // Успех - кэшируем продукты с barcode
                        val foodsToCache = remoteResult.data.filter { it.barcode != null }
                        if (foodsToCache.isNotEmpty()) {
                            repository.insertFoods(foodsToCache)
                        }
                        flowOf(remoteResult)
                    }
                    is NetworkResult.Loading -> {
                        flowOf(NetworkResult.Loading)
                    }
                    is NetworkResult.Error, is NetworkResult.Empty -> {
                        // Бизнес-логика fallback: при ошибке API ищем в локальном кэше
                        repository.searchFoods(query)
                            .map { localResult ->
                                when (localResult) {
                                    is NetworkResult.Success -> {
                                        if (localResult.data.isNotEmpty()) {
                                            localResult
                                        } else {
                                            NetworkResult.Error(
                                                message = "Нет подключения к интернету и нет кэшированных данных",
                                                exception = (remoteResult as? NetworkResult.Error)?.exception
                                            )
                                        }
                                    }
                                    is NetworkResult.Error -> {
                                        NetworkResult.Error(
                                            message = "Нет подключения к интернету и нет кэшированных данных",
                                            exception = (remoteResult as? NetworkResult.Error)?.exception
                                        )
                                    }
                                    is NetworkResult.Loading -> NetworkResult.Loading
                                    is NetworkResult.Empty -> NetworkResult.Error(
                                        message = "Нет подключения к интернету и нет кэшированных данных",
                                        exception = (remoteResult as? NetworkResult.Error)?.exception
                                    )
                                }
                            }
                    }
                }
            }
    }
}