package com.example.rl_v1.ui.theme.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Small red message shown under a field when its input is not valid. */
@Composable
fun FieldError(text: String?) {
    if (text != null) {
        Text(
            text = text,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}
