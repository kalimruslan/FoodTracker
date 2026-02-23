package com.ruslan.foodtracker.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ruslan.foodtracker.core.ui.theme.FoodTrackerTheme

/**
 * Индикатор загрузки с опциональным текстом
 *
 * @param modifier модификатор
 * @param text текст под индикатором (опционально)
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    text: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary
        )

        if (text != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ========== Preview ==========

@Preview(name = "LoadingIndicator - With Text", showBackground = true)
@Composable
private fun LoadingIndicatorWithTextPreview() {
    FoodTrackerTheme {
        LoadingIndicator(text = "Загрузка продуктов...")
    }
}

@Preview(name = "LoadingIndicator - Without Text", showBackground = true)
@Composable
private fun LoadingIndicatorWithoutTextPreview() {
    FoodTrackerTheme {
        LoadingIndicator()
    }
}

@Preview(name = "LoadingIndicator - Dark", showBackground = true)
@Composable
private fun LoadingIndicatorDarkPreview() {
    FoodTrackerTheme(darkTheme = true) {
        LoadingIndicator(text = "Загрузка...")
    }
}
