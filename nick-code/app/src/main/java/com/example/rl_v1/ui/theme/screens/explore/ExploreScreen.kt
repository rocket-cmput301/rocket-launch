package com.example.rl_v1.ui.theme.screens.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rl_v1.ui.theme.components.LargeButtonPrimary
import com.example.rl_v1.ui.theme.components.PrimaryTextBox
import com.example.rl_v1.ui.theme.components.ScreenHeader

@Composable
fun ExploreScreen(){
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize().background(Color.White).padding(top = 16.dp)
    ){
        item{
            ScreenHeader("Explore Demo Screen")
        }
        item{
            PrimaryTextBox(
                value = "",
                label = "Sample Camper",
                Modifier.fillMaxSize(),
                placeholder = "Device Linked Profile - No password needed\nChange profile photo"
            )
        }
        item{
            PrimaryTextBox(
                value = "",
                label = "Personal Information",
                Modifier.fillMaxSize(),
                placeholder = "Name: Sample Camper\n" +
                        "Email: camper@example.com\n" +
                        "Phone: 123-456-7890"

            )
        }
        item{
            PrimaryTextBox(
                value = "",
                label = "Your Outdoor Preferences",
                Modifier.fillMaxSize(),
                placeholder = "Forest Hiking Lakeside\nPersonalizes featured campsites"

            )
        }
        item{
            PrimaryTextBox(
                value = "",
                label = "Make it yours",
                Modifier.fillMaxSize(),
                placeholder = "Accessibility Settings >\nNotifications: On"
            )
        }
        item{
            LargeButtonPrimary("Save Profile", onClick = { /*TODO*/ })
        }

    }


}

@Preview
@Composable
fun ExploreScreenPreview(){
    ExploreScreen()
}





