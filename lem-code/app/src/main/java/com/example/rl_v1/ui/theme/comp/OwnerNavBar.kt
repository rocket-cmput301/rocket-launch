package com.example.rl_v1.ui.theme.comp

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rl_v1.ui.theme.Cream
import com.example.rl_v1.ui.theme.DarkGreen
import com.example.rl_v1.ui.theme.GreyGreen

enum class OwnerBottomTab(
    val label: String,
    val icon: ImageVector,
    val boldLabel: Boolean = false
) {
    Explore("Campsites", Icons.Outlined.Search, boldLabel = true),
    Activity("Lotteries", Icons.Outlined.CalendarToday),
    Inbox("Bookings", Icons.Outlined.ChatBubbleOutline),
    Profile("Inbox", Icons.Outlined.PersonOutline)
}

@Composable
fun AppBottomBar(
    selectedTab: OwnerBottomTab,
    onTabSelected: (OwnerBottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        containerColor = Color.White,
        tonalElevation = 0.dp
    ) {
        OwnerBottomTab.entries.forEach { tab ->
            OwnerBottomBarItem(
                tab = tab,
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun OwnerBottomBarItem(
    tab: OwnerBottomTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val circleColor by animateColorAsState(
        targetValue = if (selected) DarkGreen else Color.Transparent,
        label = "circleColor"
    )

    // normal tabs go black -> green when selected.
    // The bold tab (Explore) goes grey-green -> black when selected.
    val labelColor = when {
        tab.boldLabel && selected -> Color.Black
        tab.boldLabel -> GreyGreen
        selected -> DarkGreen
        else -> Color.Black
    }

    Box(
        modifier = modifier
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(circleColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = null,
                    tint = if (selected) Color.White else GreyGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = tab.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (tab.boldLabel) FontWeight.Bold else FontWeight.Normal,
                color = labelColor
            )
        }
    }
}

@Composable
fun OwnerMainScaffold() {
    var selectedTab by rememberSaveable { mutableStateOf(BottomTab.Explore) }

    Scaffold(
        containerColor = Cream,
        bottomBar = {
            AppBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Swap these for real screens later
            when (selectedTab) {
                BottomTab.Explore -> Text("Explore")
                BottomTab.Activity -> Text("Activity")
                BottomTab.Inbox -> Text("Inbox")
                BottomTab.Profile -> Text("Profile")
            }
        }
    }
}