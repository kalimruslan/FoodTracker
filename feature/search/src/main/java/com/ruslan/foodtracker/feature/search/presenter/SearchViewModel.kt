package com.ruslan.foodtracker.feature.search.presenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruslan.foodtracker.core.ui.components.ProductData
import com.ruslan.foodtracker.domain.model.Food
import com.ruslan.foodtracker.domain.model.NetworkResult
import com.ruslan.foodtracker.domain.usecase.food.SearchFoodsByNameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchFoodsByNameUseCase: SearchFoodsByNameUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)

        // Отменяем предыдущий поиск
        searchJob?.cancel()

        // Debounce - ждем 500ms после ввода
        searchJob = viewModelScope.launch {
            delay(500)
            if (query.length >= 2) {
                searchProducts(query)
            } else {
                _uiState.value = _uiState.value.copy(products = emptyList())
            }
        }
    }

    fun onTabSelected(tab: SearchTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
        // TODO: Load data for selected tab
    }

    fun onToggleFavorite(productId: String) {
        val products = _uiState.value.products.map { product ->
            if (product.id == productId) {
                product.copy(isFavorite = !product.isFavorite)
            } else product
        }
        _uiState.value = _uiState.value.copy(products = products)
        // TODO: Save to repository
    }

    private fun searchProducts(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = searchFoodsByNameUseCase(query)) {
                is NetworkResult.Success -> {
                    val productDataList = result.data.map { it.toProductData() }
                    _uiState.value = _uiState.value.copy(
                        products = productDataList,
                        isLoading = false,
                        error = null
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is NetworkResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
                is NetworkResult.Empty -> {
                    _uiState.value = _uiState.value.copy(
                        products = emptyList(),
                        isLoading = false,
                        error = "Продукты не найдены"
                    )
                }
            }
        }
    }
}

/**
 * Маппинг Food модели в ProductData для UI
 */
private fun Food.toProductData(): ProductData {
    return ProductData(
        id = id.toString(),
        name = name,
        brand = brand,
        portion = "${servingSize.toInt()}$servingUnit",
        calories = calories,
        protein = protein.toFloat(),
        fat = fat.toFloat(),
        carbs = carbs.toFloat(),
        isFavorite = false // TODO: Проверить в избранном
    )
}

data class SearchUiState(
    val searchQuery: String = "",
    val selectedTab: SearchTab = SearchTab.SEARCH,
    val products: List<ProductData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class SearchTab(val label: String, val icon: String) {
    SEARCH("Поиск", "🔍"),
    RECENT("Недавние", "🕐"),
    FAVORITES("Избранное", "⭐"),
    RECIPES("Рецепты", "📋")
}
