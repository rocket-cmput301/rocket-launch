package com.example.rl_v1.ui.theme.comp

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rl_v1.ui.theme.DarkGreen
import com.example.rl_v1.ui.theme.LightGreen

@Composable //Button that spans the entire width of the screen or parent container
fun LargeButtonPrimary(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DarkGreen,
            contentColor = Color.White,
            disabledContainerColor = Color.Gray,
            disabledContentColor = Color.White
        ),

    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable // Secondary Button that spans the entire width of the screen or parent container with a lighter background color
fun LargeButtonSecondary(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = LightGreen,
            contentColor = DarkGreen,
            disabledContainerColor = Color.Gray,
            disabledContentColor = Color.White
        ),

        ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SmallButtonPrimary(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = true,
    enabled: Boolean = true
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) DarkGreen else LightGreen,
        label = "containerColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) Color.White else DarkGreen,
        label = "contentColor"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(28.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = Color.Gray,
            disabledContentColor = Color.White
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

