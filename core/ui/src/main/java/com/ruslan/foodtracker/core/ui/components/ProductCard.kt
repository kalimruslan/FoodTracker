package com.ruslan.foodtracker.core.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruslan.foodtracker.core.ui.theme.*

/**
 * Модель данных для продукта
 */
data class ProductData(
    val id: String,
    val name: String,
    val brand: String?,
    val portion: String,
    val calories: Int,
    val protein: Float,
    val fat: Float,
    val carbs: Float,
    val isFavorite: Boolean = false,
    val localFoodId: Long = 0L,
    val servingSizeGrams: Double = 100.0,
    val servingUnit: String = "g"
)

/**
 * Карточка продукта в списке поиска
 * Отображает название, бренд, макронутриенты (Б/Ж/У), калории и кнопку избранного
 *
 * @param product данные продукта
 * @param onClick обработчик клика по карточке
 * @param onFavoriteClick обработчик клика по кнопке избранного
 * @param modifier модификатор
 */
@Composable
fun ProductCard(
    product: ProductData,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Левая часть: название, бренд, макронутриенты
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Название продукта
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                // Бренд + порция
                Text(
                    text = "${product.brand ?: "без бренда"} · ${product.portion}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Макронутриенты: Б/Ж/У
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MacroText(
                        label = "Б",
                        value = product.protein,
                        color = ProteinColor
                    )
                    MacroText(
                        label = "Ж",
                        value = product.fat,
                        color = FatColor
                    )
                    MacroText(
                        label = "У",
                        value = product.carbs,
                        color = CarbsColor
                    )
                }
            }

            // Правая часть: калории + кнопка избранного
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Калории
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = product.calories.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Primary
                    )
                    Text(
                        text = "ккал",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Кнопка избранного
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (product.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription = if (product.isFavorite) "Удалить из избранного" else "Добавить в избранное",
                        tint = if (product.isFavorite) Accent else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Компонент для отображения макронутриента (Б/Ж/У)
 */
@Composable
private fun MacroText(
    label: String,
    value: Float,
    color: androidx.compose.ui.graphics.Color
) {
    Text(
        text = "$label ${String.format("%.1f", value)}г",
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = color
    )
}

// ========== Preview ==========

@Preview(name = "ProductCard - Обычная", showBackground = true)
@Composable
private fun ProductCardPreview() {
    FoodTrackerTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            ProductCard(
                product = ProductData(
                    id = "1",
                    name = "Куриная грудка",
                    brand = "без бренда",
                    portion = "100г",
                    calories = 110,
                    protein = 23.1f,
                    fat = 1.2f,
                    carbs = 0f,
                    isFavorite = false
                ),
                onClick = {},
                onFavoriteClick = {}
            )
        }
    }
}

@Preview(name = "ProductCard - В избранном", showBackground = true)
@Composable
private fun ProductCardPreviewFavorite() {
    FoodTrackerTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            ProductCard(
                product = ProductData(
                    id = "2",
                    name = "Творог 5%",
                    brand = "Домик в деревне",
                    portion = "100г",
                    calories = 121,
                    protein = 17.2f,
                    fat = 5.0f,
                    carbs = 1.8f,
                    isFavorite = true
                ),
                onClick = {},
                onFavoriteClick = {}
            )
        }
    }
}

@Preview(name = "ProductCard - Длинное название", showBackground = true)
@Composable
private fun ProductCardPreviewLongName() {
    FoodTrackerTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            ProductCard(
                product = ProductData(
                    id = "3",
                    name = "Овсяная каша на молоке с мёдом и орехами",
                    brand = "Быстров",
                    portion = "100г",
                    calories = 88,
                    protein = 3.0f,
                    fat = 1.7f,
                    carbs = 15.0f,
                    isFavorite = false
                ),
                onClick = {},
                onFavoriteClick = {}
            )
        }
    }
}

@Preview(name = "ProductCard - Dark Theme", showBackground = true)
@Composable
private fun ProductCardPreviewDark() {
    FoodTrackerTheme(darkTheme = true) {
        Surface(modifier = Modifier.padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProductCard(
                    product = ProductData(
                        id = "4",
                        name = "Рис бурый варёный",
                        brand = null,
                        portion = "100г",
                        calories = 120,
                        protein = 2.6f,
                        fat = 0.8f,
                        carbs = 24.7f,
                        isFavorite = false
                    ),
                    onClick = {},
                    onFavoriteClick = {}
                )

                ProductCard(
                    product = ProductData(
                        id = "5",
                        name = "Гречневая каша",
                        brand = "Мистраль",
                        portion = "100г",
                        calories = 132,
                        protein = 4.5f,
                        fat = 2.3f,
                        carbs = 25.0f,
                        isFavorite = true
                    ),
                    onClick = {},
                    onFavoriteClick = {}
                )
            }
        }
    }
}
