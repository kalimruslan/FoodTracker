package com.ruslan.foodtracker.core.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Preview компонент для демонстрации цветовой палитры и типографики
 * Food Tracker согласно дизайн-спецификации
 */
@Composable
private fun ThemePreviewContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Заголовок
        Text(
            text = "Food Tracker Theme",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Цветовая палитра - Primary
        ColorPaletteSection(
            title = "Primary Colors",
            colors = listOf(
                "Primary" to Primary,
                "Primary Light" to PrimaryLight,
                "Primary Dark" to PrimaryDark
            )
        )

        // Цветовая палитра - Secondary
        ColorPaletteSection(
            title = "Secondary Colors",
            colors = listOf(
                "Secondary" to Secondary,
                "Secondary Light" to SecondaryLight
            )
        )

        // Цветовая палитра - Нутриенты
        ColorPaletteSection(
            title = "Nutrient Colors",
            colors = listOf(
                "Protein" to ProteinColor,
                "Carbs" to CarbsColor,
                "Fat" to FatColor,
                "Fiber" to FiberColor
            )
        )

        // Типографика
        TypographySection()
    }
}

@Composable
private fun ColorPaletteSection(
    title: String,
    colors: List<Pair<String, Color>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            colors.forEach { (name, color) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(color, RoundedCornerShape(8.dp))
                    )
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun TypographySection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Typography",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "displayLarge - 36sp ExtraBold",
                style = MaterialTheme.typography.displayLarge,
                color = Primary,
                fontSize = 28.sp // уменьшен для preview
            )

            Text(
                text = "titleLarge - 20sp ExtraBold",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "titleMedium - 17sp ExtraBold",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "titleSmall - 15sp Bold",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "bodyMedium - 13sp Regular",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "labelMedium - 11sp SemiBold",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(name = "Light Theme", showBackground = true)
@Composable
private fun ThemePreviewLight() {
    FoodTrackerTheme(darkTheme = false) {
        Surface {
            ThemePreviewContent()
        }
    }
}

@Preview(name = "Dark Theme", showBackground = true)
@Composable
private fun ThemePreviewDark() {
    FoodTrackerTheme(darkTheme = true) {
        Surface {
            ThemePreviewContent()
        }
    }
}
