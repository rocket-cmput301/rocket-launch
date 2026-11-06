package com.example.rl_v1.ui.theme.screens.owner

import androidx.activity.compose.BackHandler
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
import com.example.rl_v1.ui.theme.components.CampsiteCard
import com.example.rl_v1.ui.theme.components.Header1
import com.example.rl_v1.ui.theme.components.LargeButtonPrimary
import com.example.rl_v1.ui.theme.components.PrimaryTextBox
import com.example.rl_v1.ui.theme.components.ScreenHeader
import com.example.rl_v1.ui.theme.viewmodel.CampsiteViewModel
import java.util.Locale

/**
 * Owner overview (Fig. 13): the signed-in user's campsites and the
 * reservations made on them. Tapping a campsite opens its edit screen.
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
            CampsiteCard(
                campsite,
                Modifier.fillMaxWidth(),
                onClick = { onEdit(campsite.id) }
            )
        }

        item { Header1("Reservations on my campsites") }

        if (reservations.isEmpty()) {
            item { Text("No reservations yet.") }
        }

        items(reservations, key = { it.id }) { reservation ->
            val campsiteName = campsites.firstOrNull { it.id == reservation.campsiteId }?.name
                ?: "Campsite"
            PrimaryTextBox(
                value = "${reservation.checkInDate} to ${reservation.checkOutDate} • " +
                        "${reservation.numberOfGuests} guests • " +
                        "$" + String.format(Locale.US, "%.2f", reservation.totalPrice),
                label = campsiteName.uppercase()
            )
        }
    }
}
