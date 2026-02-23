package com.ruslan.foodtracker.feature.home.presenter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ruslan.foodtracker.core.navigation.NavRoutes
import com.ruslan.foodtracker.core.ui.theme.*
import com.ruslan.foodtracker.domain.model.MealType
import java.util.Locale

@Composable
fun AddFoodEntryScreen(
    route: NavRoutes.AddFoodEntry,
    onNavigateBack: () -> Unit,
    viewModel: AddFoodEntryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(route.foodId) {
        viewModel.init(route)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    AddFoodEntryContent(
        uiState = uiState,
        onAmountChanged = viewModel::onAmountChanged,
        onMealTypeChanged = viewModel::onMealTypeChanged,
        onInputModeChanged = viewModel::onInputModeChanged,
        onFavoriteToggle = viewModel::onToggleFavorite,
        onSave = { viewModel.saveEntry() },
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun AddFoodEntryContent(
    uiState: AddFoodEntryUiState,
    onAmountChanged: (String) -> Unit,
    onMealTypeChanged: (MealType) -> Unit,
    onInputModeChanged: (InputMode) -> Unit,
    onFavoriteToggle: () -> Unit,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
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
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f)
            )
            // Кнопка избранного
            IconButton(
                onClick = onFavoriteToggle,
                enabled = uiState.foodId != 0L
            ) {
                Icon(
                    imageVector = if (uiState.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = if (uiState.isFavorite) "Убрать из избранного" else "Добавить в избранное",
                    tint = if (uiState.isFavorite) Accent
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (uiState.foodId != 0L) 1f else 0.4f
                    ),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Карточка продукта
            ProductInfoCard(uiState = uiState)

            // Выбор единицы измерения + поле ввода количества
            AmountInputCard(
                amountText = uiState.amountText,
                selectedInputMode = uiState.selectedInputMode,
                portionGrams = uiState.portionGrams,
                onAmountChanged = onAmountChanged,
                onInputModeChanged = onInputModeChanged
            )

            // Расчётные нутриенты
            NutrientsResultCard(uiState = uiState)

            // Выбор типа приёма пищи
            MealTypeSelectorCard(
                selectedMealType = uiState.selectedMealType,
                onMealTypeChanged = onMealTypeChanged
            )

            // Ошибка
            if (uiState.error != null) {
                Text(
                    text = uiState.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Кнопка сохранения
            Button(
                onClick = onSave,
                enabled = !uiState.isLoading && uiState.isAmountValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Добавить в дневник",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProductInfoCard(uiState: AddFoodEntryUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        val cardBrush = remember {
            Brush.horizontalGradient(
                colors = listOf(Primary.copy(alpha = 0.08f), Primary.copy(alpha = 0.02f))
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBrush)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = uiState.foodName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                if (uiState.brand != null) {
                    Text(
                        text = uiState.brand,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "На 1 порцию (${uiState.servingSize.toInt()} ${uiState.servingUnit}): ${uiState.caloriesPerServing} ккал",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AmountInputCard(
    amountText: String,
    selectedInputMode: InputMode,
    portionGrams: Double,
    onAmountChanged: (String) -> Unit,
    onInputModeChanged: (InputMode) -> Unit
) {
    val inputModes = remember { InputMode.entries }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Количество",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            // Чипы выбора единицы измерения
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                inputModes.forEach { mode ->
                    FilterChip(
                        selected = selectedInputMode == mode,
                        onClick = { onInputModeChanged(mode) },
                        label = {
                            Text(
                                text = mode.getLabel(portionGrams),
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Поле ввода
            OutlinedTextField(
                value = amountText,
                onValueChange = { text ->
                    if (text.isEmpty() || text.matches(Regex("^\\d{0,5}(\\.\\d{0,1})?$"))) {
                        onAmountChanged(text)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                suffix = {
                    Text(
                        text = selectedInputMode.suffix,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
private fun NutrientsResultCard(uiState: AddFoodEntryUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Пищевая ценность",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            // Калории — главный показатель
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.calculatedCalories.toString(),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Primary
                    )
                    Text(
                        text = "ккал",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // Макронутриенты
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MacroItem(
                    label = "Белки",
                    value = uiState.calculatedProtein,
                    color = ProteinColor
                )
                MacroItem(
                    label = "Жиры",
                    value = uiState.calculatedFat,
                    color = FatColor
                )
                MacroItem(
                    label = "Углеводы",
                    value = uiState.calculatedCarbs,
                    color = CarbsColor
                )
            }
        }
    }
}

@Composable
private fun MacroItem(label: String, value: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = String.format(Locale.US, "%.1f", value),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = "$label, г",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MealTypeSelectorCard(
    selectedMealType: MealType,
    onMealTypeChanged: (MealType) -> Unit
) {
    val mealTypes = remember {
        listOf(
            MealType.BREAKFAST to ("🌅" to "Завтрак"),
            MealType.LUNCH to ("☀️" to "Обед"),
            MealType.DINNER to ("🌙" to "Ужин"),
            MealType.SNACK to ("🍎" to "Перекус")
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Приём пищи",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                mealTypes.forEach { (type, info) ->
                    val (emoji, label) = info
                    val isSelected = selectedMealType == type
                    val onTypeClick = remember(type) { { onMealTypeChanged(type) } }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) Primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = if (isSelected) 0.dp else 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable(onClick = onTypeClick)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = emoji, fontSize = 16.sp)
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
