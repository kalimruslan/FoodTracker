package com.ruslan.foodtracker.feature.product.presenter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.ruslan.foodtracker.core.navigation.NavRoutes
import com.ruslan.foodtracker.domain.model.Food
import com.ruslan.foodtracker.domain.model.FoodEntry
import com.ruslan.foodtracker.domain.model.MealType
import com.ruslan.foodtracker.domain.model.doActionIfError
import com.ruslan.foodtracker.domain.model.doActionIfLoading
import com.ruslan.foodtracker.domain.model.doActionIfSuccess
import com.ruslan.foodtracker.domain.usecase.entry.InsertFoodEntryUseCase
import com.ruslan.foodtracker.domain.usecase.food.GetFoodByIdUseCase
import com.ruslan.foodtracker.domain.usecase.food.InsertFoodUseCase
import com.ruslan.foodtracker.domain.usecase.food.SearchFoodByBarcodeUseCase
import java.time.LocalDateTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getFoodByIdUseCase: GetFoodByIdUseCase,
    private val searchFoodByBarcodeUseCase: SearchFoodByBarcodeUseCase,
    private val insertFoodUseCase: InsertFoodUseCase,
    private val insertFoodEntryUseCase: InsertFoodEntryUseCase
) : ViewModel() {

    private val productId: String = savedStateHandle.toRoute<NavRoutes.ProductDetail>().productId

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    private var currentFood: Food? = null

    init {
        loadProduct(productId)
    }

    fun onWeightChanged(weight: String) {
        val weightFloat = weight.toFloatOrNull() ?: 100f
        _uiState.value = _uiState.value.copy(
            weight = weight,
            multiplier = weightFloat / 100f
        )
    }

    fun onUnitSelected(unit: PortionUnit) {
        _uiState.value = _uiState.value.copy(selectedUnit = unit)
    }

    fun onShowMealDialog() {
        _uiState.value = _uiState.value.copy(showMealDialog = true)
    }

    fun onDismissMealDialog() {
        _uiState.value = _uiState.value.copy(showMealDialog = false)
    }

    fun onMealSelected(mealType: MealType, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val food = currentFood ?: return@launch

            // Вычисляем финальные значения с учетом порции
            val currentState = _uiState.value
            val servings = currentState.multiplier

            // Создаем FoodEntry
            val foodEntry = FoodEntry(
                id = 0, // Будет назначен БД
                foodId = food.id,
                foodName = food.name,
                servings = servings.toDouble(),
                calories = (food.calories * servings).toInt(),
                protein = food.protein * servings,
                carbs = food.carbs * servings,
                fat = food.fat * servings,
                timestamp = LocalDateTime.now(),
                mealType = mealType
            )

            // Сохраняем запись
            val result = insertFoodEntryUseCase(foodEntry)

            result.doActionIfSuccess {
                // Закрываем диалог и возвращаемся назад
                _uiState.value = _uiState.value.copy(showMealDialog = false)
                onSuccess()
            }

            result.doActionIfError {
                // Показываем ошибку
                _uiState.value = _uiState.value.copy(
                    showMealDialog = false,
                    error = "Ошибка сохранения записи"
                )
            }
        }
    }

    private fun loadProduct(id: String) {
        viewModelScope.launch {
            // Сначала пытаемся загрузить из локальной БД
            val localId = id.toLongOrNull()
            if (localId != null) {
                // Это локальный ID
                val result = getFoodByIdUseCase(localId)
                result.doActionIfSuccess { food ->
                    currentFood = food
                    updateUiStateFromFood(food)
                }
                result.doActionIfError {
                    // Если не найдено локально, пробуем загрузить по штрихкоду
                    loadFromApi(id)
                }
            } else {
                // Это штрихкод (barcode)
                loadFromApi(id)
            }
        }
    }

    private fun loadFromApi(barcode: String) {
        viewModelScope.launch {
            searchFoodByBarcodeUseCase(barcode).collect { result ->
                result.doActionIfLoading {
                    _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                }

                result.doActionIfSuccess { food ->
                    currentFood = food
                    updateUiStateFromFood(food)

                    // Сохраняем продукт в локальную БД для будущего использования
                    viewModelScope.launch {
                        insertFoodUseCase(food)
                    }
                }

                result.doActionIfError { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Ошибка загрузки продукта"
                    )
                }
            }
        }
    }

    private fun updateUiStateFromFood(food: Food) {
        _uiState.value = _uiState.value.copy(
            productId = food.barcode ?: food.id.toString(),
            name = food.name,
            brand = food.brand ?: "без бренда",
            caloriesPer100g = food.calories.toFloat(),
            proteinPer100g = food.protein.toFloat(),
            fatPer100g = food.fat.toFloat(),
            carbsPer100g = food.carbs.toFloat(),
            fiberPer100g = food.fiber.toFloat(),
            isLoading = false,
            error = null
        )
    }
}

data class ProductDetailUiState(
    val productId: String = "",
    val name: String = "",
    val brand: String = "",
    val caloriesPer100g: Float = 0f,
    val proteinPer100g: Float = 0f,
    val fatPer100g: Float = 0f,
    val carbsPer100g: Float = 0f,
    val fiberPer100g: Float = 0f,
    val weight: String = "100",
    val selectedUnit: PortionUnit = PortionUnit.GRAM,
    val multiplier: Float = 1f,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showMealDialog: Boolean = false
) {
    val calories: Float get() = caloriesPer100g * multiplier
    val protein: Float get() = proteinPer100g * multiplier
    val fat: Float get() = fatPer100g * multiplier
    val carbs: Float get() = carbsPer100g * multiplier
    val fiber: Float get() = fiberPer100g * multiplier
}

enum class PortionUnit(val label: String) {
    GRAM("г"),
    PIECE("шт"),
    PORTION("порц")
}
