package com.ruslan.foodtracker.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.ruslan.foodtracker.core.ui.components.FoodTrackerToggle
import com.ruslan.foodtracker.core.ui.theme.*

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Primary, PrimaryDark)))
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Профиль", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(20.dp))
            Box(Modifier.size(80.dp).clip(CircleShape).background(Color.White.copy(0.2f)), contentAlignment = Alignment.Center) {
                Text("👤", fontSize = 36.sp)
            }
            Spacer(Modifier.height(12.dp))
            Text(uiState.name, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text("Цель: ${uiState.targetCalories} ккал/день", fontSize = 13.sp, color = Color.White.copy(0.8f))
        }

        Spacer(Modifier.height(16.dp))

        Text("Динамика веса", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(10.dp))

        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                BarChart(data = uiState.weightHistory, height = 120.dp, defaultColor = Primary)
                Spacer(Modifier.height(12.dp))
                Text("↓ -5 кг за 7 месяцев", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Success)
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("Напоминания", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(10.dp))

        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp)) {
            Column {
                listOf("Завтрак — 08:00", "Обед — 13:00", "Ужин — 19:00").forEachIndexed { i, text ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text, fontSize = 14.sp)
                        FoodTrackerToggle(checked = true, onCheckedChange = {})
                    }
                    if (i < 2) HorizontalDivider()
                }
            }
        }
    }
}
