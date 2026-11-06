package com.example.rl_v1.ui.theme.screens.explore

import android.R.attr.text
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rl_v1.ui.theme.components.Header1
import com.example.rl_v1.ui.theme.components.LargeButtonPrimary
import com.example.rl_v1.ui.theme.components.PrimaryTextBox
import com.example.rl_v1.ui.theme.components.ScreenHeader
import com.example.rl_v1.ui.theme.components.SmallButtonPrimary
import com.example.rl_v1.ui.theme.viewmodel.CampsiteViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.example.rl_v1.ui.theme.CampsiteCard

@Composable
fun CampsiteSelectScreen(vm: CampsiteViewModel, onBack: () -> Unit, onCampsiteClick: (String) -> Unit){
    val campsites by vm.filteredCampsites.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()
    Box(Modifier.fillMaxSize().background(Color.White)) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxSize().padding(top = 16.dp)
        ) {
            item {
                ScreenHeader("Select a Campsite")
            }
            message?.let { text ->
                item { Text(text, color = Color.Red) }
            }
            item{
                Text("PLACE DATE GUESTS")
                Spacer(Modifier.height(12.dp))

            }
            item{
                Header1("Featured for You")
            }
            item{
                Text("Based on your forest & hiking preferences")
            }
            item{
                // Images
            }
            item{
                Header1(" Best Matches")
            }
            items(campsites, key = { it.id }) { campsite ->
                CampsiteCard(
                    campsite,
                    Modifier.fillMaxWidth(),
                    onClick = { onCampsiteClick(campsite.id) }
                )
            }
            item {
                LargeButtonPrimary(text = "Explore map", onClick = onBack)
            }
        }
    }
}

