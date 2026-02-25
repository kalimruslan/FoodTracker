package com.ruslan.foodtracker.feature.home.presenter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ruslan.foodtracker.core.common.util.DateTimeUtils
import com.ruslan.foodtracker.core.ui.components.*
import com.ruslan.foodtracker.core.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Главный экран (Дневник питания)
 * Отображает калории, макронутриенты, приёмы пищи и трекер воды
 */
@Composable
fun HomeScreen(
    onNavigateToSearch: (mealType: String, date: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    quickAddViewModel: QuickAddViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val quickAddUiState by quickAddViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Snackbar "Добавлено в дневник" после QuickAdd
    LaunchedEffect(quickAddUiState.isSaved) {
        if (quickAddUiState.isSaved) {
            snackbarHostState.showSnackbar("Добавлено в дневник")
            quickAddViewModel.onSavedHandled()
        }
    }

    // Snackbar "Продукт удалён" с кнопкой Undo после свайп-удаления
    LaunchedEffect(uiState.showDeleteSnackbar) {
        if (uiState.showDeleteSnackbar) {
            val result = snackbarHostState.showSnackbar(
                message = "Продукт удалён",
                actionLabel = "Отменить",
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.onUndoDelete()
            } else {
                viewModel.onDeleteSnackbarDismissed()
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        HomeScreenContent(
            uiState = uiState,
            onMealAddClick = { meal ->
                quickAddViewModel.open(meal.mealType, uiState.selectedDate)
            },
            onNavigateToSearch = onNavigateToSearch,
            onDaySelected = viewModel::onDaySelected,
            onPreviousWeek = viewModel::onPreviousWeek,
            onNextWeek = viewModel::onNextWeek,
            onTodayClicked = viewModel::onTodayClicked,
            onAddWaterGlass = viewModel::onAddWaterGlass,
            onDeleteItem = viewModel::onDeleteEntry,
            onEditItem = viewModel::onEditEntry,
            modifier = Modifier.padding(padding),
        )
    }

    // QuickAdd bottom sheet — вне Scaffold для корректной обработки WindowInsets
    QuickAddBottomSheet(
        uiState = quickAddUiState,
        onFoodSelected = quickAddViewModel::onFoodSelected,
        onRecentEntrySelected = quickAddViewModel::onRecentEntrySelected,
        onAmountChanged = quickAddViewModel::onAmountChanged,
        onMealTypeChanged = quickAddViewModel::onMealTypeChanged,
        onTabSelected = quickAddViewModel::onTabSelected,
        onBackToSelection = quickAddViewModel::onBackToSelection,
        onSave = quickAddViewModel::saveEntry,
        onDismiss = quickAddViewModel::close,
        onNavigateToSearch = {
            quickAddViewModel.close()
            onNavigateToSearch(
                quickAddUiState.selectedMealType.name,
                uiState.selectedDate.toString(),
            )
        },
    )

    // EditEntry bottom sheet — вне Scaffold для корректной обработки WindowInsets
    uiState.editingEntry?.let { entry ->
        EditEntryBottomSheet(
            entry = entry,
            onSave = { newGrams -> viewModel.onUpdateEntryAmount(entry, newGrams) },
            onDismiss = viewModel::onEditDismiss,
        )
    }
}

@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    onMealAddClick: (MealData) -> Unit,
    onNavigateToSearch: (mealType: String, date: String) -> Unit,
    onDaySelected: (Int) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onTodayClicked: () -> Unit,
    onAddWaterGlass: () -> Unit,
    onDeleteItem: (Long) -> Unit,
    onEditItem: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        HomeHeader(
            uiState = uiState,
            onDaySelected = onDaySelected,
            onPreviousWeek = onPreviousWeek,
            onNextWeek = onNextWeek,
            onTodayClicked = onTodayClicked,
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                LoadingIndicator(text = "Загрузка...")
            }
            return
        }

        if (uiState.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = uiState.error,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            MealsSection(
                meals = uiState.meals,
                onAddClick = onMealAddClick,
                onDeleteItem = onDeleteItem,
                onEditItem = onEditItem,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            )

            WaterTracker(
                current = uiState.waterGlasses,
                target = uiState.waterTarget,
                onAddGlass = onAddWaterGlass,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Header с градиентом: дата, дни недели, кольцевая диаграмма + макронутриенты
 */
@Composable
private fun HomeHeader(
    uiState: HomeUiState,
    onDaySelected: (Int) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onTodayClicked: () -> Unit,
) {
    val headerBrush = remember { Brush.verticalGradient(colors = listOf(Primary, PrimaryDark)) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("d MMMM", Locale("ru")) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = headerBrush)
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                val headerLabel = if (uiState.selectedDate == LocalDate.now()) {
                    "Сегодня"
                } else {
                    uiState.selectedDate.format(DateTimeFormatter.ofPattern("d MMMM", Locale("ru")))
                }
                Text(
                    text = headerLabel,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Normal,
                )
                Text(
                    text = uiState.selectedDate.format(dateFormatter),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeaderIconButton(icon = Icons.Filled.CalendarToday)
                HeaderIconButton(icon = Icons.Filled.Notifications)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        WeekNavigationRow(
            weekRange = DateTimeUtils.formatWeekRange(uiState.currentWeekStart),
            showTodayButton = uiState.showTodayButton,
            canGoNext = DateTimeUtils.weekStart(uiState.currentWeekStart.plusWeeks(1)) <=
                DateTimeUtils.weekStart(LocalDate.now()),
            onPreviousWeek = onPreviousWeek,
            onNextWeek = onNextWeek,
            onTodayClicked = onTodayClicked,
        )

        Spacer(modifier = Modifier.height(4.dp))

        WeekDaySelector(
            selectedDayIndex = uiState.selectedDayIndex,
            currentWeekStart = uiState.currentWeekStart,
            onDaySelected = onDaySelected,
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgress(
                value = uiState.consumedCalories,
                max = uiState.targetCalories,
                size = 130.dp,
                strokeWidth = 10.dp,
                color = Color.White,
                backgroundColor = Color.White.copy(alpha = 0.2f),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = uiState.consumedCalories.toInt().toString(),
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White,
                    )
                    Text(
                        text = "из ${uiState.targetCalories.toInt()} ккал",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                    Text(
                        text = "Осталось ${(uiState.targetCalories - uiState.consumedCalories).toInt()}",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f),
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MacroProgressBarWhite(
                    label = "Белки",
                    value = uiState.protein.consumed,
                    max = uiState.protein.target,
                    unit = "г",
                    color = ProteinColor,
                )
                MacroProgressBarWhite(
                    label = "Жиры",
                    value = uiState.fat.consumed,
                    max = uiState.fat.target,
                    unit = "г",
                    color = FatColor,
                )
                MacroProgressBarWhite(
                    label = "Углеводы",
                    value = uiState.carbs.consumed,
                    max = uiState.carbs.target,
                    unit = "г",
                    color = CarbsColor,
                )
                MacroProgressBarWhite(
                    label = "Клетчатка",
                    value = uiState.fiber.consumed,
                    max = uiState.fiber.target,
                    unit = "г",
                    color = FiberColor,
                )
            }
        }
    }
}

@Composable
private fun HeaderIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .clickable { /* TODO */ },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun WeekNavigationRow(
    weekRange: String,
    showTodayButton: Boolean,
    canGoNext: Boolean,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onTodayClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPreviousWeek) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Предыдущая неделя",
                tint = Color.White,
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = weekRange,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
            if (showTodayButton) {
                TextButton(
                    onClick = onTodayClicked,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                ) {
                    Text(
                        text = "Сегодня",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
            }
        }

        IconButton(onClick = onNextWeek, enabled = canGoNext) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Следующая неделя",
                tint = if (canGoNext) Color.White else Color.White.copy(alpha = 0.3f),
            )
        }
    }
}

@Composable
private fun WeekDaySelector(
    selectedDayIndex: Int,
    currentWeekStart: LocalDate,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()
    val weekDayNames = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val weekDates = DateTimeUtils.weekDates(currentWeekStart)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        weekDates.forEachIndexed { index, date ->
            val isFuture = date.isAfter(today)
            val isSelected = selectedDayIndex == index
            val isToday = date == today

            Column(
                modifier = Modifier
                    .width(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when {
                            isSelected -> Color.White.copy(alpha = 0.25f)
                            else -> Color.Transparent
                        },
                    )
                    .then(
                        if (!isFuture) {
                            Modifier.clickable { onDaySelected(index) }
                        } else {
                            Modifier
                        },
                    )
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = weekDayNames[index],
                    fontSize = 10.sp,
                    color = if (isFuture) Color.White.copy(alpha = 0.3f)
                    else Color.White.copy(alpha = 0.7f),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = date.dayOfMonth.toString(),
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                    color = when {
                        isFuture -> Color.White.copy(alpha = 0.3f)
                        else -> Color.White
                    },
                )
                if (isToday && !isSelected) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.8f)),
                    )
                }
            }
        }
    }
}

