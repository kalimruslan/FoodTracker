package com.ruslan.foodtracker.core.ui.error

import android.content.Context
import com.ruslan.foodtracker.domain.error.DomainError

/**
 * Provider для получения локализованных сообщений об ошибках
 *
 * Использование в ViewModel/Composable:
 * ```kotlin
 * val errorMessageProvider = ErrorMessageProvider(context)
 * val message = errorMessageProvider.getMessage(domainError)
 * ```
 *
 * Или через extension функцию (предпочтительно):
 * ```kotlin
 * val context = LocalContext.current
 * val message = domainError.toMessage(context)
 * ```
 */
class ErrorMessageProvider(
    private val context: Context
) {
    /**
     * Получить локализованное сообщение об ошибке
     *
     * @param error - типизированная ошибка из domain слоя
     * @return Локализованное сообщение из строковых ресурсов
     */
    fun getMessage(error: DomainError): String {
        val stringResId = error.toStringRes()

        return if (error.requiresFormatting()) {
            // Форматируем строку с аргументами (например, HTTP код)
            val formatArgs = error.getFormatArgs()
            if (formatArgs != null) {
                context.getString(stringResId, *formatArgs)
            } else {
                context.getString(stringResId)
            }
        } else {
            // Просто получаем строку из ресурсов
            context.getString(stringResId)
        }
    }
}

/**
 * Extension функция для удобного получения сообщения из Context
 *
 * Использование в Composable:
 * ```kotlin
 * val context = LocalContext.current
 * Text(text = domainError.toMessage(context))
 * ```
 */
fun DomainError.toMessage(context: Context): String {
    val stringResId = this.toStringRes()

    return if (this.requiresFormatting()) {
        val formatArgs = this.getFormatArgs()
        if (formatArgs != null) {
            context.getString(stringResId, *formatArgs)
        } else {
            context.getString(stringResId)
        }
    } else {
        context.getString(stringResId)
    }
}
