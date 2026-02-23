package com.ruslan.foodtracker.domain.usecase.food

import com.ruslan.foodtracker.domain.error.DomainError
import com.ruslan.foodtracker.domain.model.Food
import com.ruslan.foodtracker.domain.model.NetworkResult
import com.ruslan.foodtracker.domain.model.PaginatedResult
import com.ruslan.foodtracker.domain.repository.FoodRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use Case для поиска продуктов по названию через Open Food Facts API с пагинацией
 *
 * Выполняет remote-first поиск с кэшированием и fallback:
 * 1. Пои lfск через API с пагинацией
 * 2. Кэширование успешных результатов в БД (бизнес-логика)
 * 3. При ошибке сети - fallback на локальный кэш (без пагинации, бизнес-логика)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchFoodsByNameUseCase
    @Inject
    constructor(
        private val repository: FoodRepository
    ) {
        /**
         * Поиск продуктов по текстовому запросу с пагинацией
         *
         * @param query Поисковый запрос (название продукта, бренд и т.д.)
         * @param page Номер страницы (начинается с 1)
         * @return Flow<NetworkResult<PaginatedResult<Food>>> с пагинированными результатами
         */
        operator fun invoke(
            query: String,
            page: Int = 1
        ): Flow<NetworkResult<PaginatedResult<Food>>> {
            // Валидация запроса
            if (query.isBlank()) {
                return flowOf(NetworkResult.Error(DomainError.Validation.EmptyQuery))
            }

            // Минимальная длина запроса для эффективного поиска
            if (query.length < 2) {
                return flowOf(NetworkResult.Error(DomainError.Validation.QueryTooShort))
            }

            // Remote-first поиск с кэшированием и fallback
            return repository
                .searchFoodsByNameRemote(query, page)
                .flatMapLatest { remoteResult ->
                    when (remoteResult) {
                        is NetworkResult.Success -> {
                            // Успех - кэшируем продукты с barcode
                            val foodsToCache = remoteResult.data.data.filter { it.barcode != null }
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
                            // Только для первой страницы (page == 1), для последующих страниц возвращаем ошибку
                            if (page == 1) {
                                repository
                                    .searchFoods(query)
                                    .map { localResult ->
                                        when (localResult) {
                                            is NetworkResult.Success -> {
                                                if (localResult.data.isNotEmpty()) {
                                                    // Конвертируем локальный результат в PaginatedResult (одна страница)
                                                    NetworkResult.Success(
                                                        PaginatedResult.single(localResult.data)
                                                    )
                                                } else {
                                                    NetworkResult.Error(
                                                        error = DomainError.Data.NoCache,
                                                        exception = (remoteResult as? NetworkResult.Error)?.exception
                                                    )
                                                }
                                            }
                                            is NetworkResult.Error -> {
                                                NetworkResult.Error(
                                                    error = DomainError.Data.NoCache,
                                                    exception = (remoteResult as? NetworkResult.Error)?.exception
                                                )
                                            }
                                            is NetworkResult.Loading -> NetworkResult.Loading
                                            is NetworkResult.Empty -> NetworkResult.Error(
                                                error = DomainError.Data.NoCache,
                                                exception = (remoteResult as? NetworkResult.Error)?.exception
                                            )
                                        }
                                    }
                            } else {
                                // Для page > 1 не делаем fallback, просто возвращаем ошибку
                                flowOf(remoteResult)
                            }
                        }
                    }
                }
        }
    }