/**
 * Секция с карточками приёмов пищи
 */
@Composable
private fun MealsSection(
    meals: List<MealData>,
    onAddClick: (MealData) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onEditItem: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Приёмы пищи",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        meals.forEach { meal ->
            val onMealAddClick = remember(meal) { { onAddClick(meal) } }
            MealCard(
                emoji = meal.emoji,
                name = meal.name,
                time = meal.time,
                totalCalories = meal.totalCalories,
                foodItems = meal.foodItems,
                onAddClick = onMealAddClick,
                onDeleteItem = onDeleteItem,
                onEditItem = onEditItem,
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

/**
 * Трекер воды
 */
@Composable
private fun WaterTracker(
    current: Int,
    target: Int,
    onAddGlass: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        val waterBrush = remember {
            Brush.horizontalGradient(
                colors = listOf(
                    Secondary.copy(alpha = 0.15f),
                    Secondary.copy(alpha = 0.08f),
                ),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = waterBrush)
                .padding(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "💧", fontSize = 24.sp)
                    Column {
                        Text(
                            text = "Вода",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "$current из $target стаканов",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Button(
                    onClick = onAddGlass,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Secondary,
                    ),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(
                        text = "+ Стакан",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

// ========== Preview ==========

@Preview(name = "HomeScreen - Light", showBackground = true)
@Composable
private fun HomeScreenPreview() {
    FoodTrackerTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                consumedCalories = 684f,
                targetCalories = 2200f,
                protein = MacroData(56f, 140f),
                fat = MacroData(22f, 73f),
                carbs = MacroData(88f, 275f),
                fiber = MacroData(8f, 30f),
                meals = listOf(
                    MealData(
                        id = 1,
                        mealType = com.ruslan.foodtracker.domain.model.MealType.BREAKFAST,
                        emoji = "🌅",
                        name = "Завтрак",
                        time = "08:00",
                        totalCalories = 303,
                        foodItems = listOf(
                            FoodItemData("Овсяная каша", "200г", 150, entryId = 1L),
                            FoodItemData("Банан", "1 шт", 89, entryId = 2L),
                        ),
                    ),
                    MealData(
                        id = 2,
                        mealType = com.ruslan.foodtracker.domain.model.MealType.LUNCH,
                        emoji = "☀️",
                        name = "Обед",
                        time = "13:00",
                        totalCalories = 381,
                        foodItems = listOf(
                            FoodItemData("Куриная грудка", "150г", 165, entryId = 3L),
                        ),
                    ),
                ),
                selectedDayIndex = 3,
                waterGlasses = 4,
                waterTarget = 8,
            ),
            onMealAddClick = {},
            onNavigateToSearch = { _, _ -> },
            onDaySelected = {},
            onPreviousWeek = {},
            onNextWeek = {},
            onTodayClicked = {},
            onAddWaterGlass = {},
            onDeleteItem = {},
            onEditItem = {},
        )
    }
}

@Preview(name = "HomeScreen - Dark", showBackground = true)
@Composable
private fun HomeScreenPreviewDark() {
    FoodTrackerTheme(darkTheme = true) {
        HomeScreenContent(
            uiState = HomeUiState(
                consumedCalories = 1250f,
                targetCalories = 2200f,
                protein = MacroData(100f, 140f),
                fat = MacroData(50f, 73f),
                carbs = MacroData(150f, 275f),
                fiber = MacroData(20f, 30f),
                meals = emptyList(),
                selectedDayIndex = 0,
                waterGlasses = 6,
                waterTarget = 8,
            ),
            onMealAddClick = {},
            onNavigateToSearch = { _, _ -> },
            onDaySelected = {},
            onPreviousWeek = {},
            onNextWeek = {},
            onTodayClicked = {},
            onAddWaterGlass = {},
            onDeleteItem = {},
            onEditItem = {},
        )
    }
}

@Preview(name = "WeekNavigationRow - Current Week", showBackground = true, backgroundColor = 0xFF5C7EE6)
@Composable
private fun WeekNavigationRowCurrentWeekPreview() {
    FoodTrackerTheme {
        WeekNavigationRow(
            weekRange = "24 – 30 фев",
            showTodayButton = false,
            canGoNext = false,
            onPreviousWeek = {},
            onNextWeek = {},
            onTodayClicked = {},
        )
    }
}

@Preview(name = "WeekNavigationRow - Past Week", showBackground = true, backgroundColor = 0xFF5C7EE6)
@Composable
private fun WeekNavigationRowPastWeekPreview() {
    FoodTrackerTheme {
        WeekNavigationRow(
            weekRange = "17 – 23 фев",
            showTodayButton = true,
            canGoNext = true,
            onPreviousWeek = {},
            onNextWeek = {},
            onTodayClicked = {},
        )
    }
}
