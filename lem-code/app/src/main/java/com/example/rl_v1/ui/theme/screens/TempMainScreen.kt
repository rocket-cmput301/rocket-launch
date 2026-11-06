package com.example.rl_v1.ui.theme.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.rl_v1.navigation.AppNavHost
import com.example.rl_v1.ui.theme.components.AppBottomBar
import com.example.rl_v1.ui.theme.viewmodel.CampsiteViewModel
import com.example.rl_v1.ui.theme.components.BottomTab

/** Main app layout: the bottom tab bar and the current screen. */
@Composable
fun TempMainScreen() {
    val vm : CampsiteViewModel = viewModel()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    var lastTab by rememberSaveable { mutableStateOf(BottomTab.Explore) }
    val routeTab = BottomTab.entries.firstOrNull { it.name == currentRoute }
    LaunchedEffect(routeTab) {
        if (routeTab != null) {
            lastTab = routeTab
        }
    }
    val selectedTab = routeTab ?: lastTab

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White,
        bottomBar = {
            AppBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    if (tab == selectedTab) {
                        navController.popBackStack(tab.name, inclusive = false)
                    } else {
                        lastTab = tab
                        navController.navigate(tab.name) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            vm = vm,
            modifier = Modifier.padding(innerPadding)
        )
    }
}