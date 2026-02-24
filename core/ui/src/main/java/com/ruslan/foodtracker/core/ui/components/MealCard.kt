package com.ruslan.foodtracker.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruslan.foodtracker.core.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Максимальный люфт вправо при свайп-жесте (px) для UX-плавности */
private const val SWIPE_OVERSHOOT_PX = 16f

/**
 * Модель данных для продукта в приёме пищи
 */
data class FoodItemData(
    val name: String,
    val weight: String,
    val calories: Int,
    val entryId: Long = 0L,
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
 * @param onDeleteItem колбэк удаления продукта по entryId (null — свайп недоступен)
 * @param onEditItem колбэк редактирования граммовки по entryId (null — свайп недоступен)
 */
@Composable
fun MealCard(
    emoji: String,
    name: String,
    time: String?,
    totalCalories: Int,
    foodItems: List<FoodItemData>,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    onDeleteItem: ((Long) -> Unit)? = null,
    onEditItem: ((Long) -> Unit)? = null,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
            ).border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp),
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
        ) {
            MealCardHeader(
                emoji = emoji,
                name = name,
                time = time,
                totalCalories = totalCalories,
                onAddClick = onAddClick,
            )
            if (foodItems.isNotEmpty()) {
                MealCardFoodList(
                    foodItems = foodItems,
                    onDeleteItem = onDeleteItem,
                    onEditItem = onEditItem,
                )
            }
        }
    }
}

@Composable
private fun MealCardHeader(
    emoji: String,
    name: String,
    time: String?,
    totalCalories: Int,
    onAddClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Левая часть: emoji + название + время
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = emoji,
                fontSize = 24.sp,
            )
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                if (time != null) {
                    Text(
                        text = time,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Правая часть: калории + кнопка "+"
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (totalCalories > 0) {
                Text(
                    text = "$totalCalories ккал",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                )
            }
            MealCardAddButton(onAddClick = onAddClick)
        }
    }
}

@Composable
private fun MealCardAddButton(onAddClick: () -> Unit) {
    val addButtonBrush = remember { Brush.linearGradient(colors = listOf(Primary, PrimaryLight)) }
    FilledIconButton(
        onClick = onAddClick,
        modifier = Modifier
            .size(32.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(10.dp),
                ambientColor = Primary.copy(alpha = 0.4f),
            ).clip(RoundedCornerShape(10.dp))
            .background(brush = addButtonBrush),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
        ),
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Добавить продукт",
            tint = androidx.compose.ui.graphics.Color.White,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun MealCardFoodList(
    foodItems: List<FoodItemData>,
    onDeleteItem: ((Long) -> Unit)?,
    onEditItem: ((Long) -> Unit)?,
) {
    Column {
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(2.dp))

        foodItems.forEach { item ->
            if (onDeleteItem != null || onEditItem != null) {
                SwipeableActionFoodRow(
                    name = item.name,
                    weight = item.weight,
                    calories = item.calories,
                    onDelete = { onDeleteItem?.invoke(item.entryId) },
                    onEdit = { onEditItem?.invoke(item.entryId) },
                )
            } else {
                FoodItemRow(
                    name = item.name,
                    weight = item.weight,
                    calories = item.calories,
                )
            }
        }
    }
}

/**
 * Строка с продуктом и свайп-жестом влево для открытия кнопок "Редактировать" и "Удалить".
 * После свайпа кнопки остаются видимыми и тапабельными. Свайп вправо закрывает панель.
 */
@Composable
private fun SwipeableActionFoodRow(
    name: String,
    weight: String,
    calories: Int,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
) {
    val density = LocalDensity.current
    // Ширина панели с кнопками: 2 x 40dp + 4dp gap + 4dp padding = ~104dp
    val actionWidthPx = with(density) { 104.dp.toPx() }
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }

    Box(modifier = Modifier.fillMaxWidth()) {
        SwipeActionButtons(
            onEdit = {
                scope.launch { offsetX.animateTo(0f, spring()) }
                onEdit()
            },
            onDelete = {
                scope.launch { offsetX.animateTo(0f, spring()) }
                onDelete()
            },
            modifier = Modifier.align(Alignment.CenterEnd),
        )
        SwipeableFoodContent(
            name = name,
            weight = weight,
            calories = calories,
            offsetX = offsetX,
            actionWidthPx = actionWidthPx,
            scope = scope,
        )
    }
}

@Composable
private fun SwipeActionButtons(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.width(104.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Кнопка "Редактировать"
        IconButton(
            onClick = onEdit,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Редактировать",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
        // Кнопка "Удалить"
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Удалить",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
    }
}

@Suppress("LongMethod")
@Composable
private fun SwipeableFoodContent(
    name: String,
    weight: String,
    calories: Int,
    offsetX: Animatable<Float, AnimationVector1D>,
    actionWidthPx: Float,
    scope: CoroutineScope,
) {
    // Основной контент строки (смещается при свайпе)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 6.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        scope.launch {
                            // Снэп: открыть если прошли половину ширины панели, иначе закрыть
                            val target =
                                if (offsetX.value < -actionWidthPx / 2f) -actionWidthPx else 0f
                            offsetX.animateTo(
                                target,
                                spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            )
                        }
                    },
                    onDragCancel = {
                        scope.launch { offsetX.animateTo(0f, spring()) }
                    },
                    onHorizontalDrag = { _, delta ->
                        scope.launch {
                            // Свайп только влево; небольшой люфт вправо для UX
                            offsetX.snapTo(
                                (offsetX.value + delta).coerceIn(-actionWidthPx, SWIPE_OVERSHOOT_PX),
                            )
                        }
                    },
                )
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = name,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = weight,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = calories.toString(),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Строка с продуктом внутри карточки приёма пищи (без свайпа)
 */
@Composable
private fun FoodItemRow(
    name: String,
    weight: String,
    calories: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = name,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = weight,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text(
            text = calories.toString(),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ========== Preview ==========

@Preview(name = "MealCard - С продуктами и свайпом", showBackground = true)
@Composable
private fun MealCardPreviewWithSwipe() {
    FoodTrackerTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            MealCard(
                emoji = "🌅",
                name = "Завтрак",
                time = "08:00",
                totalCalories = 303,
                foodItems = listOf(
                    FoodItemData("Овсяная каша", "200г", 150, entryId = 1L),
                    FoodItemData("Банан", "1 шт", 89, entryId = 2L),
                    FoodItemData("Мёд", "1 ст.л.", 64, entryId = 3L),
                ),
                onAddClick = {},
                onDeleteItem = {},
                onEditItem = {},
            )
        }
    }
}

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
                    FoodItemData("Мёд", "1 ст.л.", 64),
                ),
                onAddClick = {},
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
                onAddClick = {},
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
                onAddClick = {},
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
                    FoodItemData("Рис бурый", "180г", 216),
                ),
                onAddClick = {},
                onDeleteItem = {},
                onEditItem = {},
            )
        }
    }
}
