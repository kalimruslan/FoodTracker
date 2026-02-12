package com.ruslan.foodtracker.ui.profile

import androidx.lifecycle.ViewModel
import com.ruslan.foodtracker.core.ui.components.BarData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadMockData()
    }

    private fun loadMockData() {
        _uiState.value = ProfileUiState(
            name = "Пользователь",
            targetCalories = 2200,
            weight = 130f,
            weightHistory = listOf(
                BarData("Янв", 135f),
                BarData("Фев", 133.5f),
                BarData("Мар", 132f),
                BarData("Апр", 131.5f),
                BarData("Май", 130.8f),
                BarData("Июн", 130.2f),
                BarData("Июл", 130f)
            )
        )
    }
}

data class ProfileUiState(
    val name: String = "",
    val targetCalories: Int = 2200,
    val weight: Float = 0f,
    val weightHistory: List<BarData> = emptyList()
)
