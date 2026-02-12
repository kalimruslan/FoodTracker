package com.ruslan.foodtracker.data.remote.mapper

import com.ruslan.foodtracker.data.remote.dto.ProductDto
import com.ruslan.foodtracker.domain.model.Food

/**
 * Маппер для конвертации ProductDto (Open Food Facts API) в Domain модель Food
 */

/**
 * Конвертирует ProductDto в Food
 *
 * @return Food domain модель или null если недостаточно данных
 */
fun ProductDto.toDomain(): Food? {
    // Проверяем обязательные поля
    val productName = this.productName ?: return null
    val nutriments = this.nutriments ?: return null

    // Извлекаем пищевую ценность (на 100g)
    val calories = nutriments.energyKcal100g?.toInt() ?: 0
    val protein = nutriments.proteins100g ?: 0.0
    val carbs = nutriments.carbohydrates100g ?: 0.0
    val fat = nutriments.fat100g ?: 0.0
    val fiber = nutriments.fiber100g ?: 0.0

    // Парсим serving size (например, "100g" -> 100.0)
    val (servingSize, servingUnit) = parseServingSize(this.servingSize)

    // Извлекаем дополнительные данные
    val barcode = this.code
    val brand = this.brands
    val imageUrl = this.imageFrontUrl ?: this.imageUrl

    return Food(
        id = 0, // ID назначается при сохранении в Room
        name = productName,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        fiber = fiber,
        servingSize = servingSize,
        servingUnit = servingUnit,
        barcode = barcode,
        brand = brand,
        imageUrl = imageUrl
    )
}

/**
 * Парсит serving size из строки формата "100g", "1 шт", "200ml"
 *
 * @param servingSizeStr Строка с размером порции
 * @return Pair(размер, единица измерения)
 */
private fun parseServingSize(servingSizeStr: String?): Pair<Double, String> {
    if (servingSizeStr.isNullOrBlank()) {
        return Pair(100.0, "г") // Значение по умолчанию
    }

    // Попытка извлечь число и единицу измерения
    val regex = """([\d.]+)\s*([a-zA-Zа-яА-Я]+)""".toRegex()
    val matchResult = regex.find(servingSizeStr)

    return if (matchResult != null) {
        val size = matchResult.groupValues[1].toDoubleOrNull() ?: 100.0
        val unit = matchResult.groupValues[2]
        Pair(size, unit)
    } else {
        // Если не удалось распарсить, используем значение по умолчанию
        Pair(100.0, "г")
    }
}

/**
 * Конвертирует список ProductDto в список Food, отфильтровывая null значения
 */
fun List<ProductDto>.toDomainList(): List<Food> {
    return this.mapNotNull { it.toDomain() }
}