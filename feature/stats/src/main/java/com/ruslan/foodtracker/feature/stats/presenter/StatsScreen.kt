package com.ruslan.foodtracker.feature.stats.presenter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ruslan.foodtracker.core.ui.components.BarChart
import com.ruslan.foodtracker.core.ui.theme.*

@Composable
fun StatsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val headerBrush = remember { Brush.verticalGradient(listOf(Primary, PrimaryDark)) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBrush)
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .padding(20.dp)
        ) {
            Text("Статистика", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.ExtraBold, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            BarChart(
                data = uiState.weekCalories,
                height = 130.dp,
                defaultColor = Color.White,
                gradientColors = listOf(Color.White.copy(0.9f), Color.White.copy(0.5f))
            )
        }

        Spacer(Modifier.height(16.dp))

        // Average stats
        Text("Средние показатели", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(10.dp))

        Row(modifier = Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("🔥", "Калории", "${uiState.avgCalories.toInt()}", "ккал/день", Primary, Modifier.weight(1f))
            StatCard("🥩", "Белки", "${uiState.avgProtein.toInt()}", "г/день", ProteinColor, Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("🥑", "Жиры", "${uiState.avgFat.toInt()}", "г/день", FatColor, Modifier.weight(1f))
            StatCard("🍞", "Углеводы", "${uiState.avgCarbs.toInt()}", "г/день", CarbsColor, Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCard(icon: String, label: String, value: String, unit: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(icon, fontSize = 20.sp)
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(unit, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
