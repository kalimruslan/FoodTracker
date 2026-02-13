package com.ruslan.foodtracker.data.repository

import com.ruslan.foodtracker.data.mapper.toDomain
import com.ruslan.foodtracker.data.mapper.toEntity
import com.ruslan.foodtracker.data.remote.mapper.toDomain
import com.ruslan.foodtracker.data.remote.mapper.toDomainList
import com.ruslan.foodtracker.data.source.local.FoodLocalDataSource
import com.ruslan.foodtracker.data.source.remote.FoodRemoteDataSource
import com.ruslan.foodtracker.domain.error.DomainError
import com.ruslan.foodtracker.domain.model.Food
import com.ruslan.foodtracker.domain.model.NetworkResult
import com.ruslan.foodtracker.domain.model.mapNetworkResult
import com.ruslan.foodtracker.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

/**
 * Реализация FoodRepository с использованием DataSource паттерна
 *
 * Ответственность:
 * - Координация LocalDataSource и RemoteDataSource
 * - Маппинг Entity/DTO → Domain модели
 * - Fallback стратегия при ошибках сети
 */
class FoodRepositoryImpl @Inject constructor(
    private val localDataSource: FoodLocalDataSource,
    private val remoteDataSource: FoodRemoteDataSource
) : FoodRepository {

    // ========== LOCAL DATABASE OPERATIONS ==========

    override fun getAllFoods(): Flow<NetworkResult<List<Food>>> =
        localDataSource.getAllFoods()
            .map<List<com.ruslan.foodtracker.data.local.entity.FoodEntity>, NetworkResult<List<Food>>> { entities ->
                NetworkResult.Success(entities.map { it.toDomain() })
            }
            .onStart { emit(NetworkResult.Loading) }
            .catch { e ->
                emit(NetworkResult.Error(
                    error = DomainError.Database.FetchFailed,
                    exception = e
                ))
            }

    override suspend fun getFoodById(id: Long): NetworkResult<Food> = try {
        val entity = localDataSource.getFoodById(id)
        if (entity != null) {
            NetworkResult.Success(entity.toDomain())
        } else {
            NetworkResult.Error(DomainError.Database.NotFound)
        }
    } catch (e: Exception) {
        NetworkResult.Error(
            error = DomainError.Database.FetchFailed,
            exception = e
        )
    }

    override suspend fun getFoodByBarcodeLocal(barcode: String): NetworkResult<Food> = try {
        val entity = localDataSource.getFoodByBarcode(barcode)
        if (entity != null) {
            NetworkResult.Success(entity.toDomain())
        } else {
            NetworkResult.Error(DomainError.Database.BarcodeNotFound)
        }
    } catch (e: Exception) {
        NetworkResult.Error(
            error = DomainError.Database.SearchFailed,
            exception = e
        )
    }

    override suspend fun insertFood(food: Food): NetworkResult<Long> = try {
        val id = localDataSource.insertFood(food.toEntity())
        NetworkResult.Success(id)
    } catch (e: Exception) {
        NetworkResult.Error(
            error = DomainError.Database.InsertFailed,
            exception = e
        )
    }

    override suspend fun insertFoods(foods: List<Food>): NetworkResult<List<Long>> = try {
        // Фильтруем дубликаты по barcode внутри списка для предотвращения конфликтов
        val uniqueFoods = foods.distinctBy { it.barcode }
        val ids = localDataSource.insertFoods(uniqueFoods.map { it.toEntity() })
        NetworkResult.Success(ids)
    } catch (e: Exception) {
        NetworkResult.Error(
            error = DomainError.Database.InsertFailed,
            exception = e
        )
    }

    override suspend fun updateFood(food: Food): NetworkResult<Unit> = try {
        localDataSource.updateFood(food.toEntity())
        NetworkResult.Success(Unit)
    } catch (e: Exception) {
        NetworkResult.Error(
            error = DomainError.Database.UpdateFailed,
            exception = e
        )
    }

    override suspend fun deleteFood(food: Food): NetworkResult<Unit> = try {
        localDataSource.deleteFood(food.toEntity())
        NetworkResult.Success(Unit)
    } catch (e: Exception) {
        NetworkResult.Error(
            error = DomainError.Database.DeleteFailed,
            exception = e
        )
    }

    override fun searchFoods(query: String): Flow<NetworkResult<List<Food>>> =
        localDataSource.searchFoods(query)
            .map<List<com.ruslan.foodtracker.data.local.entity.FoodEntity>, NetworkResult<List<Food>>> { entities ->
                NetworkResult.Success(entities.map { it.toDomain() })
            }
            .onStart { emit(NetworkResult.Loading) }
            .catch { e ->
                emit(NetworkResult.Error(
                    error = DomainError.Database.SearchFailed,
                    exception = e
                ))
            }

    // ========== REMOTE API OPERATIONS ==========

    /**
     * Поиск продуктов по названию через Open Food Facts API с пагинацией
     * Только запрос к API и маппинг DTO -> Domain
     * Fallback логика находится в UseCase
     *
     * @param query Поисковый запрос
     * @param page Номер страницы (начинается с 1)
     */
    override fun searchFoodsByNameRemote(query: String, page: Int): Flow<NetworkResult<com.ruslan.foodtracker.domain.model.PaginatedResult<Food>>> =
        remoteDataSource.searchProducts(query, page = page, pageSize = 20)
            .mapNetworkResult { response ->
                // Маппинг SearchResponse -> PaginatedResult<Food>
                com.ruslan.foodtracker.domain.model.PaginatedResult(
                    data = response.products?.toDomainList() ?: emptyList(),
                    currentPage = response.page ?: 1,
                    totalPages = response.pageCount ?: 1,
                    pageSize = response.pageSize ?: 20,
                    totalCount = response.count ?: 0
                )
            }

    /**
     * Получение продукта по штрих-коду через Open Food Facts API
     * Только запрос к API и маппинг DTO -> Domain
     * Fallback логика находится в UseCase
     */
    override fun getFoodByBarcode(barcode: String): Flow<NetworkResult<Food>> =
        remoteDataSource.getProductByBarcode(barcode)
            .map { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        val response = result.data
                        // Проверяем успешность ответа API
                        if (response.status != 1 || response.product == null) {
                            NetworkResult.Error(DomainError.Data.ProductNotFound)
                        } else {
                            // Маппинг DTO -> Domain
                            val food = response.product.toDomain()
                            if (food != null) {
                                NetworkResult.Success(food)
                            } else {
                                NetworkResult.Error(DomainError.Data.ParseError)
                            }
                        }
                    }
                    is NetworkResult.Error -> result
                    is NetworkResult.Loading -> result
                    is NetworkResult.Empty -> result
                }
            }
}