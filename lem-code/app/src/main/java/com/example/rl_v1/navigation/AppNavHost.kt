package com.example.rl_v1.navigation

import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.rl_v1.ui.theme.screens.activity.ActivityScreen
import com.example.rl_v1.ui.theme.screens.explore.ExploreScreen
import com.example.rl_v1.ui.theme.screens.inbox.InboxScreen
import com.example.rl_v1.ui.theme.screens.profile.ProfileScreen
import com.example.rl_v1.ui.theme.components.BottomTab
import com.example.rl_v1.ui.theme.screens.explore.CampsiteSelectScreen
import com.example.rl_v1.ui.theme.viewmodel.CampsiteViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    vm: CampsiteViewModel
) {
    NavHost(
        navController = navController,
        startDestination = BottomTab.Explore.name,
        modifier = modifier
    ) {
        /* EXPLORE SCREEN
        -----------------------------------------------------------------------------------*/
        composable(BottomTab.Explore.name)
        { ExploreScreen(onSwipeUp = { navController.navigate(Routes.CAMPSITE_SELECT) }) }

        composable(
            route = Routes.CAMPSITE_SELECT,
            enterTransition = { slideInVertically { it } },   // slides up from the bottom
            exitTransition = { fadeOut() },
            popExitTransition = { slideOutVertically { it } } // slides back down on Back
        ){
            CampsiteSelectScreen(onBack = { navController.popBackStack() })
        }

        /*---------------------------------------------------------------------------*/

        composable(BottomTab.Activity.name) { ActivityScreen() }
        composable(BottomTab.Inbox.name)    { InboxScreen() }
        composable(BottomTab.Profile.name)  { ProfileScreen() }
    }
}