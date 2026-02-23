package com.ruslan.foodtracker.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruslan.foodtracker.core.ui.theme.*

/**
 * Модель данных для продукта в приёме пищи
 */
data class FoodItemData(
    val name: String,
    val weight: String,
    val calories: Int
)

/**
 * Карточка приёма пищи
 * Отображает название, время, итоговые калории и список продуктов
 *
 * @param emoji иконка приёма пищи (например, "🌅" для завтрака)
 * @param name название приёма пищи (например, "Завтрак")
 * @param time время приёма пищи (например, "08:00")
 * @param totalCalories итоговые калории приёма
 * @param foodItems список продуктов в приёме
 * @param onAddClick обработчик нажатия на кнопку "+"
 * @param modifier модификатор
 */
@Composable
fun MealCard(
    emoji: String,
    name: String,
    time: String?,
    totalCalories: Int,
    foodItems: List<FoodItemData>,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
            ).border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Верхняя строка: emoji + название/время | калории + кнопка "+"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Левая часть: emoji + название + время
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = emoji,
                        fontSize = 24.sp
                    )

                    Column {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (time != null) {
                            Text(
                                text = time,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Правая часть: калории + кнопка "+"
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (totalCalories > 0) {
                        Text(
                            text = "$totalCalories ккал",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }

                    // Кнопка "+" с gradient
                    val addButtonBrush = remember { Brush.linearGradient(colors = listOf(Primary, PrimaryLight)) }
                    FilledIconButton(
                        onClick = onAddClick,
                        modifier = Modifier
                            .size(32.dp)
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(10.dp),
                                ambientColor = Primary.copy(alpha = 0.4f)
                            ).clip(RoundedCornerShape(10.dp))
                            .background(brush = addButtonBrush),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = androidx.compose.ui.graphics.Color.Transparent
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Добавить продукт",
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Список продуктов (если есть)
            if (foodItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(8.dp))

                foodItems.forEach { item ->
                    FoodItemRow(
                        name = item.name,
                        weight = item.weight,
                        calories = item.calories
                    )
                }
            }
        }
    }
}

/**
 * Строка с продуктом внутри карточки приёма пищи
 */
@Composable
private fun FoodItemRow(
    name: String,
    weight: String,
    calories: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Название продукта + вес
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = weight,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Калории
        Text(
            text = calories.toString(),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ========== Preview ==========

@Preview(name = "MealCard - С продуктами", showBackground = true)
@Composable
private fun MealCardPreviewWithItems() {
    FoodTrackerTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            MealCard(
                emoji = "🌅",
                name = "Завтрак",
                time = "08:00",
                totalCalories = 303,
                foodItems = listOf(
                    FoodItemData("Овсяная каша", "200г", 150),
                    FoodItemData("Банан", "1 шт", 89),
                    FoodItemData("Мёд", "1 ст.л.", 64)
                ),
                onAddClick = {}
            )
        }
    }
}

@Preview(name = "MealCard - Пустой", showBackground = true)
@Composable
private fun MealCardPreviewEmpty() {
    FoodTrackerTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            MealCard(
                emoji = "🌙",
                name = "Ужин",
                time = "19:00",
                totalCalories = 0,
                foodItems = emptyList(),
                onAddClick = {}
            )
        }
    }
}

@Preview(name = "MealCard - Без времени", showBackground = true)
@Composable
private fun MealCardPreviewNoTime() {
    FoodTrackerTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            MealCard(
                emoji = "🍎",
                name = "Перекус",
                time = null,
                totalCalories = 0,
                foodItems = emptyList(),
                onAddClick = {}
            )
        }
    }
}

@Preview(name = "MealCard - Dark Theme", showBackground = true)
@Composable
private fun MealCardPreviewDark() {
    FoodTrackerTheme(darkTheme = true) {
        Surface(modifier = Modifier.padding(16.dp)) {
            MealCard(
                emoji = "☀️",
                name = "Обед",
                time = "13:00",
                totalCalories = 381,
                foodItems = listOf(
                    FoodItemData("Куриная грудка", "150г", 165),
                    FoodItemData("Рис бурый", "180г", 216)
                ),
                onAddClick = {}
            )
        }
    }
}
