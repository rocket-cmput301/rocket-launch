package com.example.rl_v1.ui.theme.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.rl_v1.ui.theme.components.Header1
import com.example.rl_v1.ui.theme.components.LargeButtonPrimary
import com.example.rl_v1.ui.theme.components.LargeButtonSecondary
import com.example.rl_v1.ui.theme.components.PrimaryClickableText
import com.example.rl_v1.ui.theme.components.PrimaryTextBox
import com.example.rl_v1.ui.theme.components.ScreenHeader
import com.example.rl_v1.ui.theme.theme.Forest
import com.example.rl_v1.ui.theme.theme.GreyGreen

/**
 * App policies and guidelines shown from the Profile tab, as (section title, rules).
 * shortcut: shipped with the app, so changes reach users on the next app update; move to Firestore if they must change without a release.
 */
val APP_POLICIES = listOf(
    "Respect other campers" to "Keep your reviews and messages respectful.\nDo not post harassment or personal information.",
    "Reservations" to "Joining a lottery does not reserve dates.\nSelected entrants choose from the remaining dates.",
    "Your profile" to "Update your details or delete your profile in Profile settings."
)

/** Profile tab, with buttons to publish and manage campsites. Most profile settings are placeholders. */
@Composable
fun ProfileScreen(
    onPublishCampsite: () -> Unit = {},
    onManageCampsites: () -> Unit = {},
    onRegisterLottery: () -> Unit = {}
){
    var showPolicies by rememberSaveable { mutableStateOf(false) }

    if (showPolicies) {
        // Built from app colours, not AlertDialog: the theme's dynamic colour would tint a stock dialog.
        Dialog(onDismissRequest = { showPolicies = false }) {
            Surface(shape = RoundedCornerShape(24.dp), color = Color.White) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "Policies and Guidelines",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Forest
                    )
                    for ((title, rules) in APP_POLICIES) {
                        Column {
                            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Forest)
                            Text(rules, style = MaterialTheme.typography.bodyMedium, color = GreyGreen)
                        }
                    }
                    LargeButtonSecondary("Close", onClick = { showPolicies = false })
                }
            }
        }
    }

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
            LargeButtonPrimary("Register Lottery", onClick = onRegisterLottery)
        }
        /*
        item{
            LargeButtonPrimary("Update Lottery", onClick = onUpdateLottery)
        }
        */
        item{
            LargeButtonSecondary("Manage my campsites", onClick = onManageCampsites)
        }
        item{
            PrimaryTextBox(
                value = "\"Device Linked Profile · No password needed\nChange profile photo",
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
                onClick = { showPolicies = true },
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