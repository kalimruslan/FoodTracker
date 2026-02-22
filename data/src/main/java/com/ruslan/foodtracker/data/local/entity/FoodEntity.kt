package com.ruslan.foodtracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity для хранения продуктов в локальной БД
 *
 * Индексы:
 * - barcode (UNIQUE) - предотвращает дубликаты продуктов из API
 *   NULL значения разрешены (для продуктов созданных вручную)
 */
@Entity(
    tableName = "foods",
    indices = [Index(value = ["barcode"], unique = true)]
)
data class FoodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double = 0.0,
    val servingSize: Double,
    val servingUnit: String,
    val barcode: String? = null,
    val brand: String? = null,
    val imageUrl: String? = null,
    val isFavorite: Boolean = false
)
