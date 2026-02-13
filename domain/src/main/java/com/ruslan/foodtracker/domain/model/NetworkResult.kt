package com.ruslan.foodtracker.domain.model

import com.ruslan.foodtracker.domain.error.DomainError

/**
 * Класс для обработки результатов сетевых запросов и асинхронных операций
 */
sealed class NetworkResult<out T> {
    /**
     * Успешный результат с данными
     */
    data class Success<T>(val data: T) : NetworkResult<T>()

    /**
     * Ошибка с типизированным DomainError
     *
     * @property error - типизированная ошибка (Database, Network, Validation, Data)
     * @property exception - исходное исключение (если есть)
     */
    data class Error(
        val error: DomainError,
        val exception: Throwable? = null
    ) : NetworkResult<Nothing>() {

        /**
         * Deprecated конструктор для обратной совместимости
         * Используйте конструктор с DomainError вместо String message
         *
         * @param message - сообщение об ошибке (будет обёрнуто в DomainError.Data.ParseError)
         * @param code - HTTP код ошибки (игнорируется)
         * @param exception - исходное исключение
         */
        @Deprecated(
            message = "Use constructor with DomainError instead of String message",
            replaceWith = ReplaceWith("Error(DomainError.Network.Unknown, exception)"),
            level = DeprecationLevel.WARNING
        )
        constructor(
            message: String,
            code: Int? = null,
            exception: Throwable? = null
        ) : this(
            error = when {
                // Попытка умного маппинга старых строк в DomainError
                message.contains("подключения", ignoreCase = true) ||
                message.contains("connection", ignoreCase = true) -> DomainError.Network.NoConnection

                message.contains("не найден", ignoreCase = true) ||
                message.contains("not found", ignoreCase = true) -> DomainError.Data.ProductNotFound

                code != null && code >= 500 -> DomainError.Network.ServerError
                code != null && code >= 400 -> DomainError.Network.HttpError(code)

                else -> DomainError.Network.Unknown
            },
            exception = exception
        )

        /**
         * Получить legacy message для обратной совместимости
         * @deprecated Используйте error вместо message
         */
        @Deprecated("Use error property instead", ReplaceWith("error"))
        val message: String
            get() = error.toString()

        /**
         * Получить legacy code для обратной совместимости
         * @deprecated HTTP код теперь инкапсулирован в DomainError.Network.HttpError
         */
        @Deprecated("HTTP code is now part of DomainError.Network.HttpError")
        val code: Int?
            get() = when (error) {
                is DomainError.Network.HttpError -> error.code
                is DomainError.Network.ServerError -> 500
                else -> null
            }
    }

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
 * Выполнение действия при ошибке (новая версия с DomainError)
 */
inline fun <T> NetworkResult<T>.onError(action: (DomainError, Throwable?) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Error) {
        action(error, exception)
    }
    return this
}

/**
 * Выполнение действия при ошибке (deprecated версия для обратной совместимости)
 * @deprecated Используйте версию с DomainError
 */
@Deprecated(
    message = "Use onError with DomainError instead",
    replaceWith = ReplaceWith("onError { error, exception -> action(error.toString(), null, exception) }"),
    level = DeprecationLevel.WARNING
)
inline fun <T> NetworkResult<T>.onErrorLegacy(action: (String, Int?, Throwable?) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Error) {
        @Suppress("DEPRECATION")
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
    is NetworkResult.Error -> NetworkResult.Error(error, exception)
    is NetworkResult.Loading -> NetworkResult.Loading
    is NetworkResult.Empty -> NetworkResult.Empty
}
