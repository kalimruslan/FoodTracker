package com.ruslan.foodtracker.core.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ruslan.foodtracker.core.ui.theme.FoodTrackerTheme
import com.ruslan.foodtracker.core.ui.theme.Primary

/**
 * Кастомный переключатель (Toggle/Switch)
 * Используется для напоминаний в профиле
 *
 * @param checked состояние (включён/выключен)
 * @param onCheckedChange обработчик изменения состояния
 * @param modifier модификатор
 * @param enabled активность переключателя
 */
@Composable
fun FoodTrackerToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 18.dp else 0.dp,
        animationSpec = tween(durationMillis = 200),
        label = "toggle_animation"
    )

    Box(
        modifier = modifier
            .width(44.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(
                color = if (checked) {
                    if (enabled) Primary else Primary.copy(alpha = 0.5f)
                } else {
                    if (enabled) Color(0xFFD1D5DB) else Color(0xFFD1D5DB).copy(alpha = 0.5f)
                }
            )
            .clickable(enabled = enabled) {
                onCheckedChange(!checked)
            }
            .padding(2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(22.dp)
                .shadow(
                    elevation = 1.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.15f)
                )
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

// ========== Preview ==========

@Preview(name = "Toggle - Включён", showBackground = true)
@Composable
private fun TogglePreviewOn() {
    FoodTrackerTheme {
        Surface(modifier = Modifier.padding(24.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Завтрак — 08:00",
                    style = MaterialTheme.typography.bodyMedium
                )
                FoodTrackerToggle(
                    checked = true,
                    onCheckedChange = {}
                )
            }
        }
    }
}

@Preview(name = "Toggle - Выключен", showBackground = true)
@Composable
private fun TogglePreviewOff() {
    FoodTrackerTheme {
        Surface(modifier = Modifier.padding(24.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Обед — 13:00",
                    style = MaterialTheme.typography.bodyMedium
                )
                FoodTrackerToggle(
                    checked = false,
                    onCheckedChange = {}
                )
            }
        }
    }
}

@Preview(name = "Toggle - Disabled", showBackground = true)
@Composable
private fun TogglePreviewDisabled() {
    FoodTrackerTheme {
        Surface(modifier = Modifier.padding(24.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Включён (disabled)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    FoodTrackerToggle(
                        checked = true,
                        onCheckedChange = {},
                        enabled = false
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Выключен (disabled)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    FoodTrackerToggle(
                        checked = false,
                        onCheckedChange = {},
                        enabled = false
                    )
                }
            }
        }
    }
}

@Preview(name = "Toggle - Interactive", showBackground = true)
@Composable
private fun TogglePreviewInteractive() {
    FoodTrackerTheme {
        Surface(modifier = Modifier.padding(24.dp)) {
            var checked by remember { mutableStateOf(false) }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Умные напоминания",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    FoodTrackerToggle(
                        checked = checked,
                        onCheckedChange = { checked = it }
                    )
                }

                Text(
                    text = "Состояние: ${if (checked) "Включено" else "Выключено"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(name = "Toggle - Dark Theme", showBackground = true)
@Composable
private fun TogglePreviewDark() {
    FoodTrackerTheme(darkTheme = true) {
        Surface(modifier = Modifier.padding(24.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Завтрак — 08:00")
                    FoodTrackerToggle(checked = true, onCheckedChange = {})
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Обед — 13:00")
                    FoodTrackerToggle(checked = false, onCheckedChange = {})
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Ужин — 19:00")
                    FoodTrackerToggle(checked = true, onCheckedChange = {})
                }
            }
        }
    }
}
