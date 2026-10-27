package com.example.rl_v1.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow

/** Simplest possible screen: a role toggle, buttons, and lists. */
@Composable
fun MainScreen(vm: CampsiteViewModel = viewModel()) {
    var isOwner by remember { mutableStateOf(true) }
    val message by vm.message.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { isOwner = true }) { Text("Owner") }
            Button(onClick = { isOwner = false }) { Text("Entrant") }
        }

        message?.let { Text(it) }

        if (isOwner) OwnerScreen(vm) else EntrantScreen(vm)
    }
}

@Composable
private fun OwnerScreen(vm: CampsiteViewModel) {
    val campsites by vm.ownerCampsites.collectAsStateWithLifecycle()
    val reservations by vm.ownerReservations.collectAsStateWithLifecycle()

    LazyColumn {
        item {
            Button(onClick = {
                vm.publish("Campsite ${campsites.size + 1}", "Banff, AB", 6, 80.0)
            }) { Text("Publish a campsite") }
        }

        item { Text("My campsites") }
        items(campsites, key = { it.id }) {
            Text("${it.name} - ${it.location} - $${it.pricePerNight}/night")
        }

        item { Text("Bookings on my campsites") }
        items(reservations, key = { it.id }) {
            Text("${it.checkInDate} to ${it.checkOutDate} - booked by ${it.entrantId}")
        }
    }
}

@Composable
private fun EntrantScreen(vm: CampsiteViewModel) {
    val campsites by vm.searchedCampsites.collectAsStateWithLifecycle()
    val query by vm.searchQuery.collectAsStateWithLifecycle()
    val reservations by vm.entrantReservations.collectAsStateWithLifecycle()

    LazyColumn {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = vm::setSearchQuery,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by name or location") },
                singleLine = true
            )
        }

        item { Text("Campsites (Book = May 7 to 9, 2027, 2 guests)") }

        if (campsites.isEmpty() && query.isNotBlank()) {
            item { Text("No campsites match your search.") }
        }

        items(campsites, key = { it.id }) { campsite ->
            CampsiteCard(
                campsite,
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                trailing = {
                    Button(onClick = { vm.book(campsite, "2027-05-07", "2027-05-09", 2) }) {
                        Text("Book")
                    }
                }
            )
        }

        item { Text("My reservations") }
        items(reservations, key = { it.id }) {
            Text("${it.checkInDate} to ${it.checkOutDate} - ${it.numberOfGuests} guests")
        }
    }
}