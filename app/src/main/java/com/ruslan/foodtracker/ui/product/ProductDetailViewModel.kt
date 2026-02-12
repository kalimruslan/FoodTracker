package com.ruslan.foodtracker.ui.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.ruslan.foodtracker.ui.navigation.NavRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productId: String = savedStateHandle.toRoute<NavRoutes.ProductDetail>().productId

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    init {
        loadMockProduct(productId)
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

    private fun loadMockProduct(id: String) {
        _uiState.value = ProductDetailUiState(
            productId = id,
            name = "Куриная грудка",
            brand = "без бренда",
            caloriesPer100g = 110f,
            proteinPer100g = 23.1f,
            fatPer100g = 1.2f,
            carbsPer100g = 0f,
            fiberPer100g = 0f,
            weight = "100",
            selectedUnit = PortionUnit.GRAM,
            multiplier = 1f
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
    val error: String? = null
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
