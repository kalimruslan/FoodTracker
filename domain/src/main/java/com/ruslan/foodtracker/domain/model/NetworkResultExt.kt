package com.ruslan.foodtracker.domain.model

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
 * Обработка всех состояний результата с отдельными callback'ами
 *
 * @param onLoading - действие при загрузке
 * @param onError - действие при ошибке (получает сообщение об ошибке)
 * @param onSuccess - действие при успехе (получает данные)
 */
fun <T> NetworkResult<T>.handleResult(
    onLoading: () -> Unit,
    onError: (String) -> Unit,
    onSuccess: (data: T) -> Unit
) {
    when (this) {
        is NetworkResult.Loading -> onLoading()
        is NetworkResult.Error -> onError(message ?: "Неизвестная ошибка")
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
 * Выполнение действия ТОЛЬКО при ошибке
 * Остальные состояния игнорируются
 *
 * @param onError - действие при ошибке
 */
fun <T> NetworkResult<T>.doActionIfError(onError: (String) -> Unit) {
    when (this) {
        is NetworkResult.Error -> onError(message ?: "Неизвестная ошибка")
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
        is NetworkResult.Error -> NetworkResult.Error(result.message, result.code, result.exception)
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
        is NetworkResult.Error -> flowOf(NetworkResult.Error(result.message, result.code, result.exception))
        is NetworkResult.Empty -> flowOf(NetworkResult.Empty)
        is NetworkResult.Success -> nextAction(result.data)
    }
}