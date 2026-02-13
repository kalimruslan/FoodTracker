package com.ruslan.foodtracker.data.source.local

import com.ruslan.foodtracker.data.local.dao.FoodDao
import com.ruslan.foodtracker.data.local.entity.FoodEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Local data source для работы с БД через Room
 * Возвращает Entity модели (без маппинга в Domain)
 */
class FoodLocalDataSource @Inject constructor(
    private val foodDao: FoodDao
) {
    /**
     * Получить все продукты из БД
     */
    fun getAllFoods(): Flow<List<FoodEntity>> = foodDao.getAllFoods()

    /**
     * Получить продукт по ID
     */
    suspend fun getFoodById(id: Long): FoodEntity? = foodDao.getFoodById(id)

    /**
     * Вставить продукт в БД
     * @return ID вставленного продукта
     */
    suspend fun insertFood(food: FoodEntity): Long = foodDao.insertFood(food)

    /**
     * Обновить продукт в БД
     */
    suspend fun updateFood(food: FoodEntity) = foodDao.updateFood(food)

    /**
     * Удалить продукт из БД
     */
    suspend fun deleteFood(food: FoodEntity) = foodDao.deleteFood(food)

    /**
     * Поиск продуктов по названию в локальной БД
     */
    fun searchFoods(query: String): Flow<List<FoodEntity>> = foodDao.searchFoods(query)
}