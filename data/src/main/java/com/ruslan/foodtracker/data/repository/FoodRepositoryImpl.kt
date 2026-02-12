package com.ruslan.foodtracker.data.repository

import com.ruslan.foodtracker.data.local.dao.FoodDao
import com.ruslan.foodtracker.data.mapper.toDomain
import com.ruslan.foodtracker.data.mapper.toEntity
import com.ruslan.foodtracker.domain.model.Food
import com.ruslan.foodtracker.domain.model.NetworkResult
import com.ruslan.foodtracker.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class FoodRepositoryImpl @Inject constructor(
    private val foodDao: FoodDao
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
}
