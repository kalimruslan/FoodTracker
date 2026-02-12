package com.ruslan.foodtracker.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Notifications
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
import com.ruslan.foodtracker.core.ui.components.*
import com.ruslan.foodtracker.core.ui.theme.*
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Главный экран (Дневник питания)
 * Отображает калории, макронутриенты, приёмы пищи и трекер воды
 */
@Composable
fun HomeScreen(
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        uiState = uiState,
        onNavigateToSearch = onNavigateToSearch,
        onDaySelected = viewModel::onDaySelected,
        onAddWaterGlass = viewModel::onAddWaterGlass,
        modifier = modifier
    )
}

@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    onNavigateToSearch: () -> Unit,
    onDaySelected: (Int) -> Unit,
    onAddWaterGlass: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header с gradient
        HomeHeader(
            uiState = uiState,
            onDaySelected = onDaySelected
        )

        // Приёмы пищи
        MealsSection(
            meals = uiState.meals,
            onAddClick = onNavigateToSearch,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )

        // Трекер воды
        WaterTracker(
            current = uiState.waterGlasses,
            target = uiState.waterTarget,
            onAddGlass = onAddWaterGlass,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Header с градиентом: дата, дни недели, кольцевая диаграмма + макронутриенты
 */
@Composable
private fun HomeHeader(
    uiState: HomeUiState,
    onDaySelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Primary, PrimaryDark)
                )
            )
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Верхняя строка: Сегодня + дата | иконки
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Сегодня",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = uiState.selectedDate.format(
                        DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
                    ),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeaderIconButton(icon = Icons.Filled.CalendarToday)
                HeaderIconButton(icon = Icons.Filled.Notifications)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Выбор дня недели
        WeekDaySelector(
            selectedDayIndex = uiState.selectedDayIndex,
            onDaySelected = onDaySelected
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Кольцевая диаграмма + макронутриенты
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Кольцевая диаграмма калорий
            CircularProgress(
                value = uiState.consumedCalories,
                max = uiState.targetCalories,
                size = 130.dp,
                strokeWidth = 10.dp,
                color = Color.White,
                backgroundColor = Color.White.copy(alpha = 0.2f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.consumedCalories.toInt().toString(),
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White
                    )
                    Text(
                        text = "из ${uiState.targetCalories.toInt()} ккал",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Осталось ${(uiState.targetCalories - uiState.consumedCalories).toInt()}",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // Макронутриенты (прогресс-бары)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MacroProgressBarWhite(
                    label = "Белки",
                    value = uiState.protein.consumed,
                    max = uiState.protein.target,
                    unit = "г",
                    color = ProteinColor
                )
                MacroProgressBarWhite(
                    label = "Жиры",
                    value = uiState.fat.consumed,
                    max = uiState.fat.target,
                    unit = "г",
                    color = FatColor
                )
                MacroProgressBarWhite(
                    label = "Углеводы",
                    value = uiState.carbs.consumed,
                    max = uiState.carbs.target,
                    unit = "г",
                    color = CarbsColor
                )
                MacroProgressBarWhite(
                    label = "Клетчатка",
                    value = uiState.fiber.consumed,
                    max = uiState.fiber.target,
                    unit = "г",
                    color = FiberColor
                )
            }
        }
    }
}

/**
 * Кнопка с иконкой в header
 */
@Composable
private fun HeaderIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .clickable { /* TODO */ },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * Селектор дня недели (горизонтальный ряд)
 */
@Composable
private fun WeekDaySelector(
    selectedDayIndex: Int,
    onDaySelected: (Int) -> Unit
) {
    val weekDays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val startDay = 9 // Начальный день месяца

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        weekDays.forEachIndexed { index, day ->
            Column(
                modifier = Modifier
                    .width(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (selectedDayIndex == index) Color.White.copy(alpha = 0.25f)
                        else Color.Transparent
                    )
                    .clickable { onDaySelected(index) }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = day,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = (startDay + index).toString(),
                    fontSize = 15.sp,
                    fontWeight = if (selectedDayIndex == index) FontWeight.ExtraBold else FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Секция с приёмами пищи
 */
@Composable
private fun MealsSection(
    meals: List<MealData>,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Заголовок секции
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Приёмы пищи",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "+ Добавить приём",
                fontSize = 12.sp,
                color = Primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { /* TODO */ }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Карточки приёмов пищи
        meals.forEach { meal ->
            MealCard(
                emoji = meal.emoji,
                name = meal.name,
                time = meal.time,
                totalCalories = meal.totalCalories,
                foodItems = meal.foodItems,
                onAddClick = onAddClick
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Secondary.copy(alpha = 0.15f),
                            Secondary.copy(alpha = 0.08f)
                        )
                    )
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "💧", fontSize = 24.sp)
                    Column {
                        Text(
                            text = "Вода",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$current из $target стаканов",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onAddGlass,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Secondary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "+ Стакан",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
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
                        emoji = "🌅",
                        name = "Завтрак",
                        time = "08:00",
                        totalCalories = 303,
                        foodItems = listOf(
                            FoodItemData("Овсяная каша", "200г", 150),
                            FoodItemData("Банан", "1 шт", 89)
                        )
                    ),
                    MealData(
                        id = 2,
                        emoji = "☀️",
                        name = "Обед",
                        time = "13:00",
                        totalCalories = 381,
                        foodItems = listOf(
                            FoodItemData("Куриная грудка", "150г", 165)
                        )
                    )
                ),
                selectedDayIndex = 3,
                waterGlasses = 4,
                waterTarget = 8
            ),
            onNavigateToSearch = {},
            onDaySelected = {},
            onAddWaterGlass = {}
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
                waterTarget = 8
            ),
            onNavigateToSearch = {},
            onDaySelected = {},
            onAddWaterGlass = {}
        )
    }
}
