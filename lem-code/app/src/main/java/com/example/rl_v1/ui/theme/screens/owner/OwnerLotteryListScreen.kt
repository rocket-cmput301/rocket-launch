package com.example.rl_v1.ui.theme.screens.owner

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rl_v1.model.Lottery
import com.example.rl_v1.ui.theme.components.LargeButtonPrimary
import com.example.rl_v1.ui.theme.components.PrimaryTextBox
import com.example.rl_v1.ui.theme.components.ScreenHeader
import com.example.rl_v1.ui.theme.viewmodel.CampsiteViewModel

/**
 *  Based on Fig. 22 on the Figma: a screen for an Owner to view the entrants of a particular lottery
 */
@Composable
fun OwnerLotteryListScreen(
    vm: CampsiteViewModel,
    lottery: Lottery,
    onBack: () -> Unit,
    onSend: () -> Unit
){
    BackHandler(onBack = onBack)

    //val campsites by vm.ownerCampsites.collectAsStateWithLifecycle()
    val campsites by vm.specificOwnerSite(ownerID = "8bkEYENWDCWqz4mRcdTnWGeLmbv2").collectAsStateWithLifecycle()   //temporary just to see the 'manage campsites' view when campsites are actually implemented

    val myWaitlistEntries by remember(lottery.id) {
        vm.callViewOwnerWaitlistEntries(lotteryID = lottery.id, ownerID = "")
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize().background(Color.White).padding(top = 16.dp)
    ) {
        item{ ScreenHeader("Lottery Entrants") }

        item{       //same block from OwnerCampsitesScreen

            val correspondingCampsites = campsites.filter { it.id in lottery.campsiteIds }
            PrimaryTextBox(
                label = correspondingCampsites.joinToString { it.name },
                value = "${myWaitlistEntries.size} Entrants • ${lottery.maxWinners} winners"
            )
        }

        item{       //?? not sure what this means
            PrimaryTextBox(
                label = "View",
                value = "Waitlist selected  Sent notifications"
            )
        }

        item{ Text("ENTRANTS") }

        items(myWaitlistEntries, key = {it.id}){ waitlistEntry ->   // one box per entrant based on fig.22
            PrimaryTextBox(
                label = "Entrant ID: ${waitlistEntry.entrantId}",
                value = "${waitlistEntry.status} | Joined at: ${waitlistEntry.joinedAt}"
                //'WON' = selected, 'WAITING' = just waiting, 'TERMINATED' = Not selected or optionally opted out
            )
        }
        item{
            LargeButtonPrimary(text = "Send announcement", onClick = onSend)
            Text("Statuses update after the automatic draw", fontSize = 12.sp)
        }
    }
}