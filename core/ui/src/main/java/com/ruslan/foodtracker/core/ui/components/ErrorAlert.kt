package com.ruslan.foodtracker.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ruslan.foodtracker.core.ui.theme.FoodTrackerTheme

/**
 * AlertDialog для отображения ошибок
 *
 * @param message текст сообщения об ошибке
 * @param onDismiss действие при закрытии диалога
 * @param title заголовок диалога (по умолчанию "Ошибка")
 * @param onRetry действие при нажатии кнопки "Повторить" (опционально)
 * @param retryText текст кнопки повтора (по умолчанию "Повторить")
 * @param dismissText текст кнопки закрытия (по умолчанию "Закрыть")
 * @param modifier модификатор
 */
@Composable
fun ErrorAlert(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Ошибка",
    onRetry: (() -> Unit)? = null,
    retryText: String = "Повторить",
    dismissText: String = "Закрыть"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            if (onRetry != null) {
                TextButton(onClick = {
                    onDismiss()
                    onRetry()
                }) {
                    Text(retryText)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
        modifier = modifier
    )
}

// ========== Preview ==========

@Preview(name = "ErrorAlert - With Retry", showBackground = true)
@Composable
private fun ErrorAlertWithRetryPreview() {
    FoodTrackerTheme {
        ErrorAlert(
            message = "Не удалось загрузить данные. Проверьте подключение к интернету.",
            title = "Ошибка загрузки",
            onRetry = {},
            onDismiss = {}
        )
    }
}

@Preview(name = "ErrorAlert - Without Retry", showBackground = true)
@Composable
private fun ErrorAlertWithoutRetryPreview() {
    FoodTrackerTheme {
        ErrorAlert(
            message = "Продукт не найден в базе данных.",
            title = "Не найдено",
            onRetry = null,
            onDismiss = {}
        )
    }
}

@Preview(name = "ErrorAlert - Dark", showBackground = true)
@Composable
private fun ErrorAlertDarkPreview() {
    FoodTrackerTheme(darkTheme = true) {
        ErrorAlert(
            message = "Превышено время ожидания ответа от сервера.",
            title = "Ошибка сети",
            onRetry = {},
            onDismiss = {}
        )
    }
}

@Preview(name = "ErrorAlert - Long Message", showBackground = true)
@Composable
private fun ErrorAlertLongMessagePreview() {
    FoodTrackerTheme {
        ErrorAlert(
            message = "Произошла неизвестная ошибка при попытке загрузить данные с сервера. " +
                "Пожалуйста, попробуйте позже или обратитесь в службу поддержки.",
            title = "Критическая ошибка",
            onRetry = {},
            onDismiss = {}
        )
    }
}
