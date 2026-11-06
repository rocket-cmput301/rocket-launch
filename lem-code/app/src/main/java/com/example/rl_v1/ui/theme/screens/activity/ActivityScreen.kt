package com.example.rl_v1.ui.theme.screens.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rl_v1.ui.theme.components.LargeButtonPrimary
import com.example.rl_v1.ui.theme.components.PrimaryClickableText
import com.example.rl_v1.ui.theme.components.PrimaryTextBox
import com.example.rl_v1.ui.theme.components.ScreenHeader
import com.example.rl_v1.ui.theme.components.SecondaryTextBox
import com.example.rl_v1.ui.theme.components.SmallButtonPrimary
import com.example.rl_v1.ui.theme.theme.DarkGreen

@Composable
fun ActivityScreen(){
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize().background(Color.White).padding(top = 16.dp)
    ){
        item{
            ScreenHeader("Lottery Status")
        }
        item{
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallButtonPrimary(text = "Lotteries", onClick = {})
                SmallButtonPrimary(text = "Reservations", onClick = {})
            }

        }
        item{
            PrimaryTextBox(
                value = "You can book now. Choose remaining dates by [DATE]",
                label = "SELECTED • [CAMPSITE]",
                Modifier.fillMaxSize(),

            )
        }
        item{
            LargeButtonPrimary("Choose campsite & dates", onClick = { /*TODO*/ })
        }
        item{
            PrimaryClickableText(
                text = "Decline Opportunity",
                textColor = DarkGreen,
                onClick = { /*TODO*/ },
                modifier = Modifier.fillMaxSize(),

            )
        }
        item{
            PrimaryTextBox(
                value = "Draw on [DATE]. You are on the waiting list",
                label = "JOINED • [CAMPSITE]",
                Modifier.fillMaxSize(),
            )}
        item{
            PrimaryClickableText(
                text = "Leave waiting list",
                textColor = DarkGreen,
                onClick = { /*TODO*/ },
                modifier = Modifier.fillMaxSize(),
                )
        }
        item{
            SecondaryTextBox(
                value = "You were not selected in this draw. Explore other campsites",
                label = "NOT SELECTED • [CAMPSITE]"
            )
        }

    }
}

@Preview
@Composable
fun ActivityScreenPreview(){
    ActivityScreen()
}