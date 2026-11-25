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
import java.util.Locale

/**
 * Owner overview (Fig. 13): the signed-in user's campsites and the
 * reservations made on them. Tapping a campsite opens its edit screen.
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
                val rejected = reservation.status == "rejected"
                var label = campsiteName.uppercase()
                if (rejected) {
                    label = "REJECTED • $label"
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    PrimaryTextBox(
                        value = "${reservation.checkInDate} to ${reservation.checkOutDate} • " +
                                "${reservation.numberOfGuests} guests • " +
                                "$" + String.format(Locale.US, "%.2f", reservation.totalPrice),
                        label = label
                    )
                    if (!rejected) {
                        SmallButtonPrimary("Reject", onClick = { vm.reject(reservation) })
                    }
                }
            }
        }
    }
}