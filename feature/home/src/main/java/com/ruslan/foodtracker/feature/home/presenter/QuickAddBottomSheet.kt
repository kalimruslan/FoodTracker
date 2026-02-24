package com.ruslan.foodtracker.feature.home.presenter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruslan.foodtracker.core.ui.theme.FoodTrackerTheme
import com.ruslan.foodtracker.domain.model.Food
import com.ruslan.foodtracker.domain.model.FoodEntry
import com.ruslan.foodtracker.domain.model.MealType
import java.time.LocalDateTime

/**
 * Bottom sheet для быстрого добавления продуктов в приём пищи.
 * Поддерживает два шага: выбор продукта и ввод граммажа.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddBottomSheet(
    uiState: QuickAddUiState,
    onFoodSelected: (Food) -> Unit,
    onRecentEntrySelected: (FoodEntry) -> Unit,
    onAmountChanged: (String) -> Unit,
    onMealTypeChanged: (MealType) -> Unit,
    onTabSelected: (QuickAddTab) -> Unit,
    onBackToSelection: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!uiState.isVisible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        when (uiState.step) {
            QuickAddStep.FOOD_SELECTION -> FoodSelectionStep(
                uiState = uiState,
                onFoodSelected = onFoodSelected,
                onRecentEntrySelected = onRecentEntrySelected,
                onTabSelected = onTabSelected,
                onDismiss = onDismiss,
                onNavigateToSearch = onNavigateToSearch,
            )
            QuickAddStep.AMOUNT_INPUT -> AmountInputStep(
                uiState = uiState,
                onAmountChanged = onAmountChanged,
                onMealTypeChanged = onMealTypeChanged,
                onBackToSelection = onBackToSelection,
                onSave = onSave,
                onDismiss = onDismiss,
            )
        }
    }
}

@Composable
private fun FoodSelectionStep(
    uiState: QuickAddUiState,
    onFoodSelected: (Food) -> Unit,
    onRecentEntrySelected: (FoodEntry) -> Unit,
    onTabSelected: (QuickAddTab) -> Unit,
    onDismiss: () -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Заголовок с названием MealType и кнопкой закрытия
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Добавить в ${uiState.selectedMealType.displayName()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Закрыть",
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // TabRow: Избранное / Недавние
        val tabs = QuickAddTab.entries
        TabRow(selectedTabIndex = tabs.indexOf(uiState.selectedTab)) {
            tabs.forEach { tab ->
                Tab(
                    selected = uiState.selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    text = { Text(text = tab.label) },
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Список продуктов
        val listContent = when (uiState.selectedTab) {
            QuickAddTab.FAVORITES -> if (uiState.favoriteFoods.isEmpty()) null else uiState.favoriteFoods
            QuickAddTab.RECENT -> null // handled separately
        }

        when (uiState.selectedTab) {
            QuickAddTab.FAVORITES -> {
                if (uiState.favoriteFoods.isEmpty()) {
                    EmptyState(
                        text = "Нет избранных продуктов.\nДобавьте продукты в избранное на экране продукта.",
                    )
                } else {
                    LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                        items(uiState.favoriteFoods, key = { it.id }) { food ->
                            FoodListItem(
                                name = food.name,
                                calories = food.calories,
                                servingInfo = "${food.servingSize.toInt()}${food.servingUnit}",
                                onClick = { onFoodSelected(food) },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
            QuickAddTab.RECENT -> {
                if (uiState.isLoadingRecent) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Загрузка...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else if (uiState.recentEntries.isEmpty()) {
                    EmptyState(text = "Нет недавних продуктов.\nДобавьте первый приём пищи!")
                } else {
                    LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                        items(uiState.recentEntries, key = { it.id }) { entry ->
                            FoodListItem(
                                name = entry.foodName,
                                calories = (entry.calories.toDouble() / entry.amountGrams.coerceAtLeast(1.0) * 100).toInt(),
                                servingInfo = "100г",
                                onClick = { onRecentEntrySelected(entry) },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Кнопка "Открыть полный поиск"
        TextButton(
            onClick = onNavigateToSearch,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Открыть полный поиск →")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun FoodListItem(
    name: String,
    calories: Int,
    servingInfo: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = servingInfo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = "$calories ккал",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyState(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AmountInputStep(
    uiState: QuickAddUiState,
    onAmountChanged: (String) -> Unit,
    onMealTypeChanged: (MealType) -> Unit,
    onBackToSelection: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        // Заголовок с кнопкой "Назад", названием продукта и кнопкой закрытия
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBackToSelection) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                )
            }
            Text(
                text = uiState.selectedFood?.name ?: "",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Закрыть",
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Поле ввода граммов
        OutlinedTextField(
            value = uiState.amountText,
            onValueChange = { text ->
                // Только цифры и точка/запятая
                if (text.all { it.isDigit() || it == '.' || it == ',' }) {
                    onAmountChanged(text.replace(',', '.'))
                }
            },
            label = { Text("Количество") },
            suffix = { Text("г") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Live preview калорий
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = uiState.calculatedCalories.toString(),
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "ккал",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Б ${String.format("%.1f", uiState.calculatedProtein)}г  " +
                    "Ж ${String.format("%.1f", uiState.calculatedFat)}г  " +
                    "У ${String.format("%.1f", uiState.calculatedCarbs)}г",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Выбор MealType
        Text(
            text = "Приём пищи:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MealType.entries.forEach { mealType ->
                FilterChip(
                    selected = uiState.selectedMealType == mealType,
                    onClick = { onMealTypeChanged(mealType) },
                    label = {
                        Text(
                            text = mealType.shortDisplayName(),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Ошибка (если есть)
        if (uiState.error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Кнопка "Добавить в дневник"
        Button(
            onClick = onSave,
            enabled = !uiState.isLoading && uiState.isAmountValid,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = if (uiState.isLoading) "Сохранение..." else "Добавить в дневник",
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun MealType.displayName(): String =
    when (this) {
        MealType.BREAKFAST -> "Завтрак"
        MealType.LUNCH -> "Обед"
        MealType.DINNER -> "Ужин"
        MealType.SNACK -> "Перекус"
    }

private fun MealType.shortDisplayName(): String =
    when (this) {
        MealType.BREAKFAST -> "Завт"
        MealType.LUNCH -> "Обед"
        MealType.DINNER -> "Ужин"
        MealType.SNACK -> "Пер"
    }

// ========== Previews ==========

@Preview(name = "QuickAdd - Food Selection Step", showBackground = true)
@Composable
private fun QuickAddFoodSelectionPreview() {
    FoodTrackerTheme {
        FoodSelectionStep(
            uiState = QuickAddUiState(
                isVisible = true,
                selectedMealType = MealType.BREAKFAST,
                selectedTab = QuickAddTab.FAVORITES,
                favoriteFoods = listOf(
                    Food(
                        id = 1L,
                        name = "Овсяная каша",
                        calories = 88,
                        protein = 3.0,
                        carbs = 15.0,
                        fat = 1.5,
                        servingSize = 100.0,
                        servingUnit = "г",
                        isFavorite = true,
                    ),
                    Food(
                        id = 2L,
                        name = "Куриная грудка",
                        calories = 110,
                        protein = 23.0,
                        carbs = 0.0,
                        fat = 1.8,
                        servingSize = 100.0,
                        servingUnit = "г",
                        isFavorite = true,
                    ),
                ),
            ),
            onFoodSelected = {},
            onRecentEntrySelected = {},
            onTabSelected = {},
            onDismiss = {},
            onNavigateToSearch = {},
        )
    }
}

@Preview(name = "QuickAdd - Food Selection Empty", showBackground = true)
@Composable
private fun QuickAddFoodSelectionEmptyPreview() {
    FoodTrackerTheme {
        FoodSelectionStep(
            uiState = QuickAddUiState(
                isVisible = true,
                selectedMealType = MealType.LUNCH,
                selectedTab = QuickAddTab.FAVORITES,
                favoriteFoods = emptyList(),
            ),
            onFoodSelected = {},
            onRecentEntrySelected = {},
            onTabSelected = {},
            onDismiss = {},
            onNavigateToSearch = {},
        )
    }
}

@Preview(name = "QuickAdd - Amount Input Step", showBackground = true)
@Composable
private fun QuickAddAmountInputPreview() {
    FoodTrackerTheme {
        AmountInputStep(
            uiState = QuickAddUiState(
                isVisible = true,
                selectedMealType = MealType.LUNCH,
                step = QuickAddStep.AMOUNT_INPUT,
                selectedFood = Food(
                    id = 2L,
                    name = "Куриная грудка",
                    calories = 110,
                    protein = 23.0,
                    carbs = 0.0,
                    fat = 1.8,
                    servingSize = 100.0,
                    servingUnit = "г",
                ),
                amountText = "150",
                calculatedCalories = 165,
                calculatedProtein = 34.5,
                calculatedCarbs = 0.0,
                calculatedFat = 2.7,
                effectiveAmountGrams = 150.0,
            ),
            onAmountChanged = {},
            onMealTypeChanged = {},
            onBackToSelection = {},
            onSave = {},
            onDismiss = {},
        )
    }
}

@Preview(name = "QuickAdd - Amount Input with Error", showBackground = true)
@Composable
private fun QuickAddAmountInputErrorPreview() {
    FoodTrackerTheme {
        AmountInputStep(
            uiState = QuickAddUiState(
                isVisible = true,
                selectedMealType = MealType.BREAKFAST,
                step = QuickAddStep.AMOUNT_INPUT,
                selectedFood = Food(
                    id = 1L,
                    name = "Овсяная каша",
                    calories = 88,
                    protein = 3.0,
                    carbs = 15.0,
                    fat = 1.5,
                    servingSize = 100.0,
                    servingUnit = "г",
                ),
                amountText = "",
                calculatedCalories = 0,
                error = "Ошибка сохранения",
            ),
            onAmountChanged = {},
            onMealTypeChanged = {},
            onBackToSelection = {},
            onSave = {},
            onDismiss = {},
        )
    }
}
