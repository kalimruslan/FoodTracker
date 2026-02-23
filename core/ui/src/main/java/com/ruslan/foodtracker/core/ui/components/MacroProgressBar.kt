package com.ruslan.foodtracker.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruslan.foodtracker.core.ui.theme.*
import kotlin.math.min

/**
 * Горизонтальный прогресс-бар для макронутриентов
 * Используется на главном экране для отображения Б/Ж/У/Клетчатки
 *
 * @param label название нутриента (например, "Белки")
 * @param value текущее значение
 * @param max максимальное значение (цель)
 * @param unit единица измерения (например, "г")
 * @param color цвет прогресс-бара
 * @param modifier модификатор
 */
@Composable
fun MacroProgressBar(
    label: String,
    value: Float,
    max: Float,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Верхняя строка: название и значение
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "${value.toInt()}/${max.toInt()}$unit",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Прогресс-бар
        val progress = min(value / max, 1f).coerceIn(0f, 1f)
        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(durationMillis = 1000),
            label = "progress_animation"
        )

        val progressBrush = remember(color) { Brush.horizontalGradient(colors = listOf(color, color.copy(alpha = 0.8f))) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush = progressBrush)
            )
        }
    }
}

/**
 * Прогресс-бар для макронутриентов на белом фоне header
 * Используется на главном экране в gradient header
 *
 * @param label название нутриента
 * @param value текущее значение
 * @param max максимальное значение
 * @param unit единица измерения
 * @param color цвет прогресс-бара
 * @param modifier модификатор
 */
@Composable
fun MacroProgressBarWhite(
    label: String,
    value: Float,
    max: Float,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Верхняя строка: название и значение
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.85f)
            )
            Text(
                text = "${value.toInt()}/${max.toInt()}$unit",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Прогресс-бар
        val progress = min(value / max, 1f).coerceIn(0f, 1f)
        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(durationMillis = 1000),
            label = "progress_animation_white"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}

// ========== Preview ==========

@Preview(name = "MacroProgressBar - Normal", showBackground = true)
@Composable
private fun MacroProgressBarPreview() {
    FoodTrackerTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MacroProgressBar(
                    label = "Белки",
                    value = 56f,
                    max = 140f,
                    unit = "г",
                    color = ProteinColor
                )

                MacroProgressBar(
                    label = "Жиры",
                    value = 22f,
                    max = 73f,
                    unit = "г",
                    color = FatColor
                )

                MacroProgressBar(
                    label = "Углеводы",
                    value = 88f,
                    max = 275f,
                    unit = "г",
                    color = CarbsColor
                )

                MacroProgressBar(
                    label = "Клетчатка",
                    value = 8f,
                    max = 30f,
                    unit = "г",
                    color = FiberColor
                )
            }
        }
    }
}

@Preview(name = "MacroProgressBar - White (Header)", showBackground = true, backgroundColor = 0xFFFF6B35)
@Composable
private fun MacroProgressBarWhitePreview() {
    FoodTrackerTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Primary, PrimaryDark)
                    )
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MacroProgressBarWhite(
                label = "Белки",
                value = 56f,
                max = 140f,
                unit = "г",
                color = ProteinColor
            )

            MacroProgressBarWhite(
                label = "Жиры",
                value = 22f,
                max = 73f,
                unit = "г",
                color = FatColor
            )

            MacroProgressBarWhite(
                label = "Углеводы",
                value = 88f,
                max = 275f,
                unit = "г",
                color = CarbsColor
            )

            MacroProgressBarWhite(
                label = "Клетчатка",
                value = 8f,
                max = 30f,
                unit = "г",
                color = FiberColor
            )
        }
    }
}

@Preview(name = "MacroProgressBar - Empty", showBackground = true)
@Composable
private fun MacroProgressBarEmptyPreview() {
    FoodTrackerTheme {
        Surface {
            Box(modifier = Modifier.padding(16.dp)) {
                MacroProgressBar(
                    label = "Белки",
                    value = 0f,
                    max = 140f,
                    unit = "г",
                    color = ProteinColor
                )
            }
        }
    }
}

@Preview(name = "MacroProgressBar - Full", showBackground = true)
@Composable
private fun MacroProgressBarFullPreview() {
    FoodTrackerTheme {
        Surface {
            Box(modifier = Modifier.padding(16.dp)) {
                MacroProgressBar(
                    label = "Углеводы",
                    value = 275f,
                    max = 275f,
                    unit = "г",
                    color = CarbsColor
                )
            }
        }
    }
}
