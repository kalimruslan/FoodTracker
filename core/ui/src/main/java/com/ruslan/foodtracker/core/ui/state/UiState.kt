package com.ruslan.foodtracker.core.ui.state

import com.ruslan.foodtracker.domain.model.NetworkResult

/**
 * Базовый UI State для экранов с данными
 */
sealed class UiState<out T> {
    /**
     * Начальное состояние
     */
    data object Idle : UiState<Nothing>()

    /**
     * Загрузка данных
     */
    data object Loading : UiState<Nothing>()

    /**
     * Успешная загрузка с данными
     */
    data class Success<T>(val data: T) : UiState<T>()

    /**
     * Ошибка при загрузке
     */
    data class Error(val message: String) : UiState<Nothing>()

    /**
     * Пустой результат
     */
    data object Empty : UiState<Nothing>()
}

/**
 * Конвертация NetworkResult в UiState
 */
fun <T> NetworkResult<T>.toUiState(): UiState<T> = when (this) {
    is NetworkResult.Success -> UiState.Success(data)
    is NetworkResult.Error -> UiState.Error(message)
    is NetworkResult.Loading -> UiState.Loading
    is NetworkResult.Empty -> UiState.Empty
}

/**
 * Extension функции для UiState
 */

/**
 * Проверка на успешное состояние
 */
fun <T> UiState<T>.isSuccess(): Boolean = this is UiState.Success

/**
 * Проверка на загрузку
 */
fun <T> UiState<T>.isLoading(): Boolean = this is UiState.Loading

/**
 * Проверка на ошибку
 */
fun <T> UiState<T>.isError(): Boolean = this is UiState.Error

/**
 * Получение данных или null
 */
fun <T> UiState<T>.getOrNull(): T? = when (this) {
    is UiState.Success -> data
    else -> null
}
