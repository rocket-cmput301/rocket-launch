package com.example.rl_v1.ui.theme.screens.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.example.rl_v1.repository.fakeLottery.FakeLotteryRepository
import com.example.rl_v1.ui.theme.components.CampsiteCard
import com.example.rl_v1.ui.theme.components.LargeButtonPrimary
import com.example.rl_v1.ui.theme.components.PrimaryClickableText
import com.example.rl_v1.ui.theme.components.PrimaryTextBox
import com.example.rl_v1.ui.theme.components.ScreenHeader
import com.example.rl_v1.ui.theme.components.SecondaryTextBox
import com.example.rl_v1.ui.theme.components.SmallButtonPrimary
import com.example.rl_v1.ui.theme.theme.DarkGreen
import com.example.rl_v1.ui.theme.viewmodel.CampsiteViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.util.Locale
import androidx.compose.runtime.collectAsState

/** Activity tab: the user's reservations, and placeholder lottery cards. */
@Composable
fun ActivityScreen(vm: CampsiteViewModel){
    var showReservations by rememberSaveable { mutableStateOf(false) }
    val reservations by vm.entrantReservations.collectAsStateWithLifecycle()
    val campsites by vm.allCampsites.collectAsStateWithLifecycle()
    val joinedLotteries by vm.userJoinedLotteries.collectAsStateWithLifecycle()        //testing ui
    //val selectedLotteries by vm.userWonLotteries.collectAsStateWithLifecycle()       //haven't been implemented yet
    //val notSelectedLotteries by vm.userLostLotteries.collectAsStateWithLifecycle()   //haven't been implemented yet

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
        } else {        //showLotteries - currently placeholders still
        item{
            PrimaryTextBox(
                value = "You can book now. Choose remaining dates by [DATE]",
                label = "SELECTED • [CAMPSITE]",
                Modifier.fillMaxSize(),

            )
        }
        item{
            LargeButtonPrimary("Choose campsite & dates", onClick = { /*TODO*/ })
        }
        item{
            PrimaryClickableText(
                text = "Decline Opportunity",
                textColor = DarkGreen,
                onClick = { /*TODO*/ },
                modifier = Modifier.fillMaxSize(),

            )
        }
        //disposed of placeholder -- still testing
        if (joinedLotteries.isEmpty()){
            item {
                Text("You are not in any Lottery Waitlists.")
            }
        }
        else{
            items(joinedLotteries, key = {it.id}) { lottery ->
                val correspondingCampsite = campsites.filter{it.id in lottery.campsiteIds}  //uses filter because multiple campsites may be grouped
                PrimaryTextBox(
                    value = "Draw after ${lottery.registrationPeriod.endDate}",
                    label = "JOINED • ${correspondingCampsite.joinToString { it.name }}"
                )
                PrimaryClickableText(                   //trying one text per WaitlistEntry first to ensure functionality
                    text = "Leave waiting list",
                    textColor = DarkGreen,
                    onClick = {

                        vm.callLeave(lottery.campsiteIds[0])    //remove first campsite, if its a list it removes it anyway due to filter in callLeave()
                        //delete this item
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

            /*
        item{
            PrimaryClickableText(
                text = "Leave waiting list",
                textColor = DarkGreen,
                onClick = {
                    //text is a toggle, then you pick which one you want removed

                    //vm.callLeave(campId = campsite)
                },
                modifier = Modifier.fillMaxSize(),
                )
        }*/

        item{
            SecondaryTextBox(
                value = "You were not selected in this draw. Explore other campsites",
                label = "NOT SELECTED • [CAMPSITE]"
            )
        }
        }

    }
}