package com.example.rl_v1.ui.theme.screens.inbox

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
import com.example.rl_v1.ui.theme.components.PrimaryTextBox
import com.example.rl_v1.ui.theme.components.ScreenHeader
import com.example.rl_v1.ui.theme.viewmodel.CampsiteViewModel

/** Inbox tab: the entrant's lottery results and reservation confirmations, updated live. */
@Composable
fun InboxScreen(vm: CampsiteViewModel) {
    val reservations by vm.entrantReservations.collectAsStateWithLifecycle()
    val won by vm.userWonLotteries.collectAsStateWithLifecycle()
    val lost by vm.userLostLotteries.collectAsStateWithLifecycle()
    val campsites by vm.allCampsites.collectAsStateWithLifecycle()
    val notices = inboxItems(reservations, won, lost, campsites)

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize().background(Color.White).padding(top = 16.dp)
    ) {
        item { ScreenHeader("Inbox") }
        if (notices.isEmpty()) {
            item { Text("No notifications yet. Lottery results and reservation confirmations show up here.") }
        }
        items(notices) { notice ->
            PrimaryTextBox(value = notice.text, label = notice.label, Modifier.fillMaxWidth())
        }
    }
}
