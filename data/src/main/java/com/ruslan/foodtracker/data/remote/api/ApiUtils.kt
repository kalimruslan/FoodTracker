package com.ruslan.foodtracker.data.remote.api

import com.ruslan.foodtracker.domain.error.DomainError
import com.ruslan.foodtracker.domain.model.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Утилиты для работы с API
 * Паттерн взят из проекта ДОСААФ
 */

/**
 * Универсальный обработчик для Retrofit запросов
 * Превращает Response<T> в Flow<NetworkResult<T>> с автоматической обработкой состояний
 *
 * Пример использования:
 * ```
 * fun getProduct(id: String): Flow<NetworkResult<ProductDto>> = handleApi {
 *     api.getProduct(id)
 * }
 * ```
 *
 * @param execute - suspend функция для выполнения Retrofit запроса
 * @return Flow с результатом в виде NetworkResult (Loading -> Success/Error)
 */
internal inline fun <T> handleApi(
    crossinline execute: suspend () -> Response<T>
): Flow<NetworkResult<T>> = flow {
    // Испускаем Loading состояние
    emit(NetworkResult.Loading)

    // Выполняем запрос
    val response = execute()

    // Обрабатываем ответ
    when {
        response.isSuccessful -> {
            val body = response.body()
            if (body != null) {
                emit(NetworkResult.Success(body))
            } else {
                // Пустой ответ от сервера
                emit(NetworkResult.Error(
                    error = DomainError.Network.EmptyResponse,
                    exception = null
                ))
            }
        }
        else -> {
            // Обработка HTTP ошибок
            val httpCode = response.code()
            val error = when {
                httpCode >= 500 -> DomainError.Network.ServerError // 5xx - ошибка сервера
                httpCode >= 400 -> DomainError.Network.HttpError(httpCode) // 4xx - клиентская ошибка
                else -> DomainError.Network.Unknown
            }

            emit(NetworkResult.Error(
                error = error,
                exception = null
            ))
        }
    }
}.catch { exception ->
    // Обработка исключений (сеть недоступна, timeout и т.д.)
    val error = when (exception) {
        is SocketTimeoutException -> DomainError.Network.Timeout
        is UnknownHostException, is IOException -> DomainError.Network.NoConnection
        else -> DomainError.Network.Unknown
    }

    emit(NetworkResult.Error(
        error = error,
        exception = exception
    ))
}.flowOn(Dispatchers.IO) // Выполняем на IO потоке