package com.ruslan.foodtracker.data.repository

import com.ruslan.foodtracker.data.local.dao.FoodDao
import com.ruslan.foodtracker.data.mapper.toDomain
import com.ruslan.foodtracker.data.mapper.toEntity
import com.ruslan.foodtracker.data.remote.api.OpenFoodFactsApi
import com.ruslan.foodtracker.data.remote.mapper.toDomain
import com.ruslan.foodtracker.data.remote.mapper.toDomainList
import com.ruslan.foodtracker.domain.model.Food
import com.ruslan.foodtracker.domain.model.NetworkResult
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
     */
    override suspend fun searchFoodsByNameRemote(query: String): NetworkResult<List<Food>> {
        return try {
            // Запрос к API
            val response = api.searchProducts(searchTerms = query, pageSize = 20)

            // Проверка успешности ответа
            val products = response.products
            if (products.isNullOrEmpty()) {
                // Fallback на локальный поиск если API не вернул результатов
                val result = mutableListOf<Food>()
                foodDao.searchFoods(query).collect { entities ->
                    result.addAll(entities.map { it.toDomain() })
                }
                if (result.isEmpty()) {
                    NetworkResult.Empty
                } else {
                    NetworkResult.Success(result)
                }
            } else {
                // Конвертируем DTO в Domain модели
                val foods = products.toDomainList()

                // Кэшируем результаты в локальную БД (сохраняем только продукты с barcode)
                foods.filter { it.barcode != null }.forEach { food ->
                    try {
                        foodDao.insertFood(food.toEntity())
                    } catch (e: Exception) {
                        // Игнорируем ошибки вставки (например, дубликаты)
                    }
                }

                NetworkResult.Success(foods)
            }
        } catch (e: Exception) {
            // При ошибке сети fallback на локальный кэш
            try {
                val localFoods = mutableListOf<Food>()
                foodDao.searchFoods(query).collect { entities ->
                    localFoods.addAll(entities.map { it.toDomain() })
                }
                if (localFoods.isNotEmpty()) {
                    NetworkResult.Success(localFoods)
                } else {
                    NetworkResult.Error(
                        message = "Нет подключения к интернету и нет кэшированных данных",
                        exception = e
                    )
                }
            } catch (localError: Exception) {
                NetworkResult.Error(
                    message = e.message ?: "Ошибка при поиске продуктов",
                    exception = e
                )
            }
        }
    }

    /**
     * Получение продукта по штрих-коду через Open Food Facts API
     * Результат кэшируется в локальную БД
     */
    override suspend fun getFoodByBarcode(barcode: String): NetworkResult<Food> {
        return try {
            // Запрос к API
            val response = api.getProductByBarcode(barcode)

            // Проверка успешности ответа
            if (response.status != 1 || response.product == null) {
                // Fallback на локальный поиск по barcode
                var foundFood: Food? = null
                foodDao.getAllFoods().collect { entities ->
                    foundFood = entities.find { it.barcode == barcode }?.toDomain()
                }
                if (foundFood != null) {
                    NetworkResult.Success(foundFood!!)
                } else {
                    NetworkResult.Error("Продукт с таким штрих-кодом не найден")
                }
            } else {
                // Конвертируем DTO в Domain модель
                val food = response.product.toDomain()
                if (food == null) {
                    NetworkResult.Error("Не удалось обработать данные продукта")
                } else {
                    // Кэшируем в локальную БД
                    try {
                        foodDao.insertFood(food.toEntity())
                    } catch (e: Exception) {
                        // Игнорируем ошибки вставки (например, дубликаты)
                    }

                    NetworkResult.Success(food)
                }
            }
        } catch (e: Exception) {
            // При ошибке сети fallback на локальный кэш
            try {
                var foundFood: Food? = null
                foodDao.getAllFoods().collect { entities ->
                    foundFood = entities.find { it.barcode == barcode }?.toDomain()
                }
                if (foundFood != null) {
                    NetworkResult.Success(foundFood!!)
                } else {
                    NetworkResult.Error(
                        message = "Нет подключения к интернету и продукт не найден в кэше",
                        exception = e
                    )
                }
            } catch (localError: Exception) {
                NetworkResult.Error(
                    message = e.message ?: "Ошибка при получении продукта",
                    exception = e
                )
            }
        }
    }
}
