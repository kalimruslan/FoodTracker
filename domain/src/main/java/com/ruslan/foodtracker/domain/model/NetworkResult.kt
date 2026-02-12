package com.ruslan.foodtracker.domain.model

/**
 * Класс для обработки результатов сетевых запросов и асинхронных операций
 */
sealed class NetworkResult<out T> {
    /**
     * Успешный результат с данными
     */
    data class Success<T>(val data: T) : NetworkResult<T>()

    /**
     * Ошибка с сообщением и опциональным кодом ошибки
     */
    data class Error(
        val message: String,
        val code: Int? = null,
        val exception: Throwable? = null
    ) : NetworkResult<Nothing>()

    /**
     * Состояние загрузки
     */
    data object Loading : NetworkResult<Nothing>()

    /**
     * Пустой результат (например, для DELETE операций)
     */
    data object Empty : NetworkResult<Nothing>()
}

/**
 * Extension функции для удобной работы с NetworkResult
 */

/**
 * Проверка на успешный результат
 */
fun <T> NetworkResult<T>.isSuccess(): Boolean = this is NetworkResult.Success

/**
 * Проверка на ошибку
 */
fun <T> NetworkResult<T>.isError(): Boolean = this is NetworkResult.Error

/**
 * Проверка на загрузку
 */
fun <T> NetworkResult<T>.isLoading(): Boolean = this is NetworkResult.Loading

/**
 * Получение данных или null
 */
fun <T> NetworkResult<T>.getOrNull(): T? = when (this) {
    is NetworkResult.Success -> data
    else -> null
}

/**
 * Выполнение действия при успехе
 */
inline fun <T> NetworkResult<T>.onSuccess(action: (T) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success) {
        action(data)
    }
    return this
}

/**
 * Выполнение действия при ошибке
 */
inline fun <T> NetworkResult<T>.onError(action: (String, Int?, Throwable?) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Error) {
        action(message, code, exception)
    }
    return this
}

/**
 * Выполнение действия при загрузке
 */
inline fun <T> NetworkResult<T>.onLoading(action: () -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Loading) {
        action()
    }
    return this
}

/**
 * Маппинг данных при успехе
 */
inline fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> = when (this) {
    is NetworkResult.Success -> NetworkResult.Success(transform(data))
    is NetworkResult.Error -> NetworkResult.Error(message, code, exception)
    is NetworkResult.Loading -> NetworkResult.Loading
    is NetworkResult.Empty -> NetworkResult.Empty
}
