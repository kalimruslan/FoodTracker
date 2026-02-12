package com.ruslan.foodtracker.domain.util

import com.ruslan.foodtracker.domain.model.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * Обработчик результатов для работы с Flow и NetworkResult
 */

/**
 * Оборачивает результат в NetworkResult с автоматической обработкой ошибок
 */
inline fun <T> safeApiCall(apiCall: () -> T): NetworkResult<T> {
    return try {
        NetworkResult.Success(apiCall())
    } catch (e: Exception) {
        NetworkResult.Error(
            message = e.message ?: "Неизвестная ошибка",
            exception = e
        )
    }
}

/**
 * Suspend версия safeApiCall
 */
suspend inline fun <T> safeSuspendApiCall(
    crossinline apiCall: suspend () -> T
): NetworkResult<T> {
    return try {
        NetworkResult.Success(apiCall())
    } catch (e: Exception) {
        NetworkResult.Error(
            message = e.message ?: "Неизвестная ошибка",
            exception = e
        )
    }
}

/**
 * Оборачивает Flow в NetworkResult с состояниями Loading/Success/Error
 */
fun <T> Flow<T>.asNetworkResult(): Flow<NetworkResult<T>> {
    return this
        .map<T, NetworkResult<T>> {
            NetworkResult.Success(it)
        }
        .onStart {
            emit(NetworkResult.Loading)
        }
        .catch { e ->
            emit(
                NetworkResult.Error(
                    message = e.message ?: "Неизвестная ошибка",
                    exception = e
                )
            )
        }
}
