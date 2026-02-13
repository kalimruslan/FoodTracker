package com.ruslan.foodtracker.core.ui.error

import androidx.annotation.StringRes
import com.ruslan.foodtracker.core.ui.R
import com.ruslan.foodtracker.domain.error.DomainError

/**
 * Маппер для преобразования типизированных ошибок (DomainError) в строковые ресурсы
 *
 * Использование:
 * ```kotlin
 * val errorResId = domainError.toStringRes()
 * val message = getString(errorResId)
 * ```
 *
 * Архитектура:
 * - Domain слой содержит типизированные ошибки (DomainError)
 * - Presentation слой маппит их в строковые ресурсы через этот маппер
 * - UI отображает локализованные сообщения из resources
 */

/**
 * Преобразует DomainError в ID строкового ресурса
 *
 * @return ID строкового ресурса из R.string.error_*
 */
@StringRes
fun DomainError.toStringRes(): Int = when (this) {
    // ========== DATABASE ERRORS ==========
    is DomainError.Database.FetchFailed -> R.string.error_database_fetch_failed
    is DomainError.Database.InsertFailed -> R.string.error_database_insert_failed
    is DomainError.Database.UpdateFailed -> R.string.error_database_update_failed
    is DomainError.Database.DeleteFailed -> R.string.error_database_delete_failed
    is DomainError.Database.NotFound -> R.string.error_database_not_found
    is DomainError.Database.BarcodeNotFound -> R.string.error_database_barcode_not_found
    is DomainError.Database.SearchFailed -> R.string.error_database_search_failed

    // ========== NETWORK ERRORS ==========
    is DomainError.Network.NoConnection -> R.string.error_network_no_connection
    is DomainError.Network.Timeout -> R.string.error_network_timeout
    is DomainError.Network.ServerError -> R.string.error_network_server_error
    is DomainError.Network.HttpError -> R.string.error_network_http_error // Форматируется с кодом
    is DomainError.Network.EmptyResponse -> R.string.error_network_empty_response
    is DomainError.Network.Unknown -> R.string.error_network_unknown

    // ========== VALIDATION ERRORS ==========
    is DomainError.Validation.EmptyQuery -> R.string.error_validation_empty_query
    is DomainError.Validation.QueryTooShort -> R.string.error_validation_query_too_short
    is DomainError.Validation.InvalidBarcode -> R.string.error_validation_invalid_barcode
    is DomainError.Validation.EmptyField -> R.string.error_validation_empty_field

    // ========== DATA ERRORS ==========
    is DomainError.Data.ProductNotFound -> R.string.error_data_product_not_found
    is DomainError.Data.ParseError -> R.string.error_data_parse_error
    is DomainError.Data.NoCache -> R.string.error_data_no_cache
    is DomainError.Data.Empty -> R.string.error_data_empty
}

/**
 * Проверка, требуется ли форматирование строки (например, для HttpError с кодом)
 *
 * @return true, если нужны дополнительные аргументы для форматирования
 */
fun DomainError.requiresFormatting(): Boolean = when (this) {
    is DomainError.Network.HttpError -> true // %1$d для кода
    else -> false
}

/**
 * Получить массив аргументов для форматирования строки
 *
 * @return Array<Any> с аргументами для String.format() или null
 */
fun DomainError.getFormatArgs(): Array<Any>? = when (this) {
    is DomainError.Network.HttpError -> arrayOf(code)
    else -> null
}