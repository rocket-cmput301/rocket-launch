package com.example.rl_v1.ui.theme.screens.owner

import android.preference.PreferenceActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import com.example.rl_v1.ui.theme.components.SmallButtonPrimary
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rl_v1.model.Reservation
import com.example.rl_v1.model.Campsite
import com.example.rl_v1.model.Lottery
import com.example.rl_v1.ui.theme.components.CampsiteCard
import com.example.rl_v1.ui.theme.components.Header1
import com.example.rl_v1.ui.theme.components.LargeButtonPrimary
import com.example.rl_v1.ui.theme.components.PrimaryClickableText
import com.example.rl_v1.ui.theme.components.PrimaryTextBox
import com.example.rl_v1.ui.theme.components.ScreenHeader
import com.example.rl_v1.ui.theme.theme.DarkGreen
import com.example.rl_v1.ui.theme.viewmodel.CampsiteViewModel
import java.time.LocalDate
import java.util.Locale

/**
 * Owner overview (Fig. 13): the signed-in user's campsites with their booked
 * dates, and the active, past and cancelled reservations made on them. Tapping a campsite opens its edit screen.
 */
@Composable
fun OwnerCampsitesScreen(
    vm: CampsiteViewModel,
    onBack: () -> Unit,
    onPublish: () -> Unit,                      //publish campsite
    onEdit: (campsiteId: String) -> Unit,       //edit campsite
    onCreateLottery: () -> Unit,                //create lottery
    onViewWaitlist: (lotteryId: String) -> Unit,//view top lottery waitlist;
    onUpdate: (lotteryId: String) -> Unit,      //update top lottery;
    onViewDetails: (lotteryId: String) -> Unit  //view selected lottery details (not necessarily top lottery)
) {
    BackHandler(onBack = onBack)

    //val campsites by vm.ownerCampsites.collectAsStateWithLifecycle()
    val campsites by vm.specificOwnerSite(ownerID = "8bkEYENWDCWqz4mRcdTnWGeLmbv2").collectAsStateWithLifecycle()   //temporary just to see the 'manage campsites' view when campsites are actually implemented
    val reservations by vm.ownerReservations.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()
    val lotteries by vm.ownerOwnedLotteries.collectAsStateWithLifecycle() //note: as a test is using a hard coded ID

    var showLotteries by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 16.dp)
    ) {
        item { ScreenHeader("Manage Campsites") }

        message?.let { text -> item { Text(text) } }

        item { Row(modifier = Modifier.padding(horizontal = 4.dp)) {
            PrimaryClickableText(text = "Add campsite", textColor = DarkGreen, onClick = onPublish)
            PrimaryClickableText(text = "Create lottery", textColor = DarkGreen, onClick = onCreateLottery)
            //PrimaryClickableText(text = "View QR",)
        }}

        item{
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallButtonPrimary(text = "Campsites", onClick = {showLotteries = false}, dark = !showLotteries)
                SmallButtonPrimary(text = "Lotteries", onClick = {showLotteries = true}, dark = showLotteries)
            }
        }

        if (showLotteries){
            item { Header1("My Lotteries") }
            if (lotteries.isEmpty()){
                item { Text("You have no lotteries created.") }
            }
            items(lotteries, key = {it.id}){ lottery ->
                val myWaitlistEntries by remember(lottery.id) {
                    vm.callViewOwnerWaitlistEntries(lotteryID = lottery.id, ownerID = "")
                }.collectAsStateWithLifecycle(initialValue = emptyList())
                val correspondingCampsites = campsites.filter{ it.id in lottery.campsiteIds}

                PrimaryTextBox(
                    label = correspondingCampsites.joinToString { it.name },
                    value = "${myWaitlistEntries.size} Entrants • ${lottery.maxWinners} winners • Draws ${lottery.bookingPeriod.startDate}",
                    modifier = Modifier.clickable(onClick = {onViewDetails(lottery.id)})                             //add the same clickability function as RegistrationPeriod in LotteryRegisterScreen
                )
                /*
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallButtonPrimary(
                        text = "View waitlist",
                        onClick = { onViewWaitlist(lottery.id) })    //view waitlist (US 02.09.01)
                    SmallButtonPrimary(text = "Edit lottery", onClick = {
                        //TODO: update lottery screen (US 02.04.06)
                    })
                }*/
            }
        }

        else{
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

        item { Header1("Past reservations") }

        val past = pastReservations(reservations, LocalDate.now().toString())

        if (past.isEmpty()) {
            item { Text("No past reservations.") }
        }

        items(past, key = { it.id }) { reservation ->
            val campsiteName = campsites.firstOrNull { it.id == reservation.campsiteId }?.name
                ?: "Campsite"
            PrimaryTextBox(
                value = "${reservation.checkInDate} to ${reservation.checkOutDate} • " +
                        "${reservation.numberOfGuests} guests • " +
                        "$" + String.format(Locale.US, "%.2f", reservation.totalPrice),
                label = campsiteName.uppercase()
            )
        }

        item { Header1("Cancelled reservations") }

        val cancelled = cancelledReservations(reservations)

        if (cancelled.isEmpty()) {
            item { Text("No cancelled reservations.") }
        }

        items(cancelled, key = { it.id }) { reservation ->
            val campsiteName = campsites.firstOrNull { it.id == reservation.campsiteId }?.name
                ?: "Campsite"
            var label = "CANCELLED BY ENTRANT • ${campsiteName.uppercase()}"
            if (reservation.status == "rejected") {
                label = "REJECTED BY YOU • ${campsiteName.uppercase()}"
            }
            PrimaryTextBox(
                value = "${reservation.checkInDate} to ${reservation.checkOutDate} • " +
                        "${reservation.numberOfGuests} guests • " +
                        "$" + String.format(Locale.US, "%.2f", reservation.totalPrice),
                label = label
            )
        }
    }
}

