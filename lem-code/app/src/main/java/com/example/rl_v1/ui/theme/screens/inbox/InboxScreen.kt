package com.example.rl_v1.ui.theme.screens.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.rl_v1.ui.theme.components.LargeButtonPrimary
import com.example.rl_v1.ui.theme.components.PrimaryTextBox
import com.example.rl_v1.ui.theme.components.ScreenHeader

@Composable
fun InboxScreen(){
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize().background(Color.White).padding(top = 16.dp)
    ){
        item{
            ScreenHeader("Inbox Demo Screen")
        }
        item{
            PrimaryTextBox(
                value = "",
                label = "Sample Camper",
                Modifier.fillMaxSize(),

            )
        }
        item{
            PrimaryTextBox(
                value = "",
                label = "Personal Information",
                Modifier.fillMaxSize(),


            )
        }
        item{
            PrimaryTextBox(
                value = "",
                label = "Your Outdoor Preferences",
                Modifier.fillMaxSize(),


            )
        }
        item{
            PrimaryTextBox(
                value = "",
                label = "Make it yours",
                Modifier.fillMaxSize(),

            )
        }
        item{
            LargeButtonPrimary("Send Message", onClick = { /*TODO*/ })
        }

    }
}