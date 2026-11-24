package com.example.rl_v1.ui.theme.screens.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.rl_v1.ui.theme.components.LargeButtonPrimary
import com.example.rl_v1.ui.theme.components.PrimaryClickableText
import com.example.rl_v1.ui.theme.components.PrimaryTextBox
import com.example.rl_v1.ui.theme.components.ScreenHeader
import com.example.rl_v1.ui.theme.components.SecondaryTextBox
import com.example.rl_v1.ui.theme.components.SmallButtonPrimary
import com.example.rl_v1.ui.theme.theme.DarkGreen
import com.example.rl_v1.ui.theme.viewmodel.CampsiteViewModel
import java.util.Locale
import androidx.compose.runtime.remember

/** Activity tab: the user's reservations, and placeholder lottery cards. */
@Composable
fun ActivityScreen(
    vm: CampsiteViewModel,
    onBook: (String) -> Unit
){
    var showReservations by rememberSaveable { mutableStateOf(false) }
    val reservations by vm.entrantReservations.collectAsStateWithLifecycle()
    val campsites by vm.allCampsites.collectAsStateWithLifecycle()
    val joinedLotteries by vm.userJoinedLotteries.collectAsStateWithLifecycle()
    val selectedLotteries by vm.userWonLotteries.collectAsStateWithLifecycle()       //haven't been implemented yet
    val notSelectedLotteries by vm.userLostLotteries.collectAsStateWithLifecycle()   //haven't been implemented yet

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 16.dp)
    ){
        item{
            ScreenHeader(if (showReservations) "Your Reservations" else "Lottery Status")
        }
        item{
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallButtonPrimary(text = "Lotteries", onClick = { showReservations = false }, dark = !showReservations)
                SmallButtonPrimary(text = "Reservations", onClick = { showReservations = true }, dark = showReservations)
            }

        }
        if (showReservations) {
            if (reservations.isEmpty()) {
                item { Text("You have no reservations yet.") }
            }
            items(reservations, key = { it.id }) { reservation ->
                val name = campsites.firstOrNull { it.id == reservation.campsiteId }?.name ?: "Campsite"
                PrimaryTextBox(
                    value = "${reservation.checkInDate} to ${reservation.checkOutDate} • " +
                            "${reservation.numberOfGuests} guests • $" +
                            String.format(Locale.US, "%.2f", reservation.totalPrice),
                    label = "${reservation.status.uppercase()} • ${name.uppercase()}"
                )
            }
        } else {
            if (selectedLotteries.isEmpty()){
                item{
                    Text("You have not been selected in any Lotteries.")
                }
            }
            else{
                items(selectedLotteries, key = {it.id}) { lottery ->
                    var confirmDecline by remember {mutableStateOf(false)}
                    val correspondingCampsite = campsites.filter { it.id in lottery.campsiteIds }
                    PrimaryTextBox(
                        value = "You can book now. Choose remaining dates before ${lottery.bookingPeriod.endDate}.",
                        label = "SELECTED • ${correspondingCampsite.joinToString { it.name }}",
                        Modifier.fillMaxSize().padding(bottom = 8.dp),
                    )
                    LargeButtonPrimary(
                        "Choose campsite & dates",
                        onClick = { onBook(lottery.id) }                            //prompt the same 'Select Dates' button from CampsiteDetailsScreen.kt (US 01.04.01 and US 01.04.02)
                    )
                    if (confirmDecline){
                        PrimaryClickableText(
                            text = "Cancel Decline",
                            textColor = DarkGreen,
                            onClick = { confirmDecline = false },
                            modifier = Modifier.fillMaxSize().padding(top = 8.dp),
                        )
                        PrimaryClickableText(
                            text = "Confirm Decline",
                            textColor = DarkGreen,
                            onClick = { vm.callDeclineOp(campID = lottery.campsiteIds[0]) },              //set status to 'TERMINATED' after a confirmation (US 01.04.03)
                            modifier = Modifier.fillMaxSize().padding(top = 8.dp),
                        )
                    }
                    else{
                        PrimaryClickableText(
                            text = "Decline Opportunity",
                            textColor = DarkGreen,
                            onClick = { confirmDecline = true },
                            modifier = Modifier.fillMaxSize().padding(top = 8.dp),
                        )
                    }
                }
            }

            //disposed of placeholder
            if (joinedLotteries.isEmpty()) {
                item {
                    Text("You are not in any Lottery Waitlists.")
                }
            }
            else {
                items(joinedLotteries, key = { it.id }) { lottery ->
                    var confirmPrompt by remember { mutableStateOf(false) }
                    val correspondingCampsite =
                        campsites.filter { it.id in lottery.campsiteIds }  //uses filter because multiple campsites may be grouped
                    PrimaryTextBox(
                        value = "Draw after ${lottery.registrationPeriod.endDate}",
                        label = "JOINED • ${correspondingCampsite.joinToString { it.name }}"
                    )

                    if (confirmPrompt) {
                        PrimaryClickableText(
                            text = "Cancel Leave",
                            textColor = DarkGreen,
                            onClick = {
                                confirmPrompt = false
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                        PrimaryClickableText(
                            text = "Confirm Leave",
                            textColor = DarkGreen,
                            onClick = {
                                vm.callLeaveLottery(lottery.campsiteIds[0])     //remove first campsite, if its a list it removes it anyway due to filter in callLeave()
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        PrimaryClickableText(
                            text = "Leave waiting list",
                            textColor = DarkGreen,
                            onClick = {
                                confirmPrompt = true    //confirm first
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    SmallButtonPrimary(text = "Dev Test Lottery Selection", onClick = {vm.callWinnerSelection(lottery.campsiteIds[0])})     //TEST BUTTON JUST TO WIN A LOTTERY
                }
            }
            if (notSelectedLotteries.isEmpty()){
                item {
                    Text("You have not lost any lotteries.")
                }
            }
            else{
                items(notSelectedLotteries, key = {it.id}){ lottery ->
                    val correspondingCampsite = campsites.filter { it.id in lottery.campsiteIds }  //uses filter because multiple campsites may be grouped
                    SecondaryTextBox(
                        value = "You were not selected in this draw. Explore other campsites",
                        label = "NOT SELECTED • ${correspondingCampsite.joinToString { it.name }}"
                    )
                }
            }
            item{
                //TESTING BUTTON: resets the statuses of the Lotteries by deleting them
                SmallButtonPrimary(
                    text = "Dev Test Lottery Reset",
                    onClick = { vm.resetEntries() }
                )
            }
        }
    }
}