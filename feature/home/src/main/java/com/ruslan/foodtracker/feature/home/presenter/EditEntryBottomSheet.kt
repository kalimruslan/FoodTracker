package com.ruslan.foodtracker.feature.home.presenter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruslan.foodtracker.core.ui.theme.FoodTrackerTheme
import com.ruslan.foodtracker.domain.model.FoodEntry
import com.ruslan.foodtracker.domain.model.MealType
import java.time.LocalDateTime
import kotlin.math.roundToInt

/**
 * Bottom sheet для редактирования граммовки записи приёма пищи.
 * Показывает название продукта, поле ввода граммов и live-preview пересчитанных калорий.
 *
 * Пересчёт макросов выполняется через коэффициент от исходной записи:
 *   newCalories = round((entry.calories / entry.amountGrams) * newGrams)
 *
 * @param entry запись, которую редактируем
 * @param onSave вызывается с новым значением граммов при нажатии "Сохранить"
 * @param onDismiss вызывается при закрытии bottom sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntryBottomSheet(
    entry: FoodEntry,
    onSave: (newAmountGrams: Double) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        EditEntryContent(
            entry = entry,
            onSave = onSave,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun EditEntryContent(
    entry: FoodEntry,
    onSave: (Double) -> Unit,
    onDismiss: () -> Unit,
) {
    var amountText by remember(entry.id) {
        mutableStateOf(entry.amountGrams.toInt().toString())
    }

    // Пересчёт калорий и макросов через коэффициент
    // Защита от деления на ноль: если исходные граммы <= 0, показываем исходные значения
    val newGrams = amountText.toDoubleOrNull() ?: 0.0
    val canRecalculate = entry.amountGrams > 0 && newGrams > 0
    val ratio = if (canRecalculate) newGrams / entry.amountGrams else 1.0

    val previewCalories = if (canRecalculate) (entry.calories * ratio).roundToInt() else entry.calories
    val previewProtein = if (canRecalculate) entry.protein * ratio else entry.protein
    val previewCarbs = if (canRecalculate) entry.carbs * ratio else entry.carbs
    val previewFat = if (canRecalculate) entry.fat * ratio else entry.fat

    val isSaveEnabled = newGrams > 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        // Заголовок с кнопкой закрытия
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Редактировать граммовку",
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

        Spacer(modifier = Modifier.height(4.dp))

        // Название продукта
        Text(
            text = entry.foodName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Поле ввода граммов
        OutlinedTextField(
            value = amountText,
            onValueChange = { text ->
                if (text.all { it.isDigit() || it == '.' || it == ',' }) {
                    amountText = text.replace(',', '.')
                }
            },
            label = { Text("Количество") },
            suffix = { Text("г") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Live preview калорий
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = previewCalories.toString(),
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
                text = "Б ${String.format("%.1f", previewProtein)}г  " +
                    "Ж ${String.format("%.1f", previewFat)}г  " +
                    "У ${String.format("%.1f", previewCarbs)}г",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Кнопка "Сохранить"
        Button(
            onClick = { onSave(newGrams) },
            enabled = isSaveEnabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Сохранить",
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ========== Previews ==========

@Preview(name = "EditEntryBottomSheet - content", showBackground = true)
@Composable
private fun EditEntryBottomSheetPreview() {
    FoodTrackerTheme {
        EditEntryContent(
            entry = FoodEntry(
                id = 1L,
                foodId = 10L,
                foodName = "Куриная грудка",
                servings = 1.5,
                amountGrams = 150.0,
                calories = 165,
                protein = 34.5,
                carbs = 0.0,
                fat = 2.7,
                timestamp = LocalDateTime.now(),
                mealType = MealType.LUNCH,
            ),
            onSave = {},
            onDismiss = {},
        )
    }
}