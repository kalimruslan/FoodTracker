package com.ruslan.foodtracker.domain.usecase.food

import com.ruslan.foodtracker.domain.model.Food
import com.ruslan.foodtracker.domain.model.NetworkResult
import com.ruslan.foodtracker.domain.repository.FoodRepository
import javax.inject.Inject

/**
 * Use Case для поиска продукта по штрих-коду через Open Food Facts API
 *
 * Выполняет remote-first поиск:
 * 1. Поиск через API по barcode
 * 2. Кэширование результата
 * 3. Fallback на локальный кэш при ошибке сети
 */
class SearchFoodByBarcodeUseCase @Inject constructor(
    private val repository: FoodRepository
) {
    /**
     * Поиск продукта по штрих-коду
     *
     * @param barcode Штрих-код продукта (EAN-13, EAN-8, UPC-A и т.д.)
     * @return NetworkResult с найденным продуктом
     */
    suspend operator fun invoke(barcode: String): NetworkResult<Food> {
        // Валидация штрих-кода
        if (barcode.isBlank()) {
            return NetworkResult.Error("Штрих-код не может быть пустым")
        }

        // Проверка формата (базовая валидация длины)
        val cleanBarcode = barcode.trim()
        if (cleanBarcode.length !in 8..13) {
            return NetworkResult.Error("Некорректный формат штрих-кода (должен быть 8-13 символов)")
        }

        // Проверка что штрих-код содержит только цифры
        if (!cleanBarcode.all { it.isDigit() }) {
            return NetworkResult.Error("Штрих-код должен содержать только цифры")
        }

        return repository.getFoodByBarcode(cleanBarcode)
    }
}