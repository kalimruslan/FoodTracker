package com.ruslan.foodtracker.feature.search.presenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruslan.foodtracker.core.ui.components.ProductData
import com.ruslan.foodtracker.domain.error.DomainError
import com.ruslan.foodtracker.domain.model.Food
import com.ruslan.foodtracker.domain.model.doActionIfError
import com.ruslan.foodtracker.domain.model.doActionIfLoading
import com.ruslan.foodtracker.domain.model.doActionIfSuccess
import com.ruslan.foodtracker.domain.usecase.food.SearchFoodsByNameUseCase
import com.ruslan.foodtracker.domain.usecase.food.ToggleFavoriteFoodUseCase
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
    private val searchFoodsByNameUseCase: SearchFoodsByNameUseCase,
    private val toggleFavoriteFoodUseCase: ToggleFavoriteFoodUseCase
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
                searchProducts(query, page = 1, isInitialSearch = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    products = emptyList(),
                    isLoading = false,
                    error = null,
                    currentPage = 1,
                    hasNextPage = false,
                    isFromCache = false
                )
            }
        }
    }

    fun onTabSelected(tab: SearchTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
        // TODO: Load data for selected tab
    }

    fun onToggleFavorite(productId: String) {
        val products = _uiState.value.products
        val product = products.find { it.id == productId } ?: return
        val newFavorite = !product.isFavorite

        // Оптимистичное обновление UI
        _uiState.value = _uiState.value.copy(
            products = products.map {
                if (it.id == productId) it.copy(isFavorite = newFavorite) else it
            }
        )

        // Сохраняем в БД только если продукт уже есть локально
        if (product.localFoodId != 0L) {
            viewModelScope.launch {
                val result = toggleFavoriteFoodUseCase(product.localFoodId, newFavorite)
                result.doActionIfError {
                    // Откатываем при ошибке
                    _uiState.value = _uiState.value.copy(
                        products = _uiState.value.products.map {
                            if (it.id == productId) it.copy(isFavorite = !newFavorite) else it
                        }
                    )
                }
            }
        }
    }

    /**
     * Загрузка следующей страницы результатов
     */
    fun loadNextPage() {
        val currentState = _uiState.value

        // Проверки перед загрузкой
        if (currentState.isLoadingMore || !currentState.hasNextPage || currentState.isFromCache) {
            return
        }

        val query = currentState.searchQuery
        if (query.length < 2) {
            return
        }

        searchProducts(query, page = currentState.currentPage + 1, isInitialSearch = false)
    }

    /**
     * Повторить поиск при ошибке
     */
    fun onRetrySearch() {
        val query = _uiState.value.searchQuery
        if (query.length >= 2) {
            searchProducts(query, page = 1, isInitialSearch = true)
        }
    }

    /**
     * Закрыть диалог с ошибкой
     */
    fun onDismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Повторить загрузку следующей страницы при ошибке пагинации
     */
    fun onRetryPagination() {
        val currentState = _uiState.value
        val query = currentState.searchQuery

        if (query.length >= 2 && currentState.paginationError != null) {
            // Очищаем ошибку пагинации и пробуем загрузить ту же страницу снова
            _uiState.value = _uiState.value.copy(paginationError = null)
            searchProducts(query, page = currentState.currentPage + 1, isInitialSearch = false)
        }
    }

    /**
     * Закрыть ошибку пагинации
     */
    fun onDismissPaginationError() {
        _uiState.value = _uiState.value.copy(paginationError = null)
    }

    private fun searchProducts(query: String, page: Int, isInitialSearch: Boolean) {
        viewModelScope.launch {
            searchFoodsByNameUseCase(query, page).collect { result ->
                // Обработка состояния Loading
                result.doActionIfLoading {
                    if (isInitialSearch) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            error = null,
                            isFromCache = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoadingMore = true,
                            paginationError = null
                        )
                    }
                }

                // Обработка успешного результата
                result.doActionIfSuccess { paginatedResult ->
                    val productDataList = paginatedResult.data.map { it.toProductData() }

                    // Проверяем, из кэша ли результаты (одна страница и totalPages == 1)
                    val isFromCache = paginatedResult.totalPages == 1 && paginatedResult.totalCount == paginatedResult.data.size

                    if (isInitialSearch) {
                        // Первая загрузка - заменяем список
                        _uiState.value = _uiState.value.copy(
                            products = productDataList,
                            isLoading = false,
                            error = null,
                            currentPage = paginatedResult.currentPage,
                            hasNextPage = paginatedResult.hasNextPage,
                            isFromCache = isFromCache
                        )
                    } else {
                        // Загрузка следующей страницы - добавляем в конец списка
                        _uiState.value = _uiState.value.copy(
                            products = _uiState.value.products + productDataList,
                            isLoadingMore = false,
                            paginationError = null,
                            currentPage = paginatedResult.currentPage,
                            hasNextPage = paginatedResult.hasNextPage
                        )
                    }
                }

                // Обработка ошибки
                result.doActionIfError { domainError ->
                    if (isInitialSearch) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = domainError,
                            isFromCache = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoadingMore = false,
                            paginationError = domainError
                        )
                    }
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
        id = barcode ?: id.toString(),
        name = name,
        brand = brand,
        portion = "${servingSize.toInt()}$servingUnit",
        calories = calories,
        protein = protein.toFloat(),
        fat = fat.toFloat(),
        carbs = carbs.toFloat(),
        isFavorite = isFavorite,
        localFoodId = id,
        servingSizeGrams = servingSize,
        servingUnit = servingUnit
    )
}

data class SearchUiState(
    val searchQuery: String = "",
    val selectedTab: SearchTab = SearchTab.SEARCH,
    val products: List<ProductData> = emptyList(),
    val isLoading: Boolean = false,
    val error: DomainError? = null,
    // Pagination fields
    val currentPage: Int = 1,
    val hasNextPage: Boolean = false,
    val isLoadingMore: Boolean = false,
    val paginationError: DomainError? = null,
    val isFromCache: Boolean = false // Индикатор, что результаты из локального кэша
)

enum class SearchTab(val label: String, val icon: String) {
    SEARCH("Поиск", "🔍"),
    RECENT("Недавние", "🕐"),
    FAVORITES("Избранное", "⭐"),
    RECIPES("Рецепты", "📋")
}
