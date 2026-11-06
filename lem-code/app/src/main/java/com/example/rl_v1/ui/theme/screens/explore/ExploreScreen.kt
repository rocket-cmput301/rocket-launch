package com.example.rl_v1.ui.theme.screens.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rl_v1.ui.theme.components.LargeButtonPrimary
import com.example.rl_v1.ui.theme.components.MapSearch
import com.example.rl_v1.ui.theme.components.PrimaryTextBox
import com.example.rl_v1.ui.theme.components.ScreenHeader
import com.example.rl_v1.ui.theme.components.SmallButtonPrimary
import com.example.rl_v1.ui.theme.components.SwipeUpPanel
import com.example.rl_v1.ui.theme.viewmodel.CampsiteViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.example.rl_v1.ui.theme.components.AppInputTextField


@Composable
fun ExploreScreen(vm: CampsiteViewModel, onSwipeUp: () -> Unit, onOpenFilters: () -> Unit) {
    val campsites by vm.filteredCampsites.collectAsStateWithLifecycle()
    val query by vm.searchQuery.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()
    Box(Modifier.fillMaxSize().background(Color.White)) {
        // TODO: ADD MAP

        //Top searchbar and filter controls
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),

            ) {
            message?.let { Text(it, color = Color.Red) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppInputTextField(
                    value = query, onValueChange = vm::setSearchQuery,
                    label = "Where to?", placeholder = "Name or Location"
                )

                SmallButtonPrimary("Filters", onClick = onOpenFilters)
            }
            Box(Modifier.fillMaxWidth()) {
                SmallButtonPrimary(
                    "Search this area", onClick = { },
                    dark = true,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Box(Modifier.fillMaxWidth()) {
                SmallButtonPrimary(
                    "Scan QR", onClick = { },
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }

        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SmallButtonPrimary("Within 50 km", onClick = { })
                SmallButtonPrimary("Locate me", onClick = { })
            }


            SwipeUpPanel(
                title = "${campsites.size} campsites",
                subtitle = "Swipe up for featured stays and best matches",
                onSwipeUp = onSwipeUp,
                )
        }
    }
}