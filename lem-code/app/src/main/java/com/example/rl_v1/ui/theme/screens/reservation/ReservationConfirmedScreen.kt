package com.example.rl_v1.ui.theme.screens.reservation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.rl_v1.model.Reservation
import com.example.rl_v1.ui.theme.components.LargeButtonPrimary
import com.example.rl_v1.ui.theme.components.PrimaryTextBox
import com.example.rl_v1.ui.theme.components.ScreenHeader
import java.util.Locale

/** Reservation confirmed (Fig. 07). [reservation] is null until it has loaded. */
@Composable
fun ReservationConfirmedScreen(
    reservation: Reservation?,
    campsiteName: String,
    onDone: () -> Unit
) {
    BackHandler(onBack = onDone)

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("YOU'RE GOING CAMPING")
        ScreenHeader("Reservation Confirmed")

        if (reservation == null) {
            Text("Loading your reservation...")
        } else {
            PrimaryTextBox(
                value = "${reservation.checkInDate} to ${reservation.checkOutDate} • " +
                        "${reservation.numberOfGuests} guests",
                label = "CONFIRMED • ${campsiteName.uppercase()}"
            )
            PrimaryTextBox(
                value = "$" + String.format(Locale.US, "%.2f", reservation.totalPrice),
                label = "TOTAL"
            )
            PrimaryTextBox(value = reservation.id, label = "CONFIRMATION NUMBER")
        }

        LargeButtonPrimary("Back to Explore", onClick = onDone)
    }
}
