package com.example.rl_v1.ui.theme.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.rl_v1.ui.theme.theme.Forest

/** One titled group of lines shown in [PoliciesDialog]. */
data class PolicySection(val title: String, val lines: List<String>)

/** The app's policies and guidelines, as shown from the Profile screen. */
val DefaultPolicySections = listOf(
    PolicySection(
        "Respect other campers",
        listOf(
            "Keep reviews and messages respectful",
            "Do not post harassment or personal information"
        )
    ),
    PolicySection(
        "Reservations",
        listOf(
            "Joining a lottery does not reserve dates",
            "Selected entrants choose remaining dates"
        )
    ),
    PolicySection(
        "Your profile",
        listOf("Update your details or delete your profile in Profile settings")
    )
)

/**
 * Popup card listing the app's policies and guidelines, with a Close button.
 *
 * It is shown in a [Dialog], so it sits above the whole screen and the window behind it is dimmed.
 * To blur the screen behind it as well, apply `Modifier.blur` to that screen while this is shown
 * (blur only takes effect on Android 12 / API 31 and up; older devices just get the dim).
 */
@Composable
fun PoliciesDialog(
    onDismiss: () -> Unit,
    sections: List<PolicySection> = DefaultPolicySections
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        PoliciesCard(onClose = onDismiss, sections = sections)
    }
}

/** The card content of [PoliciesDialog], usable on its own (e.g. in previews). */
@Composable
fun PoliciesCard(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    sections: List<PolicySection> = DefaultPolicySections
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Policies and Guidelines",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = Forest
            )
            sections.forEach { section ->
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Forest
                )
                Text(
                    text = section.lines.joinToString("\n"),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Forest
                )
            }
            Spacer(Modifier.height(4.dp))
            LargeButtonSecondary(text = "Close", onClick = onClose)
        }
    }
}

@Preview
@Composable
fun PoliciesCardPreview() {
    PoliciesCard(onClose = {})
}
