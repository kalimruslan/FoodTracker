package com.ruslan.foodtracker.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response
import ru.synergy.dosaaf.domain.NetworkResult

/**
 * Общий Обработчик запросов, который все ответы превращает во Flow
 * @param execute - функция для выпаолнения запросов
 */
internal inline fun <T> handleApi(crossinline execute: suspend () -> Response<T>): Flow<NetworkResult<T>> =
    flow<NetworkResult<T>> {
        emit(NetworkResult.Loading())
        val result = execute.invoke()
        when {
            result.isSuccessful -> emitSuccess(result)
            else -> emitErrorOrThrow(result)
        }
    }
        .catch {
            throw it
        }.flowOn(Dispatchers.IO)

private suspend fun <T> FlowCollector<NetworkResult<T>>.emitSuccess(response: Response<T>) {
    val body = response.body() ?: run {
        // TODO генерация ошибки
    }
    emit(NetworkResult.Success(body) as NetworkResult<T>)
}

private suspend fun <T> FlowCollector<NetworkResult<T>>.emitErrorOrThrow(response: Response<T>) {
    // TODO логика системы ошибок в приложении
}
