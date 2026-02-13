package com.ruslan.foodtracker.domain.usecase.food

import com.ruslan.foodtracker.domain.model.Food
import com.ruslan.foodtracker.domain.model.NetworkResult
import com.ruslan.foodtracker.domain.model.andThen
import com.ruslan.foodtracker.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use Case для поиска продукта по штрих-коду через Open Food Facts API
 *
 * Выполняет remote-first поиск с кэшированием и fallback:
 * 1. Поиск через API по barcode
 * 2. Кэширование успешного результата в БД (бизнес-логика)
 * 3. При ошибке сети - fallback на локальный поиск по barcode (бизнес-логика)
 */
class SearchFoodByBarcodeUseCase @Inject constructor(
    private val repository: FoodRepository
) {
    /**
     * Поиск продукта по штрих-коду
     *
     * @param barcode Штрих-код продукта (EAN-13, EAN-8, UPC-A и т.д.)
     * @return Flow<NetworkResult> с найденным продуктом
     */
    operator fun invoke(barcode: String): Flow<NetworkResult<Food>> {
        // Валидация штрих-кода
        if (barcode.isBlank()) {
            return flowOf(NetworkResult.Error("Штрих-код не может быть пустым"))
        }

        // Проверка формата (базовая валидация длины)
        val cleanBarcode = barcode.trim()
        if (cleanBarcode.length !in 8..13) {
            return flowOf(NetworkResult.Error("Некорректный формат штрих-кода (должен быть 8-13 символов)"))
        }

        // Проверка что штрих-код содержит только цифры
        if (!cleanBarcode.all { it.isDigit() }) {
            return flowOf(NetworkResult.Error("Штрих-код должен содержать только цифры"))
        }

        // Remote-first поиск с кэшированием и fallback
        return repository.getFoodByBarcode(cleanBarcode)
            .andThen { food ->
                // Бизнес-логика кэширования (сохраняем найденный продукт)
                // Игнорируем ошибки (дубликаты и т.д.)
                repository.insertFood(food)

                // Возвращаем найденный продукт
                flowOf(NetworkResult.Success(food))
            }
            .catch { exception ->
                // Бизнес-логика fallback: при ошибке сети ищем в локальной БД по barcode
                emitAll(
                    repository.getAllFoods()
                        .map { result ->
                            when (result) {
                                is NetworkResult.Success -> {
                                    val foundFood = result.data.find { it.barcode == cleanBarcode }
                                    if (foundFood != null) {
                                        NetworkResult.Success(foundFood)
                                    } else {
                                        NetworkResult.Error(
                                            message = "Продукт не найден ни в API, ни в локальном кэше",
                                            exception = exception
                                        )
                                    }
                                }
                                is NetworkResult.Error -> {
                                    NetworkResult.Error(
                                        message = "Продукт не найден ни в API, ни в локальном кэше",
                                        exception = exception
                                    )
                                }
                                is NetworkResult.Loading -> NetworkResult.Loading
                                is NetworkResult.Empty -> NetworkResult.Error(
                                    message = "Продукт не найден ни в API, ни в локальном кэше",
                                    exception = exception
                                )
                            }
                        }
                )
            }
    }
}