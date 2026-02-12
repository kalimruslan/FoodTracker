package com.ruslan.foodtracker.ui.stats

import androidx.lifecycle.ViewModel
import com.ruslan.foodtracker.core.ui.components.BarData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadMockData()
    }

    private fun loadMockData() {
        _uiState.value = StatsUiState(
            weekCalories = listOf(
                BarData("Пн", 1850f),
                BarData("Вт", 2100f),
                BarData("Ср", 1950f),
                BarData("Чт", 2250f, color = com.ruslan.foodtracker.core.ui.theme.Danger),
                BarData("Пт", 1780f),
                BarData("Сб", 2050f),
                BarData("Вс", 1680f)
            ),
            avgCalories = 1951f,
            avgProtein = 98f,
            avgFat = 62f,
            avgCarbs = 215f
        )
    }
}

data class StatsUiState(
    val weekCalories: List<BarData> = emptyList(),
    val avgCalories: Float = 0f,
    val avgProtein: Float = 0f,
    val avgFat: Float = 0f,
    val avgCarbs: Float = 0f
)
