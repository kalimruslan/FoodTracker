package com.ruslan.foodtracker.core.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruslan.foodtracker.core.ui.theme.*

/**
 * Модель данных для нутриента
 */
data class NutrientData(
    val label: String,
    val value: String,
    val unit: String,
    val color: Color
)

/**
 * Сетка макронутриентов (Grid 2 колонки)
 * Используется на экране продукта для отображения калорий, Б/Ж/У/клетчатки
 * Калории занимают всю ширину (2 колонки)
 *
 * @param calories калории
 * @param protein белки
 * @param fat жиры
 * @param carbs углеводы
 * @param fiber клетчатка (опционально)
 * @param modifier модификатор
 */
@Composable
fun NutrientGrid(
    calories: String,
    protein: String,
    fat: String,
    carbs: String,
    fiber: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Калории - на всю ширину
        NutrientCard(
            label = "Калории",
            value = calories,
            unit = "ккал",
            color = Primary,
            isLarge = true
        )

        // Остальные макронутриенты в 2 колонки
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NutrientCard(
                label = "Белки",
                value = protein,
                unit = "г",
                color = ProteinColor,
                modifier = Modifier.weight(1f)
            )

            NutrientCard(
                label = "Жиры",
                value = fat,
                unit = "г",
                color = FatColor,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NutrientCard(
                label = "Углеводы",
                value = carbs,
                unit = "г",
                color = CarbsColor,
                modifier = Modifier.weight(1f)
            )

            if (fiber != null) {
                NutrientCard(
                    label = "Клетчатка",
                    value = fiber,
                    unit = "г",
                    color = FiberColor,
                    modifier = Modifier.weight(1f)
                )
            } else {
                // Пустая карточка для симметрии, если клетчатки нет
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

/**
 * Карточка для отдельного нутриента
 */
@Composable
private fun NutrientCard(
    label: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = if (isLarge) Alignment.CenterHorizontally else Alignment.Start
        ) {
            // Label
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Value + Unit
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = value,
                    fontSize = if (isLarge) 28.sp else 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
                Text(
                    text = unit,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

// ========== Preview ==========

@Preview(name = "NutrientGrid - Полная", showBackground = true)
@Composable
private fun NutrientGridPreview() {
    FoodTrackerTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            NutrientGrid(
                calories = "165",
                protein = "34.7",
                fat = "1.8",
                carbs = "0.0",
                fiber = "0.0"
            )
        }
    }
}

@Preview(name = "NutrientGrid - Без клетчатки", showBackground = true)
@Composable
private fun NutrientGridPreviewNoFiber() {
    FoodTrackerTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            NutrientGrid(
                calories = "120",
                protein = "2.6",
                fat = "0.8",
                carbs = "24.7",
                fiber = null
            )
        }
    }
}

@Preview(name = "NutrientGrid - Dark Theme", showBackground = true)
@Composable
private fun NutrientGridPreviewDark() {
    FoodTrackerTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            NutrientGrid(
                calories = "303",
                protein = "8.4",
                fat = "3.9",
                carbs = "60.0",
                fiber = "6.8"
            )
        }
    }
}

@Preview(name = "NutrientGrid - Большие значения", showBackground = true)
@Composable
private fun NutrientGridPreviewLarge() {
    FoodTrackerTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            NutrientGrid(
                calories = "1250",
                protein = "125.5",
                fat = "89.3",
                carbs = "275.8",
                fiber = "45.2"
            )
        }
    }
}
