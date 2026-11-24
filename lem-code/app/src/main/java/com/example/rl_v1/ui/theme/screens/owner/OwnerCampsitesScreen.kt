package com.example.rl_v1.ui.theme.screens.owner

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import com.example.rl_v1.ui.theme.components.SmallButtonPrimary
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rl_v1.model.Reservation
import com.example.rl_v1.ui.theme.components.CampsiteCard
import com.example.rl_v1.ui.theme.components.Header1
import com.example.rl_v1.ui.theme.components.LargeButtonPrimary
import com.example.rl_v1.ui.theme.components.PrimaryTextBox
import com.example.rl_v1.ui.theme.components.ScreenHeader
import com.example.rl_v1.ui.theme.viewmodel.CampsiteViewModel
import java.time.LocalDate
import java.util.Locale

/**
 * Owner overview (Fig. 13): the signed-in user's campsites with their booked
 * dates, and the active reservations made on them. Tapping a campsite opens its edit screen.
 */
@Composable
fun OwnerCampsitesScreen(
    vm: CampsiteViewModel,
    onBack: () -> Unit,
    onPublish: () -> Unit,
    onEdit: (campsiteId: String) -> Unit
) {
    BackHandler(onBack = onBack)

    val campsites by vm.ownerCampsites.collectAsStateWithLifecycle()
    val reservations by vm.ownerReservations.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize().background(Color.White).padding(top = 16.dp)
    ) {
        item { ScreenHeader("Manage Campsites") }

        message?.let { text -> item { Text(text) } }

        item { LargeButtonPrimary("Publish a campsite", onClick = onPublish) }

        item { Header1("My campsites") }

        if (campsites.isEmpty()) {
            item { Text("You haven't published any campsites yet.") }
        }

        items(campsites, key = { it.id }) { campsite ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                CampsiteCard(
                    campsite,
                    Modifier.fillMaxWidth(),
                    onClick = { onEdit(campsite.id) }
                )
                PrimaryTextBox(
                    value = bookedDatesFor(campsite.id, reservations),
                    label = "BOOKED DATES"
                )
            }
        }

        item { Header1("Active reservations") }

        val active = activeReservations(reservations, LocalDate.now().toString())

        if (active.isEmpty()) {
            item { Text("No active reservations.") }
        }

        items(active, key = { it.id }) { reservation ->
            val campsiteName = campsites.firstOrNull { it.id == reservation.campsiteId }?.name
                ?: "Campsite"
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                PrimaryTextBox(
                    value = "${reservation.checkInDate} to ${reservation.checkOutDate} • " +
                            "${reservation.numberOfGuests} guests • " +
                            "$" + String.format(Locale.US, "%.2f", reservation.totalPrice),
                    label = campsiteName.uppercase()
                )
                SmallButtonPrimary("Reject", onClick = { vm.reject(reservation) })
            }
        }
    }
}

/**
 * Reservations that are not rejected and whose stay hasn't ended yet, earliest first.
 * [today] is a date like "2027-05-07". A stay ends on its check-out day.
 */
fun activeReservations(reservations: List<Reservation>, today: String): List<Reservation> {
    val active = mutableListOf<Reservation>()
    for (reservation in reservations) {
        // Dates look like "2027-05-07", so comparing the text also compares the dates.
        if (reservation.status != "rejected" && reservation.checkOutDate > today) {
            active.add(reservation)
        }
    }
    return active.sortedBy { it.checkInDate }
}

/**
 * The booked dates for one campsite, one stay per line, earliest first.
 * Rejected reservations are skipped because their nights are free again.
 */
fun bookedDatesFor(campsiteId: String, reservations: List<Reservation>): String {
    val booked = mutableListOf<Reservation>()
    for (reservation in reservations) {
        if (reservation.campsiteId == campsiteId && reservation.status != "rejected") {
            booked.add(reservation)
        }
    }

    if (booked.isEmpty()) {
        return "No booked dates yet."
    }

    // Dates look like "2027-05-07", so sorting the text also sorts by date.
    val sorted = booked.sortedBy { it.checkInDate }

    var text = ""
    for (reservation in sorted) {
        if (text != "") {
            text += "\n"
        }
        text += "${reservation.checkInDate} to ${reservation.checkOutDate}"
    }
    return text
}
