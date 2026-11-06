package com.example.rl_v1.ui.theme.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rl_v1.ui.theme.components.Header1
import com.example.rl_v1.ui.theme.components.LargeButtonPrimary
import com.example.rl_v1.ui.theme.components.PrimaryClickableText
import com.example.rl_v1.ui.theme.components.PrimaryTextBox
import com.example.rl_v1.ui.theme.components.ScreenHeader
import com.example.rl_v1.ui.theme.theme.Forest

@Composable
fun ProfileScreen(

){
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize().background(Color.White).padding(top = 16.dp)
    ){
        item{
            ScreenHeader("Profile and Preferences")
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
            PrimaryClickableText(
                text = "App Policies & Guidelines",
                textColor = Forest,
                onClick = { /*TODO*/ },
                modifier = Modifier.fillMaxSize(),
            )
        }
        item{
            LargeButtonPrimary("Save Profile", onClick = { /*TODO*/ })
        }
        item{
           PrimaryClickableText(
               text = "Delete Profile",
               textColor = Color(0xFFC4532D),
               onClick = { /*TODO*/ },
               modifier = Modifier.fillMaxSize(),
           )

        }


    }


}

@Preview
@Composable
fun ProfileScreenPreview(){
    ProfileScreen()
}