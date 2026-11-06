package com.example.rl_v1.navigation

import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.rl_v1.ui.theme.components.BottomTab
import com.example.rl_v1.ui.theme.screens.CampsiteEditScreen
import com.example.rl_v1.ui.theme.screens.CampsiteRegistrationScreen
import com.example.rl_v1.ui.theme.screens.activity.ActivityScreen
import com.example.rl_v1.ui.theme.screens.explore.CampsiteDetailsScreen
import com.example.rl_v1.ui.theme.screens.explore.CampsiteSelectScreen
import com.example.rl_v1.ui.theme.screens.explore.ExploreScreen
import com.example.rl_v1.ui.theme.screens.explore.MakeFilterScreen
import com.example.rl_v1.ui.theme.screens.inbox.InboxScreen
import com.example.rl_v1.ui.theme.screens.owner.OwnerCampsitesScreen
import com.example.rl_v1.ui.theme.screens.profile.ProfileScreen
import com.example.rl_v1.ui.theme.screens.reservation.ReservationConfirmedScreen
import com.example.rl_v1.ui.theme.screens.reservation.SelectDatesScreen
import com.example.rl_v1.ui.theme.viewmodel.CampsiteViewModel

private val idArgument = listOf(navArgument("id") { type = NavType.StringType })

/** All the app's screens and how to move between them. Starts on the Explore tab. */
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
        /* EXPLORE
        -----------------------------------------------------------------------------------*/
        composable(BottomTab.Explore.name) {
            ExploreScreen(
                vm = vm,
                onSwipeUp = { navController.navigate(Routes.CAMPSITE_SELECT) },
                onOpenFilters = { navController.navigate(Routes.FILTERS) }
            )
        }
        composable(Routes.FILTERS) {
            MakeFilterScreen(confirmFilters = { distanceFilter, dates, numGuests, preferences, _ ->
                vm.setFilters(distanceFilter, dates, numGuests, preferences)
                navController.popBackStack()
            })
        }
        composable(
            route = Routes.CAMPSITE_SELECT,
            enterTransition = { slideInVertically { it } },   // slides up from the bottom
            exitTransition = { fadeOut() },
            popExitTransition = { slideOutVertically { it } } // slides back down on Back
        ) {
            CampsiteSelectScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onCampsiteClick = { id -> navController.navigate("${Routes.CAMPSITE_DETAILS}/$id") }
            )
        }
        composable("${Routes.CAMPSITE_DETAILS}/{id}", arguments = idArgument) { entry ->
            val id = entry.arguments?.getString("id")
            val all by vm.allCampsites.collectAsStateWithLifecycle()
            val campsite = all.firstOrNull { it.id == id }
            if (campsite != null) {
                CampsiteDetailsScreen(
                    campsite = campsite,
                    onBack = { navController.popBackStack() },
                    onBook = { navController.navigate("${Routes.SELECT_DATES}/${campsite.id}") }
                )
            }
        }

        /* RESERVATIONS
        -----------------------------------------------------------------------------------*/
        composable("${Routes.SELECT_DATES}/{id}", arguments = idArgument) { entry ->
            val id = entry.arguments?.getString("id")
            val all by vm.allCampsites.collectAsStateWithLifecycle()
            val message by vm.message.collectAsStateWithLifecycle()
            val campsite = all.firstOrNull { it.id == id }
            LaunchedEffect(Unit) { vm.clearMessage() }

            if (campsite != null) {
                SelectDatesScreen(
                    campsite = campsite,
                    message = message,
                    onBack = { navController.popBackStack() },
                    onConfirm = { checkIn, checkOut, guests ->
                        vm.book(campsite, checkIn, checkOut, guests) { reservation ->
                            navController.navigate("${Routes.RESERVATION_CONFIRMED}/${reservation.id}") {
                                // Back from the confirmation shouldn't reopen the date picker.
                                popUpTo("${Routes.SELECT_DATES}/{id}") { inclusive = true }
                            }
                        }
                    }
                )
            }
        }
        composable("${Routes.RESERVATION_CONFIRMED}/{id}", arguments = idArgument) { entry ->
            val id = entry.arguments?.getString("id")
            val reservations by vm.entrantReservations.collectAsStateWithLifecycle()
            val all by vm.allCampsites.collectAsStateWithLifecycle()
            val reservation = reservations.firstOrNull { it.id == id }
            val campsiteName = all.firstOrNull { it.id == reservation?.campsiteId }?.name ?: "Campsite"

            ReservationConfirmedScreen(
                reservation = reservation,
                campsiteName = campsiteName,
                onDone = {
                    navController.popBackStack(BottomTab.Explore.name, inclusive = false)
                }
            )
        }

        /* OWNER
        -----------------------------------------------------------------------------------*/
        composable(Routes.CAMPSITE_REGISTRATION) {
            CampsiteRegistrationScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onDone = { navController.popBackStack() }
            )
        }
        composable(Routes.OWNER_CAMPSITES) {
            OwnerCampsitesScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onPublish = { navController.navigate(Routes.CAMPSITE_REGISTRATION) },
                onEdit = { id -> navController.navigate("${Routes.OWNER_EDIT}/$id") }
            )
        }
        composable("${Routes.OWNER_EDIT}/{id}", arguments = idArgument) { entry ->
            val id = entry.arguments?.getString("id")
            val mine by vm.ownerCampsites.collectAsStateWithLifecycle()
            val campsite = mine.firstOrNull { it.id == id }
            if (campsite != null) {
                CampsiteEditScreen(
                    campsite = campsite,
                    onSave = { edited -> vm.update(edited) { navController.popBackStack() } },
                    onCancel = { navController.popBackStack() }
                )
            }
        }

        /* OTHER TABS
        -----------------------------------------------------------------------------------*/
        composable(BottomTab.Activity.name) { ActivityScreen(vm) }
        composable(BottomTab.Inbox.name) { InboxScreen() }
        composable(BottomTab.Profile.name) {
            ProfileScreen(
                onPublishCampsite = { navController.navigate(Routes.CAMPSITE_REGISTRATION) },
                onManageCampsites = { navController.navigate(Routes.OWNER_CAMPSITES) }
            )
        }
    }
}
