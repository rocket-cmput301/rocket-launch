package com.example.rl_v1.ui.theme.screens.reservation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.rl_v1.model.Reservation
import com.example.rl_v1.ui.theme.components.LargeButtonPrimary
import com.example.rl_v1.ui.theme.components.PrimaryClickableText
import com.example.rl_v1.ui.theme.components.PrimaryTextBox
import com.example.rl_v1.ui.theme.components.ScreenHeader
import java.util.Locale

/**
 * Reservation confirmation (Fig. 07), shown after booking and when opened from Activity.
 * [reservation] is null until it has loaded. The cancel link shows only when [canCancel].
 */
@Composable
fun ReservationConfirmedScreen(
    reservation: Reservation?,
    campsiteName: String,
    onDone: () -> Unit,
    canCancel: Boolean = false,
    onCancel: () -> Unit = {},
    message: String? = null
) {
    BackHandler(onBack = onDone)
    var askCancel by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (reservation == null || reservation.status == "confirmed") {
            Text("YOU'RE GOING CAMPING")
            ScreenHeader("Reservation Confirmed")
        } else {
            ScreenHeader("Reservation ${reservation.status.replaceFirstChar { it.uppercase() }}")
        }

        if (reservation == null) {
            Text("Loading your reservation...")
        } else {
            PrimaryTextBox(
                value = "${reservation.checkInDate} to ${reservation.checkOutDate} • " +
                        "${reservation.numberOfGuests} guests",
                label = "${reservation.status.uppercase()} • ${campsiteName.uppercase()}"
            )
            PrimaryTextBox(
                value = "$" + String.format(Locale.US, "%.2f", reservation.totalPrice),
                label = "TOTAL"
            )
            PrimaryTextBox(value = reservation.id, label = "CONFIRMATION NUMBER")
        }

        if (message != null) {
            Text(message)
        }
        if (canCancel) {
            PrimaryClickableText(
                text = "Cancel reservation",
                textColor = MaterialTheme.colorScheme.error,
                onClick = { askCancel = true }
            )
        }

        LargeButtonPrimary("Done", onClick = onDone)
    }

    if (askCancel && reservation != null) {
        AlertDialog(
            onDismissRequest = { askCancel = false },
            title = { Text("Cancel Reservation") },
            text = {
                Text(
                    "Cancel your ${reservation.checkInDate} to ${reservation.checkOutDate} " +
                    "reservation at $campsiteName and release these dates?"
                )
            },
            confirmButton = {
                TextButton(onClick = { askCancel = false; onCancel() }) { Text("Confirm cancellation") }
            },
            dismissButton = {
                TextButton(onClick = { askCancel = false }) { Text("Keep reservation") }
            }
        )
    }
}
