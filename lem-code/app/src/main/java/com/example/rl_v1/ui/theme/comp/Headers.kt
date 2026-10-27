package com.example.rl_v1.ui.theme.comp

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.example.rl_v1.ui.theme.Forest

// For header text on each screen
@Composable
fun ScreenHeader(text: String){
    Text(text, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Forest)
}

@Composable
fun Header1(text: String){
    Text(text, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = Forest)
}

