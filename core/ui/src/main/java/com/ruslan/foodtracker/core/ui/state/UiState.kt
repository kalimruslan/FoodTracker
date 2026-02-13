package com.ruslan.foodtracker.core.ui.state

import android.content.Context
import com.ruslan.foodtracker.core.ui.error.toMessage
import com.ruslan.foodtracker.domain.error.DomainError
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
     * Ошибка при загрузке с типизированной ошибкой
     *
     * @property error - типизированная ошибка из domain слоя
     */
    data class Error(val error: DomainError) : UiState<Nothing>() {
        /**
         * Deprecated конструктор для обратной совместимости
         *
         * @param message - текстовое сообщение (будет обёрнуто в DomainError.Data.ParseError)
         */
        @Deprecated(
            message = "Use constructor with DomainError instead of String message",
            replaceWith = ReplaceWith("Error(DomainError.Data.ParseError)"),
            level = DeprecationLevel.WARNING
        )
        constructor(message: String) : this(DomainError.Data.ParseError)

        /**
         * Получить legacy message для обратной совместимости
         * @deprecated Используйте error.toMessage(context) вместо message
         */
        @Deprecated(
            message = "Use error.toMessage(context) for localized message",
            replaceWith = ReplaceWith("error.toMessage(context)"),
            level = DeprecationLevel.WARNING
        )
        val message: String
            get() = error.toString()

        /**
         * Получить локализованное сообщение об ошибке
         *
         * @param context - Android Context для доступа к ресурсам
         * @return Локализованное сообщение из строковых ресурсов
         */
        fun getMessage(context: Context): String = error.toMessage(context)
    }

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
    is NetworkResult.Error -> UiState.Error(error)
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
