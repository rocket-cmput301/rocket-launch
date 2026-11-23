package com.example.rl_v1.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rl_v1.ui.theme.screens.auth.LoginScreen
import com.example.rl_v1.ui.theme.screens.auth.SignUpScreen
import com.example.rl_v1.ui.theme.screens.auth.TempLoginScreen

///**
// * Login and sign-up screens, shown before the main app (no bottom bar).
// * Starts on Login. [onAuthenticated] is called once the user is "signed in".
// */
//@Composable
//fun AuthNavHost(
//    onAuthenticated: () -> Unit,
//    modifier: Modifier = Modifier,
//    navController: NavHostController = rememberNavController()
//) {
//    NavHost(
//        navController = navController,
//        startDestination = Routes.TEMP_LOGIN,
//        modifier = modifier
//    ) {
//        composable(Routes.LOGIN) {
//            LoginScreen(
//                onLogin = { _, _ ->
//                    // TODO: FirebaseAuth.signInWithEmailAndPassword(email, password),
//                    //  show errors on failure. Stubbed: any valid form logs in.
//                    onAuthenticated()
//                },
//                onGoogleSignIn = {
//                    // TODO: Credential Manager + GoogleIdTokenCredential, then
//                    //  FirebaseAuth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).
//                    onAuthenticated()
//                },
//                onGoToSignUp = {
//                    navController.navigate(Routes.SIGN_UP) { launchSingleTop = true }
//                },
//                onForgotPassword = {
//                    // TODO: FirebaseAuth.sendPasswordResetEmail(email)
//                }
//            )
//        }
//        composable(Routes.SIGN_UP) {
//            SignUpScreen(
//                onSignUp = { _, _, _ ->
//                    // TODO: FirebaseAuth.createUserWithEmailAndPassword(email, password)
//                    //  (or linkWithCredential to keep the current anonymous user's data),
//                    //  then save the name to the user's profile. Stubbed: any valid form signs up.
//                    onAuthenticated()
//                },
//                onGoogleSignIn = {
//                    // TODO: same Google flow as on the login screen.
//                    onAuthenticated()
//                },
//                onGoToLogin = {
//                    // Go back to Login instead of stacking a second copy of it.
//                    if (!navController.popBackStack(Routes.LOGIN, inclusive = false)) {
//                        navController.navigate(Routes.LOGIN) { launchSingleTop = true }
//                    }
//                }
//            )
//        }
//        composable(Routes.TEMP_LOGIN) {
//            TempLoginScreen(
//                onContinueToApp = {
//                    navController.navigate(Routes.LOGIN) { launchSingleTop = true }
//                }
//            )
//        }
//    }
//}

import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.rl_v1.model.SharedPrefsDeviceIdProvider
import com.example.rl_v1.repository.FirestoreUserRepository
import com.example.rl_v1.ui.theme.components.BottomTab
import com.example.rl_v1.ui.theme.screens.auth.TempSignUpScreen
import com.example.rl_v1.ui.theme.viewmodel.ensureFirebaseSession
import kotlinx.coroutines.launch

@Composable
fun AuthNavHost(
    onAuthenticated: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val deviceIdProvider = remember { SharedPrefsDeviceIdProvider(context) }
    val userRepo = remember { FirestoreUserRepository() }

    NavHost(
        navController = navController,
        startDestination = Routes.TEMP_SIGNUP,
        modifier = modifier
    ) {
        composable(Routes.TEMP_LOGIN) {
            TempLoginScreen(
                onContinueToApp = {
                    navController.navigate(BottomTab.Explore.name) { launchSingleTop = true }
                }
            )
        }
        composable(Routes.TEMP_SIGNUP) {
            TempSignUpScreen(
                onSignedUp = onAuthenticated,
                onGoToLogin = {
                    if (!navController.popBackStack(Routes.LOGIN, inclusive = false)) {
                        navController.navigate(Routes.TEMP_LOGIN) { launchSingleTop = true }
                    }
                }
            )
        }
    }
}
