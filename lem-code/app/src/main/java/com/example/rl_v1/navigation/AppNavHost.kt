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
import com.example.rl_v1.ui.theme.screens.LotteryRegistrationScreen
import com.example.rl_v1.ui.theme.screens.activity.ActivityScreen
import com.example.rl_v1.ui.theme.screens.explore.CampsiteDetailsScreen
import com.example.rl_v1.ui.theme.screens.explore.CampsiteSelectScreen
import com.example.rl_v1.ui.theme.screens.explore.ExploreScreen
import com.example.rl_v1.ui.theme.screens.explore.MakeFilterScreen
import com.example.rl_v1.ui.theme.screens.inbox.InboxScreen
import com.example.rl_v1.ui.theme.screens.owner.OwnerLotteryUpdateScreen
import com.example.rl_v1.ui.theme.screens.owner.OwnerCampsitesScreen
import com.example.rl_v1.ui.theme.screens.owner.OwnerLotteryListScreen
import com.example.rl_v1.ui.theme.screens.owner.OwnerSelectedLotteryScreen
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
                    onJoin = {
                        vm.callJoinLottery(campID = campsite.id)    //the Real Lottery Repo
                        //navController.navigate(BottomTab.Activity.name)         // commented for now since a bug arises where 'Explore' in navbar doesn't work if this gets called
                    }
                )
            }
        }

        /* RESERVATIONS
        -----------------------------------------------------------------------------------*/
        composable("${Routes.SELECT_DATES}/{id}", arguments = idArgument) { entry ->        //whole block modified for Multiple-Campsite Lotteries
            val lotID = entry.arguments?.getString("id")
            val all by vm.allCampsites.collectAsStateWithLifecycle()
            val message by vm.message.collectAsStateWithLifecycle()

            val selectedLotteries by vm.userWonLotteries.collectAsStateWithLifecycle()
            val correspondingLottery = selectedLotteries.find {it.id == lotID}

            val campsites = all.filter { correspondingLottery?.campsiteIds?.contains(it.id) == true  }

            LaunchedEffect(Unit) { vm.clearMessage() }
            SelectDatesScreen(
                campsites = campsites,
                message = message,
                onBack = { navController.popBackStack() },
                onConfirm = {selectedSite, checkIn, checkOut, guests ->
                    vm.book(selectedSite, checkIn, checkOut, guests) { reservation ->
                        navController.navigate("${Routes.RESERVATION_CONFIRMED}/${reservation.id}") {
                            // Back from the confirmation shouldn't reopen the date picker.
                            popUpTo("${Routes.SELECT_DATES}/{id}") { inclusive = true }
                        }
                    }
                }
            )
        }
        composable("${Routes.RESERVATION_CONFIRMED}/{id}", arguments = idArgument) { entry ->
            val id = entry.arguments?.getString("id")
            val reservations by vm.entrantReservations.collectAsStateWithLifecycle()
            val all by vm.allCampsites.collectAsStateWithLifecycle()
            val message by vm.message.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) { vm.clearMessage() }
            val reservation = reservations.firstOrNull { it.id == id }
            val campsiteName = all.firstOrNull { it.id == reservation?.campsiteId }?.name ?: "Campsite"

            ReservationConfirmedScreen(
                reservation = reservation,
                campsiteName = campsiteName,
                canCancel = reservation != null && vm.canCancel(reservation),
                onCancel = { reservation?.let { vm.cancel(it) } },
                message = message,
                onDone = {
                    // opened from the Activity list: go back there; right after booking: go to Explore
                    if (navController.previousBackStackEntry?.destination?.route == BottomTab.Activity.name) {
                        navController.popBackStack()
                    } else {
                        navController.popBackStack(BottomTab.Explore.name, inclusive = false)
                    }
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

        composable(Routes.LOTTERY_REGISTER) {
            LotteryRegistrationScreen(
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
                onEdit = { id -> navController.navigate("${Routes.OWNER_EDIT}/$id") },
                onCreateLottery = { navController.navigate(Routes.LOTTERY_REGISTER)},
                onViewWaitlist = { lotId -> navController.navigate("${Routes.LOTTERY_WAITLIST}/$lotId") },
                onUpdate = { lotId -> navController.navigate("${Routes.LOTTERY_UPDATE}/$lotId")},
                onViewDetails = {lotId -> navController.navigate("${Routes.LOTTERY_DETAILS}/$lotId") }
            )
        }
        composable("${Routes.OWNER_EDIT}/{id}", arguments = idArgument) { entry ->      //campsite edit
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

        composable("${Routes.LOTTERY_WAITLIST}/{id}", arguments = idArgument) { lottery ->
            val id = lottery.arguments?.getString("id")
            val mine by vm.ownerOwnedLotteries.collectAsStateWithLifecycle()
            val lottery = mine.firstOrNull { it.id == id }

            if (lottery != null) {
                OwnerLotteryListScreen(
                    vm = vm,
                    lottery = lottery,
                    onBack = { navController.popBackStack() },
                    onSend = {
                        //TODO: send announcement (US 02.10.01)
                    }
                )
            }
        }

        composable("${Routes.LOTTERY_DETAILS}/{id}", arguments = idArgument){ lottery ->
            val id = lottery.arguments?.getString("id")
            val mine by vm.ownerOwnedLotteries.collectAsStateWithLifecycle()
            val lotteryFound = mine.firstOrNull { it.id == id }

            if (lotteryFound != null) {
                OwnerSelectedLotteryScreen(
                    vm = vm,
                    lottery = lotteryFound,
                    onBack = { navController.popBackStack() },
                    onUpdate = { lotId -> navController.navigate("${Routes.LOTTERY_UPDATE}/$lotId")},
                    onViewWaitlist = {lotId -> navController.navigate("${Routes.LOTTERY_WAITLIST}/$lotId")},
                    onSend = {
                        //TODO: send announcement (US 02.10.01)
                    }
                )
            }
        }

        composable("${Routes.LOTTERY_UPDATE}/{id}", arguments = idArgument){ lottery ->
            val id = lottery.arguments?.getString("id")
            val mine by vm.ownerOwnedLotteries.collectAsStateWithLifecycle()
            val lotteryFound = mine.firstOrNull { it.id == id }

            if (lotteryFound != null){
                OwnerLotteryUpdateScreen(
                    vm = vm,
                    targetLottery = lotteryFound,
                    onBack = { navController.popBackStack() },
                    onDone = { navController.popBackStack() },  //double check
                )
            }

        }


        /* OTHER TABS
        -----------------------------------------------------------------------------------*/
        composable(BottomTab.Activity.name) {                                       //whole block modified for multi-campsite lotteries
            val allLots = vm.userWonLotteries.collectAsStateWithLifecycle()
            ActivityScreen(
                vm,
                onBook = { lotteryId ->
                    val correspondingLottery = allLots.value.find { it.id == lotteryId }        //should only ever be 1 lottery; said 1 lottery can hold many campsites though
                    navController.navigate("${Routes.SELECT_DATES}/${correspondingLottery?.id}")
                },
                onOpenReservation = { id -> navController.navigate("${Routes.RESERVATION_CONFIRMED}/$id") }
            )

        }
        composable(BottomTab.Inbox.name) { InboxScreen(vm) }
        composable(BottomTab.Profile.name) {
            ProfileScreen(
                onPublishCampsite = { navController.navigate(Routes.CAMPSITE_REGISTRATION) },
                onManageCampsites = { navController.navigate(Routes.OWNER_CAMPSITES) },
            )
        }
    }
}
