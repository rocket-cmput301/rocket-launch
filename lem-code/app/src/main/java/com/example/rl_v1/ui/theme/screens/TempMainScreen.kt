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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.rl_v1.model.UserRole
import com.example.rl_v1.navigation.AppNavHost
import com.example.rl_v1.ui.theme.components.AppBottomBar
import com.example.rl_v1.ui.theme.components.BottomTab
import com.example.rl_v1.ui.theme.viewmodel.CampsiteViewModel
import com.example.rl_v1.ui.theme.viewmodel.UserViewModel

/** Main app layout: the bottom tab bar and the current screen. */
@Composable
fun TempMainScreen(
    onProfileDeleted: () -> Unit = {},
    userViewModel: UserViewModel = viewModel()
) {
    val vm: CampsiteViewModel = viewModel()
    val userUiState by userViewModel.uiState.collectAsStateWithLifecycle()
    val isAdmin = userUiState.profile?.role?.contains(UserRole.ADMIN) == true

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    var lastTab by rememberSaveable { mutableStateOf(BottomTab.Explore) }
    val routeTab = BottomTab.entries.firstOrNull { it.name == currentRoute }

    LaunchedEffect(routeTab, isAdmin) {
        if (!isAdmin && lastTab == BottomTab.Admin) {
            lastTab = BottomTab.Explore
        }
        if (routeTab != null && (isAdmin || !routeTab.adminOnly)) {
            lastTab = routeTab
        }
    }

    val candidateTab = routeTab ?: lastTab
    val selectedTab = if (!isAdmin && candidateTab.adminOnly) BottomTab.Explore else candidateTab

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White,
        bottomBar = {
            AppBottomBar(
                selectedTab = selectedTab,
                isAdmin = isAdmin,
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
            onSignedOutToWelcome = onProfileDeleted,
            vm = vm,
            userViewModel = userViewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}