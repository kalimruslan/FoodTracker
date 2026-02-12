package com.ruslan.foodtracker.data.repository

import com.ruslan.foodtracker.data.local.dao.FoodDao
import com.ruslan.foodtracker.data.mapper.toDomain
import com.ruslan.foodtracker.data.mapper.toEntity
import com.ruslan.foodtracker.data.remote.api.OpenFoodFactsApi
import com.ruslan.foodtracker.data.remote.api.handleApi
import com.ruslan.foodtracker.data.remote.mapper.toDomain
import com.ruslan.foodtracker.data.remote.mapper.toDomainList
import com.ruslan.foodtracker.domain.model.Food
import com.ruslan.foodtracker.domain.model.NetworkResult
import com.ruslan.foodtracker.domain.model.mapNetworkResult
import com.ruslan.foodtracker.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class FoodRepositoryImpl @Inject constructor(
    private val foodDao: FoodDao,
    private val api: OpenFoodFactsApi
) : FoodRepository {

    override fun getAllFoods(): Flow<NetworkResult<List<Food>>> =
        foodDao.getAllFoods()
            .map<List<com.ruslan.foodtracker.data.local.entity.FoodEntity>, NetworkResult<List<Food>>> { entities ->
                NetworkResult.Success(entities.map { it.toDomain() })
            }
            .onStart { emit(NetworkResult.Loading) }
            .catch { e ->
                emit(NetworkResult.Error(
                    message = e.message ?: "Ошибка при загрузке продуктов",
                    exception = e
                ))
            }

    override suspend fun getFoodById(id: Long): NetworkResult<Food> = try {
        val food = foodDao.getFoodById(id)?.toDomain()
        if (food != null) {
            NetworkResult.Success(food)
        } else {
            NetworkResult.Error("Продукт не найден")
        }
    } catch (e: Exception) {
        NetworkResult.Error(
            message = e.message ?: "Ошибка при загрузке продукта",
            exception = e
        )
    }

    override suspend fun insertFood(food: Food): NetworkResult<Long> = try {
        val id = foodDao.insertFood(food.toEntity())
        NetworkResult.Success(id)
    } catch (e: Exception) {
        NetworkResult.Error(
            message = e.message ?: "Ошибка при добавлении продукта",
            exception = e
        )
    }

    override suspend fun updateFood(food: Food): NetworkResult<Unit> = try {
        foodDao.updateFood(food.toEntity())
        NetworkResult.Success(Unit)
    } catch (e: Exception) {
        NetworkResult.Error(
            message = e.message ?: "Ошибка при обновлении продукта",
            exception = e
        )
    }

    override suspend fun deleteFood(food: Food): NetworkResult<Unit> = try {
        foodDao.deleteFood(food.toEntity())
        NetworkResult.Success(Unit)
    } catch (e: Exception) {
        NetworkResult.Error(
            message = e.message ?: "Ошибка при удалении продукта",
            exception = e
        )
    }

    override fun searchFoods(query: String): Flow<NetworkResult<List<Food>>> =
        foodDao.searchFoods(query)
            .map<List<com.ruslan.foodtracker.data.local.entity.FoodEntity>, NetworkResult<List<Food>>> { entities ->
                NetworkResult.Success(entities.map { it.toDomain() })
            }
            .onStart { emit(NetworkResult.Loading) }
            .catch { e ->
                emit(NetworkResult.Error(
                    message = e.message ?: "Ошибка при поиске продуктов",
                    exception = e
                ))
            }

    /**
     * Поиск продуктов по названию через Open Food Facts API (remote-first)
     * Результаты кэшируются в локальную БД
     *
     * Использует handleApi для автоматической обработки состояний и ошибок
     */
    override fun searchFoodsByNameRemote(query: String): Flow<NetworkResult<List<Food>>> =
        handleApi { api.searchProducts(searchTerms = query, pageSize = 20) }
            .mapNetworkResult { response ->
                // Маппинг DTO -> Domain
                response.products?.toDomainList() ?: emptyList()
            }
            .map { result ->
                // Кэшируем успешные результаты в Room
                if (result is NetworkResult.Success) {
                    result.data
                        .filter { it.barcode != null }
                        .forEach { food ->
                            try {
                                foodDao.insertFood(food.toEntity())
                            } catch (e: Exception) {
                                // Игнорируем ошибки вставки (дубликаты)
                            }
                        }
                }
                result
            }
            .catch { exception ->
                // Fallback на локальный кэш при ошибке сети
                val localFoods = mutableListOf<Food>()
                foodDao.searchFoods(query).collect { entities ->
                    localFoods.addAll(entities.map { it.toDomain() })
                }

                if (localFoods.isNotEmpty()) {
                    emit(NetworkResult.Success(localFoods))
                } else {
                    emit(NetworkResult.Error(
                        message = "Нет подключения к интернету и нет кэшированных данных",
                        exception = exception
                    ))
                }
            }

    /**
     * Получение продукта по штрих-коду через Open Food Facts API
     * Результат кэшируется в локальную БД
     *
     * Использует handleApi для автоматической обработки состояний и ошибок
     */
    override fun getFoodByBarcode(barcode: String): Flow<NetworkResult<Food>> =
        handleApi { api.getProductByBarcode(barcode) }
            .mapNetworkResult { response ->
                // Проверяем успешность ответа API
                if (response.status != 1 || response.product == null) {
                    throw IllegalStateException("Продукт не найден")
                }

                // Маппинг DTO -> Domain
                response.product.toDomain()
                    ?: throw IllegalStateException("Не удалось обработать данные продукта")
            }
            .map { result ->
                // Кэшируем успешный результат в Room
                if (result is NetworkResult.Success) {
                    try {
                        foodDao.insertFood(result.data.toEntity())
                    } catch (e: Exception) {
                        // Игнорируем ошибки вставки (дубликаты)
                    }
                }
                result
            }
            .catch { exception ->
                // Fallback на локальный поиск по barcode
                var foundFood: Food? = null
                foodDao.getAllFoods().collect { entities ->
                    foundFood = entities.find { it.barcode == barcode }?.toDomain()
                }

                if (foundFood != null) {
                    emit(NetworkResult.Success(foundFood!!))
                } else {
                    emit(NetworkResult.Error(
                        message = "Продукт не найден ни в API, ни в локальном кэше",
                        exception = exception
                    ))
                }
            }
}
