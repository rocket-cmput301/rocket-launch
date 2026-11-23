package com.example.rl_v1.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rl_v1.ui.theme.theme.DarkGreen
import com.example.rl_v1.ui.theme.theme.Forest
import com.example.rl_v1.ui.theme.theme.GreyGreen
import com.example.rl_v1.ui.theme.theme.LightGreen
import com.example.rl_v1.ui.theme.theme.Rlv1Theme

/**
 * Light green box with a bold [title], an optional grey [subtitle] and a switch on the right.
 *
 * Tapping anywhere on the row flips the switch, and TalkBack reads the row as a single switch.
 * Stateless: the caller keeps [checked] and updates it in [onCheckedChange].
 */
@Composable
fun ToggleableRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange
            )
            .background(LightGreen)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Forest
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GreyGreen
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = null, // handled by the row's toggleable
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = DarkGreen,
                uncheckedThumbColor = GreyGreen,
                uncheckedTrackColor = Color.White,
                uncheckedBorderColor = GreyGreen
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "ToggleableRow")
@Composable
private fun ToggleableRowPreview() {
    Rlv1Theme(dynamicColor = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ToggleableRow(
                title = "Notifications",
                subtitle = "Get updates about your reservations and lotteries",
                checked = true,
                onCheckedChange = {}
            )
            ToggleableRow(
                title = "Notifications",
                subtitle = "Get updates about your reservations and lotteries",
                checked = false,
                onCheckedChange = {}
            )
            ToggleableRow(
                title = "No subtitle",
                checked = true,
                onCheckedChange = {}
            )
        }
    }
}
