package com.ruslan.foodtracker.feature.product.presenter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ruslan.foodtracker.core.ui.components.CircularProgress
import com.ruslan.foodtracker.core.ui.components.NutrientGrid
import com.ruslan.foodtracker.core.ui.theme.*
import com.ruslan.foodtracker.feature.product.presenter.components.MealSelectionDialog

@Composable
fun ProductDetailScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ProductDetailContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onWeightChanged = viewModel::onWeightChanged,
        onUnitSelected = viewModel::onUnitSelected,
        onShowMealDialog = viewModel::onShowMealDialog,
        onDismissMealDialog = viewModel::onDismissMealDialog,
        onMealSelected = { mealType ->
            viewModel.onMealSelected(mealType, onNavigateBack)
        },
        modifier = modifier
    )
}

@Composable
private fun ProductDetailContent(
    uiState: ProductDetailUiState,
    onNavigateBack: () -> Unit,
    onWeightChanged: (String) -> Unit,
    onUnitSelected: (PortionUnit) -> Unit,
    onShowMealDialog: () -> Unit,
    onDismissMealDialog: () -> Unit,
    onMealSelected: (com.ruslan.foodtracker.domain.model.MealType) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Loading состояние
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return
        }

        // Error состояние
        if (uiState.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = uiState.error,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = onNavigateBack) {
                        Text("Назад")
                    }
                }
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            // Header with gradient
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(remember { Brush.verticalGradient(listOf(Primary, PrimaryDark)) })
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                    Text("Продукт", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.width(48.dp))
                }

                Spacer(Modifier.height(20.dp))

                Text(uiState.name, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text(uiState.brand, fontSize = 13.sp, color = Color.White.copy(0.8f))

                Spacer(Modifier.height(20.dp))

                CircularProgress(
                    value = uiState.calories,
                    max = 500f,
                    size = 150.dp,
                    strokeWidth = 12.dp,
                    color = Color.White
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.calories.toInt().toString(), style = MaterialTheme.typography.displayLarge, color = Color.White)
                        Text("ккал", fontSize = 12.sp, color = Color.White.copy(0.8f))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Portion selector
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Порция:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    BasicTextField(
                        value = uiState.weight,
                        onValueChange = onWeightChanged,
                        modifier = Modifier
                            .width(60.dp)
                            .border(2.dp, Primary.copy(0.3f), RoundedCornerShape(10.dp))
                            .padding(8.dp),
                        textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        PortionUnit.entries.forEach { unit ->
                            val onUnitClick = remember(unit) { { onUnitSelected(unit) } }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (uiState.selectedUnit == unit) Primary else Color(0xFFF3F4F6))
                                    .clickable(onClick = onUnitClick)
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    unit.label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (uiState.selectedUnit == unit) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Nutrients
            NutrientGrid(
                calories = uiState.calories.toInt().toString(),
                protein = String.format("%.1f", uiState.protein),
                fat = String.format("%.1f", uiState.fat),
                carbs = String.format("%.1f", uiState.carbs),
                fiber = String.format("%.1f", uiState.fiber),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Add button
        Button(
            onClick = onShowMealDialog,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text("Добавить в дневник", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
        }

        // Meal selection dialog
        if (uiState.showMealDialog) {
            MealSelectionDialog(
                onMealSelected = onMealSelected,
                onDismiss = onDismissMealDialog
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductDetailPreview() {
    FoodTrackerTheme {
        ProductDetailContent(
            uiState = ProductDetailUiState(
                name = "Куриная грудка",
                brand = "без бренда",
                caloriesPer100g = 110f,
                proteinPer100g = 23.1f,
                fatPer100g = 1.2f,
                weight = "150",
                multiplier = 1.5f
            ),
            onNavigateBack = {},
            onWeightChanged = {},
            onUnitSelected = {},
            onShowMealDialog = {},
            onDismissMealDialog = {},
            onMealSelected = {}
        )
    }
}
