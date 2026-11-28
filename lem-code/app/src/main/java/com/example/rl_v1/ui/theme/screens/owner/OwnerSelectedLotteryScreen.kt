package com.example.rl_v1.ui.theme.screens.owner

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rl_v1.model.Campsite
import com.example.rl_v1.model.Lottery
import com.example.rl_v1.ui.theme.components.LargeButtonPrimary
import com.example.rl_v1.ui.theme.components.PrimaryClickableText
import com.example.rl_v1.ui.theme.components.PrimaryTextBox
import com.example.rl_v1.ui.theme.components.ScreenHeader
import com.example.rl_v1.ui.theme.components.SmallButtonPrimary
import com.example.rl_v1.ui.theme.theme.DarkGreen
import com.example.rl_v1.ui.theme.viewmodel.CampsiteViewModel

/**
 *  Based on Fig. 14 on the Figma: a screen for an Owner to view the details of a selected lottery,
 *  and buttons to edit or view the entrants of said lottery
 */

@Composable
fun OwnerSelectedLotteryScreen(
    vm: CampsiteViewModel,
    lottery: Lottery,
    onBack: () -> Unit,
    onUpdate: (lotteryId: String) -> Unit,
    onViewWaitlist: (lotteryId: String) -> Unit,
    onSend: () -> Unit,
    ){

    BackHandler(onBack=onBack)

    //val campsites by vm.ownerCampsites.collectAsStateWithLifecycle()      //actual
    val campsites by vm.specificOwnerSite(ownerID = "8bkEYENWDCWqz4mRcdTnWGeLmbv2").collectAsStateWithLifecycle()   //temporary just to see the 'manage campsites' view when campsites are actually implemented


    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize().background(Color.White).padding(top = 16.dp)
    ) {
        item{
            val correspondingCampsites = campsites.filter { it.id in lottery.campsiteIds }
            ScreenHeader(correspondingCampsites.joinToString { it.name })
        }

        item{
            val correspondingCampsites = campsites.filter { it.id in lottery.campsiteIds }
            PrimaryTextBox(
                label = "Campsites in this lottery",
                value = correspondingCampsites.joinToString { it.name }
            )
        }

        item{
            PrimaryTextBox(
                label = "Registration Window",
                value = "${lottery.registrationPeriod.startDate}-${lottery.registrationPeriod.endDate} • Automatic draw at close"
            )
        }

        item{
            PrimaryTextBox(
                label = "Booking Window",
                value = "${lottery.bookingPeriod.startDate}-${lottery.bookingPeriod.endDate} • Choose available stay dates"
            )
        }

        item{
            PrimaryTextBox(
                label = "Draw Settings",
                value = "${lottery.maxWinners} Winners • Waitlist limit: ${lottery.capacity}"
            )
        }

        item{
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallButtonPrimary(text = "Edit settings", onClick = { onUpdate(lottery.id) } )
                SmallButtonPrimary(
                    text = "View entrants",
                    onClick = { onViewWaitlist(lottery.id) })    //view waitlist (US 02.09.01)
            }
        }

        item{
            LargeButtonPrimary(text = "Send waitlist announcement", onClick = onSend)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)){
                PrimaryClickableText(text = "Sent notices", textColor = DarkGreen, onClick = {
                    //TODO: view announcements made to entrants in THIS waitlist (US 02.10.02?)
                })
                PrimaryClickableText(text = "Reservation messages", textColor = DarkGreen, onClick = {
                    //TODO: access messages for reservations made in this lottery
                })
            }
        }


    }





}