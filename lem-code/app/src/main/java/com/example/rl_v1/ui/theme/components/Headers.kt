package com.example.rl_v1.ui.theme.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.rl_v1.ui.theme.theme.Forest

// For header text on each screen
@Composable
        /** Main Screen Header */
fun ScreenHeader(text: String){
    Text(text, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold, color = Color.Black)
}

/** Section heading inside a screen, in forest green. */
@Composable
fun Header1(text: String){
    Text(text, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = Forest)
}

