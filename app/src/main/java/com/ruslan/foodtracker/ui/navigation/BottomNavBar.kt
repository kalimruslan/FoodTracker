package com.ruslan.foodtracker.ui.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ruslan.foodtracker.core.ui.theme.FoodTrackerTheme
import com.ruslan.foodtracker.core.ui.theme.Primary

/**
 * Bottom Navigation Bar для Food Tracker
 * 4 таба: Главная, Поиск, Статистика, Профиль
 */
@Composable
fun FoodTrackerBottomBar(
    currentRoute: NavRoutes?,
    onNavigate: (NavRoutes) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        BottomNavItem.entries.forEach { item ->
            val isSelected = when (currentRoute) {
                is NavRoutes.Home -> item == BottomNavItem.HOME
                is NavRoutes.Search -> item == BottomNavItem.SEARCH
                is NavRoutes.Stats -> item == BottomNavItem.STATS
                is NavRoutes.Profile -> item == BottomNavItem.PROFILE
                else -> false
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier.size(22.dp)
                        )

                        // Точка-индикатор для активного таба
                        if (isSelected) {
                            Canvas(
                                modifier = Modifier
                                    .size(4.dp)
                                    .padding(top = 1.dp)
                            ) {
                                drawCircle(color = Primary)
                            }
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    selectedTextColor = Primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    indicatorColor = Color.Transparent // Убираем стандартный индикатор
                )
            )
        }
    }
}

/**
 * Элементы Bottom Navigation
 */
private enum class BottomNavItem(
    val route: NavRoutes,
    val icon: ImageVector,
    val label: String
) {
    HOME(
        route = NavRoutes.Home,
        icon = Icons.Filled.Home,
        label = "Главная"
    ),
    SEARCH(
        route = NavRoutes.Search,
        icon = Icons.Filled.Search,
        label = "Поиск"
    ),
    STATS(
        route = NavRoutes.Stats,
        icon = Icons.Filled.BarChart,
        label = "Статистика"
    ),
    PROFILE(
        route = NavRoutes.Profile,
        icon = Icons.Filled.Person,
        label = "Профиль"
    )
}

/**
 * Проверяет, должен ли отображаться Bottom Navigation Bar для данного маршрута
 */
fun shouldShowBottomBar(currentRoute: NavRoutes?): Boolean {
    return when (currentRoute) {
        is NavRoutes.Home,
        is NavRoutes.Search,
        is NavRoutes.Stats,
        is NavRoutes.Profile -> true
        is NavRoutes.ProductDetail -> false // Скрываем на детальной странице продукта
        null -> true
    }
}

// ========== Preview ==========

@Preview(name = "Bottom Nav Bar - Light", showBackground = true)
@Composable
private fun BottomNavBarPreviewLight() {
    FoodTrackerTheme(darkTheme = false) {
        Surface {
            Column {
                Spacer(modifier = Modifier.weight(1f))
                FoodTrackerBottomBar(
                    currentRoute = NavRoutes.Home,
                    onNavigate = {}
                )
            }
        }
    }
}

@Preview(name = "Bottom Nav Bar - Dark", showBackground = true)
@Composable
private fun BottomNavBarPreviewDark() {
    FoodTrackerTheme(darkTheme = true) {
        Surface {
            Column {
                Spacer(modifier = Modifier.weight(1f))
                FoodTrackerBottomBar(
                    currentRoute = NavRoutes.Search,
                    onNavigate = {}
                )
            }
        }
    }
}

@Preview(name = "Bottom Nav Bar - Stats Selected", showBackground = true)
@Composable
private fun BottomNavBarPreviewStats() {
    FoodTrackerTheme {
        Surface {
            Column {
                Spacer(modifier = Modifier.weight(1f))
                FoodTrackerBottomBar(
                    currentRoute = NavRoutes.Stats,
                    onNavigate = {}
                )
            }
        }
    }
}
