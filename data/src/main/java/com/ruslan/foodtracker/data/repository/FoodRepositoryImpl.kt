package com.ruslan.foodtracker.data.repository

import com.ruslan.foodtracker.data.mapper.toDomain
import com.ruslan.foodtracker.data.mapper.toEntity
import com.ruslan.foodtracker.data.remote.mapper.toDomain
import com.ruslan.foodtracker.data.remote.mapper.toDomainList
import com.ruslan.foodtracker.data.source.local.FoodLocalDataSource
import com.ruslan.foodtracker.data.source.remote.FoodRemoteDataSource
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
                    message = e.message ?: "Ошибка при загрузке продуктов",
                    exception = e
                ))
            }

    override suspend fun getFoodById(id: Long): NetworkResult<Food> = try {
        val entity = localDataSource.getFoodById(id)
        if (entity != null) {
            NetworkResult.Success(entity.toDomain())
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
        val id = localDataSource.insertFood(food.toEntity())
        NetworkResult.Success(id)
    } catch (e: Exception) {
        NetworkResult.Error(
            message = e.message ?: "Ошибка при добавлении продукта",
            exception = e
        )
    }

    override suspend fun updateFood(food: Food): NetworkResult<Unit> = try {
        localDataSource.updateFood(food.toEntity())
        NetworkResult.Success(Unit)
    } catch (e: Exception) {
        NetworkResult.Error(
            message = e.message ?: "Ошибка при обновлении продукта",
            exception = e
        )
    }

    override suspend fun deleteFood(food: Food): NetworkResult<Unit> = try {
        localDataSource.deleteFood(food.toEntity())
        NetworkResult.Success(Unit)
    } catch (e: Exception) {
        NetworkResult.Error(
            message = e.message ?: "Ошибка при удалении продукта",
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
                    message = e.message ?: "Ошибка при поиске продуктов",
                    exception = e
                ))
            }

    // ========== REMOTE API OPERATIONS ==========

    /**
     * Поиск продуктов по названию через Open Food Facts API
     * Только запрос к API и маппинг DTO -> Domain
     * Fallback логика находится в UseCase
     */
    override fun searchFoodsByNameRemote(query: String): Flow<NetworkResult<List<Food>>> =
        remoteDataSource.searchProducts(query, pageSize = 20)
            .mapNetworkResult { response ->
                // Маппинг DTO -> Domain
                response.products?.toDomainList() ?: emptyList()
            }

    /**
     * Получение продукта по штрих-коду через Open Food Facts API
     * Только запрос к API и маппинг DTO -> Domain
     * Fallback логика находится в UseCase
     */
    override fun getFoodByBarcode(barcode: String): Flow<NetworkResult<Food>> =
        remoteDataSource.getProductByBarcode(barcode)
            .mapNetworkResult { response ->
                // Проверяем успешность ответа API
                if (response.status != 1 || response.product == null) {
                    throw IllegalStateException("Продукт не найден")
                }

                // Маппинг DTO -> Domain
                response.product.toDomain()
                    ?: throw IllegalStateException("Не удалось обработать данные продукта")
            }
}