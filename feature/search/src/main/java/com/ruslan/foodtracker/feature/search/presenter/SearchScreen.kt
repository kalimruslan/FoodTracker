package com.ruslan.foodtracker.feature.search.presenter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ruslan.foodtracker.core.ui.components.ProductCard
import com.ruslan.foodtracker.core.ui.components.ProductData
import com.ruslan.foodtracker.core.ui.theme.*

@Composable
fun SearchScreen(
    onNavigateToProduct: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    SearchScreenContent(
        uiState = uiState,
        onNavigateToProduct = onNavigateToProduct,
        onNavigateBack = onNavigateBack,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onTabSelected = viewModel::onTabSelected,
        onToggleFavorite = viewModel::onToggleFavorite,
        modifier = modifier
    )
}

@Composable
private fun SearchScreenContent(
    uiState: SearchUiState,
    onNavigateToProduct: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onTabSelected: (SearchTab) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
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
                    contentDescription = "Назад",
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = "Добавить продукт",
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

        // Product list
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = if (uiState.selectedTab == SearchTab.FAVORITES) {
                    uiState.products.filter { it.isFavorite }
                } else {
                    uiState.products
                },
                key = { it.id }
            ) { product ->
                ProductCard(
                    product = product,
                    onClick = { onNavigateToProduct(product.id) },
                    onFavoriteClick = { onToggleFavorite(product.id) }
                )
            }
        }
    }
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
            placeholder = { Text("Найти продукт...") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    brush = Brush.linearGradient(colors = listOf(Primary, PrimaryLight))
                )
                .clickable { /* TODO: open camera */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CameraAlt,
                contentDescription = "Сканировать штрихкод",
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
                products = listOf(
                    ProductData("1", "Куриная грудка", "без бренда", "100г", 110, 23.1f, 1.2f, 0f, false),
                    ProductData("2", "Творог 5%", "Домик в деревне", "100г", 121, 17.2f, 5.0f, 1.8f, true)
                )
            ),
            onNavigateToProduct = {},
            onNavigateBack = {},
            onSearchQueryChanged = {},
            onTabSelected = {},
            onToggleFavorite = {}
        )
    }
}
