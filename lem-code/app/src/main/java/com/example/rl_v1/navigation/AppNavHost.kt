package com.example.rl_v1.navigation

import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.rl_v1.model.UserRole
import com.example.rl_v1.model.UserStatus
import com.example.rl_v1.ui.theme.components.BottomTab
import com.example.rl_v1.ui.theme.screens.CampsiteEditScreen
import com.example.rl_v1.ui.theme.screens.CampsiteRegistrationScreen
import com.example.rl_v1.ui.theme.screens.activity.ActivityScreen
import com.example.rl_v1.ui.theme.screens.admin.AdminScreen
import com.example.rl_v1.ui.theme.screens.explore.CampsiteDetailsScreen
import com.example.rl_v1.ui.theme.screens.explore.CampsiteSelectScreen
import com.example.rl_v1.ui.theme.screens.explore.ExploreScreen
import com.example.rl_v1.ui.theme.screens.explore.MakeFilterScreen
import com.example.rl_v1.ui.theme.screens.inbox.InboxScreen
import com.example.rl_v1.ui.theme.screens.owner.OwnerCampsitesScreen
import com.example.rl_v1.ui.theme.screens.profile.AccessibilityScreen
import com.example.rl_v1.ui.theme.screens.profile.PersonalInfo
import com.example.rl_v1.ui.theme.screens.profile.PersonalInformationScreen
import com.example.rl_v1.ui.theme.screens.profile.ProfilePictureScreen
import com.example.rl_v1.ui.theme.screens.profile.ProfileScreen
import com.example.rl_v1.ui.theme.screens.reservation.ReservationConfirmedScreen
import com.example.rl_v1.ui.theme.screens.reservation.SelectDatesScreen
import com.example.rl_v1.ui.theme.viewmodel.CampsiteViewModel
import com.example.rl_v1.ui.theme.viewmodel.UserViewModel

private val idArgument = listOf(navArgument("id") { type = NavType.StringType })

/** All the app's screens and how to move between them. Starts on the Explore tab. */
@Composable
fun AppNavHost(
    navController: NavHostController,
    onSignedOutToWelcome: () -> Unit = {},
    modifier: Modifier = Modifier,
    vm: CampsiteViewModel,
    userViewModel: UserViewModel = viewModel()
) {
    val userUiState by userViewModel.uiState.collectAsStateWithLifecycle()
    val currentProfile = userUiState.profile
    val isActiveUser = currentProfile?.status == UserStatus.ACTIVE
    val isAdmin = isActiveUser && currentProfile?.role?.contains(UserRole.ADMIN) == true
    val currentPersonalInfo = remember(
        currentProfile?.name,
        currentProfile?.email,
        currentProfile?.phone
    ) {
        PersonalInfo(
            name = currentProfile?.name.orEmpty(),
            email = currentProfile?.email.orEmpty(),
            phone = currentProfile?.phone.orEmpty()
        )
    }

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
            enterTransition = { slideInVertically { it } },
            exitTransition = { fadeOut() },
            popExitTransition = { slideOutVertically { it } }
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
                    onBook = { navController.navigate("${Routes.SELECT_DATES}/${campsite.id}") },
                    onJoin = {
                        vm.callJoinLottery(campID = campsite.id)
                    }
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

        /* ADMIN
        -----------------------------------------------------------------------------------*/
        composable(BottomTab.Admin.name) {
            LaunchedEffect(isAdmin, userUiState.isLoading) {
                if (!userUiState.isLoading && !isAdmin) {
                    navController.navigate(BottomTab.Explore.name) {
                        popUpTo(BottomTab.Explore.name) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            }

            if (isAdmin) {
                AdminScreen(
                    onManageProfiles = { navController.navigate(Routes.ADMIN_PROFILES) }
                )
            }
        }

        /* OTHER TABS
        -----------------------------------------------------------------------------------*/
        composable(BottomTab.Activity.name) { ActivityScreen(vm) }
        composable(BottomTab.Inbox.name) { InboxScreen() }
        composable(BottomTab.Profile.name) {
            ProfileScreen(
                profile = currentProfile,
                onPublishCampsite = { navController.navigate(Routes.CAMPSITE_REGISTRATION) },
                onManageCampsites = { navController.navigate(Routes.OWNER_CAMPSITES) },
                onEditProfilePicture = { navController.navigate(Routes.PROFILE_PICTURE) },
                onEditPersonalInformation = { navController.navigate(Routes.PERSONAL_INFORMATION) },
                onEditAccessibility = { navController.navigate(Routes.ACCESSIBILITY) },
                onSaveProfile = {
                    // Optional if individual edits already save immediately
                },
                onDeleteProfile = {
                    userViewModel.setProfileDeleted(deleted = true) { result ->
                        result.onSuccess {
                            onSignedOutToWelcome()
                        }
                    }
                }
            )
        }
        composable(Routes.PROFILE_PICTURE) {
            ProfilePictureScreen(
                displayName = currentProfile?.name.orEmpty(),
                currentPhotoUrl = currentProfile?.photoUrl,
                onBack = { navController.popBackStack() },
                onSave = { newPhotoUrl ->
                    userViewModel.updateProfileImage(newPhotoUrl) { result ->
                        result.onSuccess {
                            navController.popBackStack()
                        }
                    }
                }
            )
        }
        composable(Routes.PERSONAL_INFORMATION) {
            PersonalInformationScreen(
                initialInfo = currentPersonalInfo,
                onBack = { navController.popBackStack() },
                onSave = { updatedInfo ->
                    userViewModel.updateContactInfo(
                        name = updatedInfo.name,
                        email = updatedInfo.email,
                        phone = updatedInfo.phone
                    ) { result ->
                        result.onSuccess {
                            navController.popBackStack()
                        }
                    }
                }
            )
        }
        composable(Routes.ACCESSIBILITY) {
            AccessibilityScreen(
                notificationsEnabled = currentProfile?.notificationsEnabled ?: true,
                onNotificationsChange = { enabled ->
                    userViewModel.setNotificationsEnabled(enabled)
                },
                onBack = {
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}