/**
 * Reservations the owner rejected or the entrant cancelled, most recent check-in first.
 */
fun cancelledReservations(reservations: List<Reservation>): List<Reservation> {
    val cancelled = mutableListOf<Reservation>()
    for (reservation in reservations) {
        if (reservation.status == "rejected" || reservation.status == "cancelled") {
            cancelled.add(reservation)
        }
    }
    return cancelled.sortedByDescending { it.checkInDate }
}

/**
 * Confirmed reservations whose stay has ended, most recent first.
 * [today] is a date like "2027-05-07". A stay ends on its check-out day.
 */
fun pastReservations(reservations: List<Reservation>, today: String): List<Reservation> {
    val past = mutableListOf<Reservation>()
    for (reservation in reservations) {
        // Dates look like "2027-05-07", so comparing the text also compares the dates.
        if (reservation.status == "confirmed" && reservation.checkOutDate <= today) {
            past.add(reservation)
        }
    }
    return past.sortedByDescending { it.checkOutDate }
}

/**
 * Confirmed reservations whose stay hasn't ended yet, earliest first.
 * [today] is a date like "2027-05-07". A stay ends on its check-out day.
 */
fun activeReservations(reservations: List<Reservation>, today: String): List<Reservation> {
    val active = mutableListOf<Reservation>()
    for (reservation in reservations) {
        // Dates look like "2027-05-07", so comparing the text also compares the dates.
        if (reservation.status == "confirmed" && reservation.checkOutDate > today) {
            active.add(reservation)
        }
    }
    return active.sortedBy { it.checkInDate }
}

/**
 * The booked dates for one campsite, one stay per line, earliest first.
 * Only confirmed reservations count; rejected or cancelled ones have freed their nights.
 */
fun bookedDatesFor(campsiteId: String, reservations: List<Reservation>): String {
    val booked = mutableListOf<Reservation>()
    for (reservation in reservations) {
        if (reservation.campsiteId == campsiteId && reservation.status == "confirmed") {
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
}