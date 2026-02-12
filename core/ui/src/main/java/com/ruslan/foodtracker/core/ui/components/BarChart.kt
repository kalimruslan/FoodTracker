package com.ruslan.foodtracker.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruslan.foodtracker.core.ui.theme.*
import kotlin.math.max

/**
 * Модель данных для столбца диаграммы
 */
data class BarData(
    val label: String,
    val value: Float,
    val color: Color? = null
)

/**
 * Столбчатая диаграмма
 * Используется для отображения статистики калорий и динамики веса
 *
 * @param data данные для столбцов
 * @param modifier модификатор
 * @param height высота диаграммы
 * @param showValues показывать значения над столбцами
 * @param showLabels показывать подписи под столбцами
 * @param maxValue максимальное значение для шкалы (если null, вычисляется автоматически)
 * @param defaultColor цвет по умолчанию для столбцов (если не указан в BarData)
 * @param gradientColors градиент для столбцов (если null, используется сплошной цвет)
 */
@Composable
fun BarChart(
    data: List<BarData>,
    modifier: Modifier = Modifier,
    height: Dp = 130.dp,
    showValues: Boolean = true,
    showLabels: Boolean = true,
    maxValue: Float? = null,
    defaultColor: Color = Primary,
    gradientColors: List<Color>? = null
) {
    if (data.isEmpty()) return

    val maxVal = maxValue ?: data.maxOf { it.value }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Столбцы диаграммы
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { barData ->
                BarColumn(
                    data = barData,
                    maxValue = max(maxVal, 1f),
                    showValue = showValues,
                    defaultColor = defaultColor,
                    gradientColors = gradientColors,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Подписи под столбцами
        if (showLabels) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                data.forEach { barData ->
                    Text(
                        text = barData.label,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Отдельный столбец диаграммы
 */
@Composable
private fun BarColumn(
    data: BarData,
    maxValue: Float,
    showValue: Boolean,
    defaultColor: Color,
    gradientColors: List<Color>?,
    modifier: Modifier = Modifier
) {
    val heightFraction = if (maxValue > 0) (data.value / maxValue) else 0f

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Значение над столбцом
        if (showValue) {
            Text(
                text = data.value.toInt().toString(),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // Столбец
        Canvas(
            modifier = Modifier
                .width(30.dp)
                .fillMaxHeight(heightFraction.coerceIn(0.1f, 1f)) // Минимум 10% для видимости
        ) {
            val barWidth = size.width * 0.7f // 70% ширины для столбца
            val barHeight = size.height
            val cornerRadius = 8.dp.toPx()

            val barColor = data.color ?: defaultColor

            val brush = if (gradientColors != null && gradientColors.size >= 2) {
                Brush.verticalGradient(
                    colors = gradientColors,
                    startY = 0f,
                    endY = barHeight
                )
            } else {
                Brush.verticalGradient(
                    colors = listOf(barColor, barColor.copy(alpha = 0.8f)),
                    startY = 0f,
                    endY = barHeight
                )
            }

            drawRoundRect(
                brush = brush,
                topLeft = Offset((size.width - barWidth) / 2, 0f),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
            )
        }
    }
}

// ========== Preview ==========

@Preview(name = "BarChart - Калории недели", showBackground = true, backgroundColor = 0xFFFF6B35)
@Composable
private fun BarChartPreviewCalories() {
    FoodTrackerTheme {
        Surface(
            color = Primary,
            modifier = Modifier.padding(16.dp)
        ) {
            BarChart(
                data = listOf(
                    BarData("Пн", 1850f),
                    BarData("Вт", 2100f),
                    BarData("Ср", 1950f),
                    BarData("Чт", 2250f, color = Danger), // Превышение
                    BarData("Пт", 1780f),
                    BarData("Сб", 2050f),
                    BarData("Вс", 1680f)
                ),
                height = 130.dp,
                showValues = true,
                showLabels = true,
                defaultColor = Color.White,
                gradientColors = listOf(
                    Color.White.copy(alpha = 0.9f),
                    Color.White.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Preview(name = "BarChart - Динамика веса", showBackground = true)
@Composable
private fun BarChartPreviewWeight() {
    FoodTrackerTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            BarChart(
                data = listOf(
                    BarData("Янв", 135f),
                    BarData("Фев", 133.5f),
                    BarData("Мар", 132f),
                    BarData("Апр", 131.5f),
                    BarData("Май", 130.8f),
                    BarData("Июн", 130.2f),
                    BarData("Июл", 130f)
                ),
                height = 120.dp,
                showValues = true,
                showLabels = true,
                defaultColor = Primary.copy(alpha = 0.3f),
                gradientColors = null
            )
        }
    }
}

@Preview(name = "BarChart - Текущий месяц выделен", showBackground = true)
@Composable
private fun BarChartPreviewCurrentMonth() {
    FoodTrackerTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            BarChart(
                data = listOf(
                    BarData("Янв", 135f, color = Primary.copy(alpha = 0.2f)),
                    BarData("Фев", 133.5f, color = Primary.copy(alpha = 0.2f)),
                    BarData("Мар", 132f, color = Primary.copy(alpha = 0.3f)),
                    BarData("Апр", 131.5f, color = Primary.copy(alpha = 0.3f)),
                    BarData("Май", 130.8f, color = Primary.copy(alpha = 0.4f)),
                    BarData("Июн", 130.2f, color = Primary.copy(alpha = 0.4f)),
                    BarData("Июл", 130f, color = Primary) // Текущий месяц
                ),
                height = 120.dp,
                showValues = true,
                showLabels = true
            )
        }
    }
}

@Preview(name = "BarChart - Dark Theme", showBackground = true)
@Composable
private fun BarChartPreviewDark() {
    FoodTrackerTheme(darkTheme = true) {
        Surface(modifier = Modifier.padding(16.dp)) {
            BarChart(
                data = listOf(
                    BarData("Пн", 1850f),
                    BarData("Вт", 2100f),
                    BarData("Ср", 1950f),
                    BarData("Чт", 1780f),
                    BarData("Пт", 2050f),
                    BarData("Сб", 1680f),
                    BarData("Вс", 2200f)
                ),
                height = 130.dp,
                showValues = true,
                showLabels = true,
                defaultColor = Primary
            )
        }
    }
}
