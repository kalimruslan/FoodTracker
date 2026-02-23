package com.ruslan.foodtracker.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ruslan.foodtracker.core.ui.theme.FoodTrackerTheme
import com.ruslan.foodtracker.core.ui.theme.Primary
import kotlin.math.min

/**
 * Кольцевая диаграмма прогресса (CircularProgressIndicator)
 * Используется для отображения калорий, макронутриентов и других показателей
 *
 * @param value текущее значение
 * @param max максимальное значение (цель)
 * @param modifier модификатор
 * @param size размер диаграммы
 * @param strokeWidth толщина кольца
 * @param color цвет прогресса
 * @param backgroundColor цвет фона (трек)
 * @param content контент внутри кольца (текст, цифры и т.д.)
 */
@Composable
fun CircularProgress(
    value: Float,
    max: Float,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    strokeWidth: Dp = 12.dp,
    color: Color = Primary,
    backgroundColor: Color = color.copy(alpha = 0.2f),
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Canvas для рисования кольцевой диаграммы
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasSize = size.toPx()
            val strokeWidthPx = strokeWidth.toPx()
            val radius = (canvasSize - strokeWidthPx) / 2f
            val center = canvasSize / 2f

            // Вычисляем прогресс (0.0 до 1.0)
            val progress = min(value / max, 1f).coerceIn(0f, 1f)
            val sweepAngle = 360f * progress

            // Рисуем фоновое кольцо (трек)
            drawCircle(
                color = backgroundColor,
                radius = radius,
                center = androidx.compose.ui.geometry
                    .Offset(center, center),
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )

            // Рисуем прогресс (закрашенная часть)
            if (progress > 0f) {
                drawArc(
                    color = color,
                    startAngle = -90f, // Начинаем сверху
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        strokeWidthPx / 2,
                        strokeWidthPx / 2
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        canvasSize - strokeWidthPx,
                        canvasSize - strokeWidthPx
                    ),
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }
        }

        // Контент внутри кольца
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

// ========== Preview ==========

@Preview(name = "CircularProgress - Калории", showBackground = true)
@Composable
private fun CircularProgressPreviewCalories() {
    FoodTrackerTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgress(
                value = 684f,
                max = 2200f,
                size = 130.dp,
                strokeWidth = 10.dp,
                color = Color.White,
                backgroundColor = Color.White.copy(alpha = 0.2f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "684",
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White
                    )
                    Text(
                        text = "из 2200 ккал",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Осталось 1516",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Preview(name = "CircularProgress - Primary", showBackground = true)
@Composable
private fun CircularProgressPreviewPrimary() {
    FoodTrackerTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgress(
                value = 165f,
                max = 500f,
                size = 150.dp,
                strokeWidth = 12.dp,
                color = Primary
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "165",
                        style = MaterialTheme.typography.displayLarge,
                        color = Primary
                    )
                    Text(
                        text = "ккал",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(name = "CircularProgress - Empty", showBackground = true)
@Composable
private fun CircularProgressPreviewEmpty() {
    FoodTrackerTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgress(
                value = 0f,
                max = 2200f,
                size = 120.dp,
                strokeWidth = 10.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.displayMedium,
                        color = Primary
                    )
                    Text(
                        text = "ккал",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Preview(name = "CircularProgress - Full", showBackground = true)
@Composable
private fun CircularProgressPreviewFull() {
    FoodTrackerTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgress(
                value = 2200f,
                max = 2200f,
                size = 120.dp,
                strokeWidth = 10.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "2200",
                        style = MaterialTheme.typography.displayMedium,
                        color = Primary
                    )
                    Text(
                        text = "ккал",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
