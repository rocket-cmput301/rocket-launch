package com.example.rl_v1.navigation

import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.rl_v1.ui.theme.screens.explore.MakeFilterScreen
import com.example.rl_v1.ui.theme.screens.explore.CampsiteDetailsScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.navArgument


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
        composable(BottomTab.Explore.name){
            ExploreScreen(
                vm = vm,
                onSwipeUp = { navController.navigate(Routes.CAMPSITE_SELECT) },
                onOpenFilters = { navController.navigate(Routes.FILTERS) }
            )
        }
        composable(Routes.FILTERS){
            MakeFilterScreen(confirmFilters = { distanceFilter, dates, numGuests, preferences, turnOffFilterScreen ->
                vm.setFilters(distanceFilter, dates, numGuests, preferences)
                navController.popBackStack()
            })
        }


        composable(
            route = Routes.CAMPSITE_SELECT,
            enterTransition = { slideInVertically { it } },   // slides up from the bottom
            exitTransition = { fadeOut() },
            popExitTransition = { slideOutVertically { it } } // slides back down on Back
        ){
            CampsiteSelectScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onCampsiteClick = { id -> navController.navigate("${Routes.CAMPSITE_DETAILS}/$id") }
            )
        }
        composable(
            route = "${Routes.CAMPSITE_DETAILS}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {entry ->
            val id = entry.arguments?.getString("id")
            val all by vm.allCampsites.collectAsStateWithLifecycle()
            val campsite = all.firstOrNull { it.id == id }
            if (campsite != null) {
                CampsiteDetailsScreen(
                    campsite = campsite,
                    onBack = { navController.popBackStack() }
                )
            }

        }

        /*---------------------------------------------------------------------------*/

        composable(BottomTab.Activity.name) { ActivityScreen() }
        composable(BottomTab.Inbox.name)    { InboxScreen() }
        composable(BottomTab.Profile.name)  { ProfileScreen() }
    }
}