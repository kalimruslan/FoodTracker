package com.ruslan.foodtracker.feature.product.presenter.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ruslan.foodtracker.core.ui.theme.FoodTrackerTheme
import com.ruslan.foodtracker.core.ui.theme.Primary
import com.ruslan.foodtracker.domain.model.MealType

/**
 * Диалог выбора приема пищи
 *
 * @param onMealSelected Callback при выборе приема пищи
 * @param onDismiss Callback при закрытии диалога
 */
@Composable
fun MealSelectionDialog(
    onMealSelected: (MealType) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Заголовок
                Text(
                    text = "Выберите приём пищи",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                // Список приемов пищи
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MealOption(
                        emoji = "🌅",
                        name = "Завтрак",
                        mealType = MealType.BREAKFAST,
                        onClick = { onMealSelected(MealType.BREAKFAST) }
                    )
                    MealOption(
                        emoji = "☀️",
                        name = "Обед",
                        mealType = MealType.LUNCH,
                        onClick = { onMealSelected(MealType.LUNCH) }
                    )
                    MealOption(
                        emoji = "🌙",
                        name = "Ужин",
                        mealType = MealType.DINNER,
                        onClick = { onMealSelected(MealType.DINNER) }
                    )
                    MealOption(
                        emoji = "🍎",
                        name = "Перекус",
                        mealType = MealType.SNACK,
                        onClick = { onMealSelected(MealType.SNACK) }
                    )
                }

                // Кнопка отмены
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Отмена")
                }
            }
        }
    }
}

/**
 * Вариант выбора приема пищи
 */
@Composable
private fun MealOption(
    emoji: String,
    name: String,
    mealType: MealType,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Emoji
        Text(
            text = emoji,
            fontSize = 24.sp
        )

        // Название
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
    }
}

// ========== Preview ==========

@Preview(showBackground = true)
@Composable
private fun MealSelectionDialogPreview() {
    FoodTrackerTheme {
        MealSelectionDialog(
            onMealSelected = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MealSelectionDialogPreviewDark() {
    FoodTrackerTheme(darkTheme = true) {
        MealSelectionDialog(
            onMealSelected = {},
            onDismiss = {}
        )
    }
}
