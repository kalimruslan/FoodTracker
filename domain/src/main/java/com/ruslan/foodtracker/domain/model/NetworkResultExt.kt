package com.ruslan.foodtracker.domain.model

import com.ruslan.foodtracker.domain.error.DomainError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Extension функции для удобной работы с NetworkResult
 * Паттерн взят из проекта ДОСААФ
 */

/**
 * Обработка всех состояний результата с отдельными callback'ами (новая версия с DomainError)
 *
 * @param onLoading - действие при загрузке
 * @param onError - действие при ошибке (получает типизированную ошибку)
 * @param onSuccess - действие при успехе (получает данные)
 */
fun <T> NetworkResult<T>.handleResult(
    onLoading: () -> Unit,
    onError: (DomainError) -> Unit,
    onSuccess: (data: T) -> Unit
) {
    when (this) {
        is NetworkResult.Loading -> onLoading()
        is NetworkResult.Error -> onError(error)
        is NetworkResult.Success -> onSuccess(data)
        is NetworkResult.Empty -> onError(DomainError.Data.Empty)
    }
}

/**
 * Обработка всех состояний результата (deprecated версия для обратной совместимости)
 * @deprecated Используйте версию с DomainError
 */
@Deprecated(
    message = "Use handleResult with DomainError instead",
    replaceWith = ReplaceWith("handleResult(onLoading, { error -> onError(error.toString()) }, onSuccess)"),
    level = DeprecationLevel.WARNING
)
fun <T> NetworkResult<T>.handleResultLegacy(
    onLoading: () -> Unit,
    onError: (String) -> Unit,
    onSuccess: (data: T) -> Unit
) {
    when (this) {
        is NetworkResult.Loading -> onLoading()
        is NetworkResult.Error -> onError(error.toString())
        is NetworkResult.Success -> onSuccess(data)
        is NetworkResult.Empty -> onError("Нет данных")
    }
}

/**
 * Выполнение действия ТОЛЬКО при успешном ответе
 * Остальные состояния игнорируются
 *
 * @param onSuccess - действие при успехе
 */
fun <T> NetworkResult<T>.doActionIfSuccess(onSuccess: (data: T) -> Unit) {
    when (this) {
        is NetworkResult.Success -> onSuccess(data)
        else -> { /* игнорируем */ }
    }
}

/**
 * Выполнение действия ТОЛЬКО при ошибке (новая версия с DomainError)
 * Остальные состояния игнорируются
 *
 * @param onError - действие при ошибке (получает типизированную ошибку)
 */
fun <T> NetworkResult<T>.doActionIfError(onError: (DomainError) -> Unit) {
    when (this) {
        is NetworkResult.Error -> onError(error)
        is NetworkResult.Empty -> onError(DomainError.Data.Empty)
        else -> { /* игнорируем */ }
    }
}

/**
 * Выполнение действия ТОЛЬКО при ошибке (deprecated версия)
 * @deprecated Используйте версию с DomainError
 */
@Deprecated(
    message = "Use doActionIfError with DomainError instead",
    replaceWith = ReplaceWith("doActionIfError { error -> onError(error.toString()) }"),
    level = DeprecationLevel.WARNING
)
fun <T> NetworkResult<T>.doActionIfErrorLegacy(onError: (String) -> Unit) {
    when (this) {
        is NetworkResult.Error -> onError(error.toString())
        is NetworkResult.Empty -> onError("Нет данных")
        else -> { /* игнорируем */ }
    }
}

/**
 * Выполнение действия ТОЛЬКО при загрузке
 * Остальные состояния игнорируются
 *
 * @param onLoading - действие при загрузке
 */
fun <T> NetworkResult<T>.doActionIfLoading(onLoading: () -> Unit) {
    when (this) {
        is NetworkResult.Loading -> onLoading()
        else -> { /* игнорируем */ }
    }
}

/**
 * Трансформирует данные внутри NetworkResult (например, маппинг DTO -> Domain)
 * Сохраняет состояния Loading и Error без изменений
 *
 * @param transform - функция трансформации данных
 * @return Flow с трансформированными данными
 */
inline fun <T, R> Flow<NetworkResult<T>>.mapNetworkResult(
    crossinline transform: (T) -> R
): Flow<NetworkResult<R>> = map { result ->
    when (result) {
        is NetworkResult.Success -> NetworkResult.Success(transform(result.data))
        is NetworkResult.Error -> NetworkResult.Error(result.error, result.exception)
        is NetworkResult.Loading -> NetworkResult.Loading
        is NetworkResult.Empty -> NetworkResult.Empty
    }
}

/**
 * Создает цепочку последовательных операций
 * После успешного выполнения первой операции запускает вторую на основе результата
 *
 * Пример использования:
 * ```
 * getUserProfile()
 *     .andThen { user ->
 *         saveUserLocally(user)
 *     }
 * ```
 *
 * @param nextAction - следующая операция, которая использует результат предыдущей
 * @return Flow с результатом второй операции
 */
@OptIn(ExperimentalCoroutinesApi::class)
inline fun <T, R> Flow<NetworkResult<T>>.andThen(
    crossinline nextAction: suspend (T) -> Flow<NetworkResult<R>>
): Flow<NetworkResult<R>> = flatMapLatest { result ->
    when (result) {
        is NetworkResult.Loading -> flowOf(NetworkResult.Loading)
        is NetworkResult.Error -> flowOf(NetworkResult.Error(result.error, result.exception))
        is NetworkResult.Empty -> flowOf(NetworkResult.Empty)
        is NetworkResult.Success -> nextAction(result.data)
    }
}