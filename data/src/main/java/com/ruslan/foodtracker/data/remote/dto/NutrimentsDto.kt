package com.ruslan.foodtracker.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO для пищевой ценности продукта из Open Food Facts API
 * Все значения указаны на 100g продукта
 */
data class NutrimentsDto(
    @SerializedName("energy-kcal_100g")
    val energyKcal100g: Double? = null,
    @SerializedName("proteins_100g")
    val proteins100g: Double? = null,
    @SerializedName("fat_100g")
    val fat100g: Double? = null,
    @SerializedName("carbohydrates_100g")
    val carbohydrates100g: Double? = null,
    @SerializedName("fiber_100g")
    val fiber100g: Double? = null,
    @SerializedName("sugars_100g")
    val sugars100g: Double? = null,
    @SerializedName("salt_100g")
    val salt100g: Double? = null
)
