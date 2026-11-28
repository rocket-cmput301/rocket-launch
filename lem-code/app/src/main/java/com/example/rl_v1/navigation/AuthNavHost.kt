package com.example.rl_v1.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rl_v1.ui.theme.screens.auth.DeactivatedAccountScreen
import com.example.rl_v1.ui.theme.screens.auth.RegistrationScreen
import com.example.rl_v1.ui.theme.screens.auth.WelcomeScreen

/**
 * Onboarding and account-entry navigation shown before the main app (Explore tab).
 * Starts on [Routes.WELCOME] and calls [onAuthenticated] once the user has an active profile.
 */
@Composable
fun AuthNavHost(
    onAuthenticated: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME,
        modifier = modifier
    ) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onNeedsRegistration = {
                    navController.navigate(Routes.REGISTRATION) {
                        launchSingleTop = true
                    }
                },
                onAccountDeactivated = {
                    navController.navigate(Routes.DEACTIVATED_ACCOUNT) {
                        launchSingleTop = true
                    }
                },
                onAuthenticated = onAuthenticated
            )
        }

        composable(Routes.REGISTRATION) {
            RegistrationScreen(
                onRegistrationComplete = onAuthenticated
            )
        }

        composable(Routes.DEACTIVATED_ACCOUNT) {
            DeactivatedAccountScreen(
                onReactivated = onAuthenticated
            )
        }
    }
}