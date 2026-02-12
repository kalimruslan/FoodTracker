package com.ruslan.foodtracker.data.remote.api

import com.ruslan.foodtracker.domain.model.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response

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
                emit(NetworkResult.Error(
                    message = "Пустой ответ от сервера",
                    code = response.code()
                ))
            }
        }
        else -> {
            // Обработка ошибки
            val errorMessage = response.errorBody()?.string()
                ?: response.message()
                ?: "Неизвестная ошибка"

            emit(NetworkResult.Error(
                message = errorMessage,
                code = response.code()
            ))
        }
    }
}.catch { exception ->
    // Обработка исключений (сеть недоступна, timeout и т.д.)
    emit(NetworkResult.Error(
        message = exception.message ?: "Ошибка соединения",
        exception = exception
    ))
}.flowOn(Dispatchers.IO) // Выполняем на IO потоке