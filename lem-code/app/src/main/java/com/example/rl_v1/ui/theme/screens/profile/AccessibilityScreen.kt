package com.example.rl_v1.ui.theme.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rl_v1.ui.theme.components.ClickablePrimaryTextBox
import com.example.rl_v1.ui.theme.components.ScreenHeader
import com.example.rl_v1.ui.theme.components.ToggleableRow
import com.example.rl_v1.ui.theme.theme.Forest
import com.example.rl_v1.ui.theme.theme.GreyGreen
import com.example.rl_v1.ui.theme.theme.Rlv1Theme

/**
 * Accessibility and notification settings, opened from "Make it yours" on the Profile screen.
 *
 * Frontend only: the toggle is kept in local state and reported through [onNotificationsChange].
 *
 * @param notificationsEnabled the saved setting, used as the starting value.
 * @param onNotificationsChange called with the new value each time the user flips the switch.
 * @param onBack leave the screen (back arrow).
 */
@Composable
fun AccessibilityScreen(
    notificationsEnabled: Boolean = true,
    onNotificationsChange: (Boolean) -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var notificationsOn by rememberSaveable(notificationsEnabled) { mutableStateOf(notificationsEnabled) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Forest
                )
            }
            Spacer(Modifier.width(4.dp))
            ScreenHeader("Accessibility")
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Preview changes before saving.",
            style = MaterialTheme.typography.bodyMedium,
            color = GreyGreen,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(Modifier.height(24.dp))

        ToggleableRow(
            title = "Notifications",
            subtitle = "Get updates about your reservations and lotteries",
            checked = notificationsOn,
            onCheckedChange = { checked ->
                notificationsOn = checked
                // TODO: persist the setting (DataStore/Firestore) and, on Android 13+,
                //  request the POST_NOTIFICATIONS permission when turning it on.
                onNotificationsChange(checked)
            }
        )
        Spacer(Modifier.height(8.dp))
        ClickablePrimaryTextBox(
            value = "On",
            label = "Colour Vision Support",
            onClick = { /*TODO*/ },
            modifier = Modifier.fillMaxSize(),
        )
        Spacer(Modifier.height(8.dp))
        ClickablePrimaryTextBox(
            value = "Standard",
            label = "Text Size",
            onClick = { /*TODO*/ },
            modifier = Modifier.fillMaxSize(),
        )
        Spacer(Modifier.height(8.dp))
        ClickablePrimaryTextBox(
            value = "Standard",
            label = "Buttons and Controls",
            onClick = { /*TODO*/ },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "Notifications on")
@Composable
private fun AccessibilityScreenPreview() {
    Rlv1Theme(dynamicColor = false) {
        AccessibilityScreen(notificationsEnabled = true)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "Notifications off")
@Composable
private fun AccessibilityScreenOffPreview() {
    Rlv1Theme(dynamicColor = false) {
        AccessibilityScreen(notificationsEnabled = false)
    }
}
