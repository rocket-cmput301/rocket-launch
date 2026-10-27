package com.example.rl_v1.ui.theme.comp

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.rl_v1.ui.theme.Cream
import com.example.rl_v1.ui.theme.DarkGreen
import com.example.rl_v1.ui.theme.GreyGreen

enum class BottomTab(
    val label: String,
    val icon: ImageVector,
    val boldLabel: Boolean = false
) {
    Explore("Explore", Icons.Outlined.Search, boldLabel = true),
    Activity("Activity", Icons.Outlined.CalendarToday),
    Inbox("Inbox", Icons.Outlined.ChatBubbleOutline),
    Profile("Profile", Icons.Outlined.PersonOutline)
}

@Composable
fun AppBottomBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = Color.White,
        tonalElevation = 0.dp
    ) {
        BottomTab.entries.forEach { tab ->
            BottomBarItem(
                tab = tab,
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BottomBarItem(
    tab: BottomTab,
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

    Column(
        modifier = modifier
            .fillMaxHeight()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
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

@Composable
fun MainScaffold() {
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