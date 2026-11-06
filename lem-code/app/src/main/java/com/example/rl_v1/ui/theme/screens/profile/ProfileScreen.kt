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
import com.example.rl_v1.ui.theme.components.LargeButtonSecondary
import com.example.rl_v1.ui.theme.components.PrimaryClickableText
import com.example.rl_v1.ui.theme.components.PrimaryTextBox
import com.example.rl_v1.ui.theme.components.ScreenHeader
import com.example.rl_v1.ui.theme.theme.Forest

/** Profile tab, with buttons to publish and manage campsites. Most profile settings are placeholders. */
@Composable
fun ProfileScreen(
    onPublishCampsite: () -> Unit = {},
    onManageCampsites: () -> Unit = {}
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
            Header1("Hosting")
        }
        item{
            LargeButtonPrimary("Publish a campsite", onClick = onPublishCampsite)
        }
        item{
            LargeButtonSecondary("Manage my campsites", onClick = onManageCampsites)
        }
        item{
            PrimaryTextBox(
                value = "\"Device Linked Profile - No password needed\nChange profile photo",
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
                value = "\"Forest Hiking Lakeside\\nPersonalizes featured campsites\"",
                label = "Your Outdoor Preferences",
                Modifier.fillMaxSize(),

            )
        }
        item{
            PrimaryTextBox(
                value = "Accessibility Settings >\nNotifications: On",
                label = "Make it yours",
                Modifier.fillMaxSize(),

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