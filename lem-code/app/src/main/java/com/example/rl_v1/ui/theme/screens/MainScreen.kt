package com.example.rl_v1.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rl_v1.ui.theme.viewmodel.CampsiteViewModel

/** Simplest possible screen: a role toggle, buttons, and lists. */
@Composable
fun MainScreen(vm: CampsiteViewModel = viewModel()) {
    var isOwner by remember { mutableStateOf(true) }
    val message by vm.message.collectAsStateWithLifecycle()

    Column(Modifier
        .fillMaxSize()
        .padding(16.dp)) {
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
    val allCampsites by vm.allCampsites.collectAsStateWithLifecycle()
    val query by vm.searchQuery.collectAsStateWithLifecycle()
    val reservations by vm.entrantReservations.collectAsStateWithLifecycle()
    var viewFilterScreen by remember { mutableStateOf(false) }

    if (viewFilterScreen) {     // screen for an entrant's choice of filters
        MakeFilterScreen(confirmFilters = { distanceFilter, dates, numGuests, preferences, turnOffFilterScreen ->
            vm.setFilters(distanceFilter, dates, numGuests, preferences)
            viewFilterScreen = turnOffFilterScreen  //last step: get rid of filter screen to refresh search
        })

    }
    else {                       // screen for an entrant's searched + filtered (if used) campsites
        Button(onClick = {       //access Filter Campsites screen via toggle
            viewFilterScreen = !viewFilterScreen
        }) { Text("Select Filters") }
        var selectedId by rememberSaveable { mutableStateOf<String?>(null) }

        // Looked up in the full list, so typing in search can't close the details
        // screen, and a deleted campsite simply returns you to the list.
        val selected = allCampsites.firstOrNull { it.id == selectedId }
        if (selected != null) {
            CampsiteDetailsScreen(selected, onBack = { selectedId = null })
            return
        }

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
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    onClick = { selectedId = campsite.id },
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
}