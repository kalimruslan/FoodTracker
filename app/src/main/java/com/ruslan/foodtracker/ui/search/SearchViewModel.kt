package com.ruslan.foodtracker.ui.search

import androidx.lifecycle.ViewModel
import com.ruslan.foodtracker.core.ui.components.ProductData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        loadMockData()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        // TODO: Search products
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

    private fun loadMockData() {
        _uiState.value = SearchUiState(
            products = listOf(
                ProductData("1", "Куриная грудка", "без бренда", "100г", 110, 23.1f, 1.2f, 0f, false),
                ProductData("2", "Рис бурый варёный", "без бренда", "100г", 120, 2.6f, 0.8f, 24.7f, false),
                ProductData("3", "Гречневая каша", "без бренда", "100г", 132, 4.5f, 2.3f, 25.0f, false),
                ProductData("4", "Творог 5%", "Домик в деревне", "100г", 121, 17.2f, 5.0f, 1.8f, true),
                ProductData("5", "Яйцо куриное", "без бренда", "100г", 155, 12.6f, 10.6f, 0.7f, false),
                ProductData("6", "Овсянка на воде", "без бренда", "100г", 88, 3.0f, 1.7f, 15.0f, false)
            )
        )
    }
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
