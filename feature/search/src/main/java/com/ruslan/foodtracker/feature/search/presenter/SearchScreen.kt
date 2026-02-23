package com.ruslan.foodtracker.feature.search.presenter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ruslan.foodtracker.core.ui.components.ErrorAlert
import com.ruslan.foodtracker.core.ui.components.LoadingIndicator
import com.ruslan.foodtracker.core.ui.components.ProductCard
import com.ruslan.foodtracker.core.ui.components.ProductData
import com.ruslan.foodtracker.core.ui.error.toStringRes
import com.ruslan.foodtracker.core.ui.theme.*
import com.ruslan.foodtracker.domain.error.DomainError
import com.ruslan.foodtracker.feature.search.R

@Composable
fun SearchScreen(
    onProductClick: (ProductData) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    SearchScreenContent(
        uiState = uiState,
        onProductClick = onProductClick,
        onNavigateBack = onNavigateBack,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onTabSelected = viewModel::onTabSelected,
        onToggleFavorite = viewModel::onToggleFavorite,
        onLoadNextPage = viewModel::loadNextPage,
        onRetrySearch = viewModel::onRetrySearch,
        onDismissError = viewModel::onDismissError,
        onRetryPagination = viewModel::onRetryPagination,
        onDismissPaginationError = viewModel::onDismissPaginationError,
        modifier = modifier
    )
}

@Composable
private fun SearchScreenContent(
    uiState: SearchUiState,
    onProductClick: (ProductData) -> Unit,
    onNavigateBack: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onTabSelected: (SearchTab) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onLoadNextPage: () -> Unit,
    onRetrySearch: () -> Unit,
    onDismissError: () -> Unit,
    onRetryPagination: () -> Unit,
    onDismissPaginationError: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Показываем ErrorAlert при наличии ошибки
    if (uiState.error != null) {
        ErrorAlert(
            message = stringResource(uiState.error.toStringRes()),
            title = stringResource(R.string.search_error_title),
            onRetry = onRetrySearch,
            onDismiss = onDismissError,
            retryText = stringResource(R.string.search_error_retry),
            dismissText = stringResource(R.string.search_error_dismiss)
        )
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.search_back_button),
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = stringResource(R.string.search_screen_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
        }

        // Search bar
        SearchBar(
            query = uiState.searchQuery,
            onQueryChanged = onSearchQueryChanged,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Tabs
        TabsRow(
            selectedTab = uiState.selectedTab,
            onTabSelected = onTabSelected,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Content area (list, loading, empty state)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when {
                // Первая загрузка - показываем LoadingIndicator
                uiState.isLoading && uiState.products.isEmpty() -> {
                    LoadingIndicator(
                        text = stringResource(R.string.search_loading),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                // Пустой запрос - подсказка
                uiState.searchQuery.length < 2 && uiState.products.isEmpty() -> {
                    EmptyStateMessage(
                        text = stringResource(R.string.search_empty_state),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                // Нет результатов
                !uiState.isLoading && uiState.products.isEmpty() && uiState.error == null -> {
                    EmptyStateMessage(
                        text = stringResource(R.string.search_no_results),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                // Есть продукты - показываем список
                else -> {
                    ProductList(
                        uiState = uiState,
                        onProductClick = onProductClick,
                        onToggleFavorite = onToggleFavorite,
                        onLoadNextPage = onLoadNextPage,
                        onRetryPagination = onRetryPagination,
                        onDismissPaginationError = onDismissPaginationError
                    )
                }
            }
        }
    }
}

/**
 * Список продуктов с пагинацией
 */
@Composable
private fun ProductList(
    uiState: SearchUiState,
    onProductClick: (ProductData) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onLoadNextPage: () -> Unit,
    onRetryPagination: () -> Unit,
    onDismissPaginationError: () -> Unit
) {
    val listState = rememberLazyListState()

    // Определяем, нужно ли загружать следующую страницу
    val shouldLoadMore = remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

            // Загружаем за 3 элемента до конца
            lastVisibleItemIndex > (totalItemsNumber - 3)
        }
    }

    // Автоматическая загрузка при скролле
    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && uiState.hasNextPage && !uiState.isLoadingMore) {
            onLoadNextPage()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Индикатор "из кэша"
        if (uiState.isFromCache) {
            CacheInfoBanner()
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val displayProducts = if (uiState.selectedTab == SearchTab.FAVORITES) {
                uiState.products.filter { it.isFavorite }
            } else {
                uiState.products
            }

            items(
                items = displayProducts,
                key = { it.id }
            ) { product ->
                ProductCard(
                    product = product,
                    onClick = { onProductClick(product) },
                    onFavoriteClick = { onToggleFavorite(product.id) }
                )
            }

            // Footer - состояния пагинации
            item {
                when {
                    // Загрузка следующей страницы
                    uiState.isLoadingMore -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    // Ошибка при загрузке следующей страницы
                    uiState.paginationError != null -> {
                        PaginationErrorItem(
                            error = uiState.paginationError,
                            onRetry = onRetryPagination,
                            onDismiss = onDismissPaginationError
                        )
                    }
                    // Конец результатов
                    !uiState.hasNextPage && uiState.products.isNotEmpty() && !uiState.isFromCache -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.search_end_of_results),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Баннер информации о результатах из кэша
 */
@Composable
private fun CacheInfoBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = stringResource(R.string.search_from_cache),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

/**
 * Элемент ошибки пагинации (inline в списке)
 */
@Composable
private fun PaginationErrorItem(
    error: DomainError,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(error.toStringRes()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(stringResource(R.string.search_error_dismiss))
                }
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(R.string.search_pagination_retry))
                }
            }
        }
    }
}

/**
 * Сообщение о пустом состоянии
 */
@Composable
private fun EmptyStateMessage(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier.padding(32.dp)
    )
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF3F4F6))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )

        TextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(R.string.search_placeholder)) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )

        val cameraButtonBrush = remember { Brush.linearGradient(colors = listOf(Primary, PrimaryLight)) }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(brush = cameraButtonBrush)
                .clickable { /* TODO: open camera */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CameraAlt,
                contentDescription = stringResource(R.string.search_barcode_scan),
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun TabsRow(
    selectedTab: SearchTab,
    onTabSelected: (SearchTab) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(SearchTab.entries.toTypedArray()) { tab ->
            TabChip(
                text = "${tab.icon} ${tab.label}",
                isSelected = selectedTab == tab,
                onClick = { onTabSelected(tab) }
            )
        }
    }
}

@Composable
private fun TabChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Primary else Color(0xFFF3F4F6))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenPreview() {
    FoodTrackerTheme {
        SearchScreenContent(
            uiState = SearchUiState(
                searchQuery = "курица",
                products = listOf(
                    ProductData("1", "Куриная грудка", "без бренда", "100г", 110, 23.1f, 1.2f, 0f, false),
                    ProductData("2", "Творог 5%", "Домик в деревне", "100г", 121, 17.2f, 5.0f, 1.8f, true)
                ),
                hasNextPage = true
            ),
            onProductClick = {},
            onNavigateBack = {},
            onSearchQueryChanged = {},
            onTabSelected = {},
            onToggleFavorite = {},
            onLoadNextPage = {},
            onRetrySearch = {},
            onDismissError = {},
            onRetryPagination = {},
            onDismissPaginationError = {}
        )
    }
}
