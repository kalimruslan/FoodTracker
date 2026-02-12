package com.ruslan.foodtracker.domain.model

data class Food(
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
    val imageUrl: String? = null
)
