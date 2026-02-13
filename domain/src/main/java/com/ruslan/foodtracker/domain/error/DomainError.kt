package com.ruslan.foodtracker.domain.error

/**
 * Базовый sealed class для всех типов ошибок в приложении
 *
 * Используется вместо хардкоженных строк для:
 * 1. Типизации ошибок
 * 2. Централизованного управления сообщениями
 * 3. Локализации через строковые ресурсы
 * 4. Упрощения тестирования
 *
 * Архитектура:
 * - Domain слой содержит типы ошибок (этот файл)
 * - Data слой возвращает типизированные ошибки в NetworkResult
 * - Presentation слой маппит ошибки в строковые ресурсы через ErrorMapper
 */
sealed class DomainError {

    // ========== DATABASE ERRORS ==========

    /**
     * Ошибки работы с локальной базой данных (Room)
     */
    sealed class Database : DomainError() {
        /**
         * Ошибка при загрузке данных из БД
         * Используется в: getAllFoods(), getFoodById(), searchFoods()
         */
        data object FetchFailed : Database()

        /**
         * Ошибка при вставке записи в БД
         * Используется в: insertFood(), insertFoods()
         */
        data object InsertFailed : Database()

        /**
         * Ошибка при обновлении записи в БД
         * Используется в: updateFood()
         */
        data object UpdateFailed : Database()

        /**
         * Ошибка при удалении записи из БД
         * Используется в: deleteFood()
         */
        data object DeleteFailed : Database()

        /**
         * Запись не найдена в БД по ID
         * Используется в: getFoodById()
         */
        data object NotFound : Database()

        /**
         * Продукт с указанным штрих-кодом не найден в локальной БД
         * Используется в: getFoodByBarcodeLocal()
         */
        data object BarcodeNotFound : Database()

        /**
         * Ошибка при выполнении поиска в БД
         * Используется в: searchFoods()
         */
        data object SearchFailed : Database()
    }

    // ========== NETWORK ERRORS ==========

    /**
     * Ошибки сетевых запросов (Open Food Facts API)
     */
    sealed class Network : DomainError() {
        /**
         * Нет подключения к интернету
         * Используется в: handleApi() при IOException
         */
        data object NoConnection : Network()

        /**
         * Превышено время ожидания ответа от сервера
         * Используется в: handleApi() при SocketTimeoutException
         */
        data object Timeout : Network()

        /**
         * Ошибка сервера (HTTP 5xx)
         * Используется в: handleApi() при code >= 500
         */
        data object ServerError : Network()

        /**
         * HTTP ошибка (4xx)
         * @param code - HTTP код ошибки (например, 404, 401)
         * Используется в: handleApi() при code 4xx
         */
        data class HttpError(val code: Int) : Network()

        /**
         * Сервер вернул пустой ответ (body == null)
         * Используется в: handleApi() при response.body() == null
         */
        data object EmptyResponse : Network()

        /**
         * Неизвестная сетевая ошибка
         * Используется в: handleApi() при необработанных исключениях
         */
        data object Unknown : Network()
    }

    // ========== VALIDATION ERRORS ==========

    /**
     * Ошибки валидации пользовательского ввода
     */
    sealed class Validation : DomainError() {
        /**
         * Пустой поисковый запрос
         * Используется в: SearchFoodsByNameUseCase при query.isBlank()
         */
        data object EmptyQuery : Validation()

        /**
         * Поисковый запрос слишком короткий (< 2 символов)
         * Используется в: SearchFoodsByNameUseCase при query.length < 2
         */
        data object QueryTooShort : Validation()

        /**
         * Невалидный формат штрих-кода
         * Используется в: SearchFoodByBarcodeUseCase при невалидном barcode
         */
        data object InvalidBarcode : Validation()

        /**
         * Обязательное поле не заполнено
         * Используется в: формах создания/редактирования продуктов
         */
        data object EmptyField : Validation()
    }

    // ========== DATA ERRORS ==========

    /**
     * Ошибки обработки и парсинга данных
     */
    sealed class Data : DomainError() {
        /**
         * Продукт не найден в Open Food Facts API
         * Используется в: getFoodByBarcode() при status != 1 или product == null
         */
        data object ProductNotFound : Data()

        /**
         * Ошибка парсинга/обработки данных от сервера
         * Используется в: маппинг DTO -> Domain при невалидных данных
         */
        data object ParseError : Data()

        /**
         * Нет кэшированных данных для fallback
         * Используется в: UseCase при fallback на локальный кэш
         */
        data object NoCache : Data()

        /**
         * Пустой результат (нет данных)
         * Используется в: UseCase при успешном запросе, но пустом результате
         */
        data object Empty : Data()
    }

    // ========== HELPER METHODS ==========

    /**
     * Получить краткое описание типа ошибки (для логирования)
     */
    fun getType(): String = when (this) {
        is Database -> "Database"
        is Network -> "Network"
        is Validation -> "Validation"
        is Data -> "Data"
    }

    /**
     * Проверка, является ли ошибка критической (требует показа пользователю)
     */
    fun isCritical(): Boolean = when (this) {
        is Database.FetchFailed,
        is Database.NotFound,
        is Network.NoConnection,
        is Network.Timeout,
        is Network.ServerError,
        is Data.ProductNotFound -> true
        else -> false
    }
}