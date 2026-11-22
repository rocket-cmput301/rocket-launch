package com.example.rl_v1.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rl_v1.ui.theme.screens.auth.LoginScreen
import com.example.rl_v1.ui.theme.screens.auth.SignUpScreen

/**
 * Login and sign-up screens, shown before the main app (no bottom bar).
 * Starts on Login. [onAuthenticated] is called once the user is "signed in".
 */
@Composable
fun AuthNavHost(
    onAuthenticated: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        modifier = modifier
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLogin = { _, _ ->
                    // TODO: FirebaseAuth.signInWithEmailAndPassword(email, password),
                    //  show errors on failure. Stubbed: any valid form logs in.
                    onAuthenticated()
                },
                onGoogleSignIn = {
                    // TODO: Credential Manager + GoogleIdTokenCredential, then
                    //  FirebaseAuth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).
                    onAuthenticated()
                },
                onGoToSignUp = {
                    navController.navigate(Routes.SIGN_UP) { launchSingleTop = true }
                },
                onForgotPassword = {
                    // TODO: FirebaseAuth.sendPasswordResetEmail(email)
                }
            )
        }
        composable(Routes.SIGN_UP) {
            SignUpScreen(
                onSignUp = { _, _, _ ->
                    // TODO: FirebaseAuth.createUserWithEmailAndPassword(email, password)
                    //  (or linkWithCredential to keep the current anonymous user's data),
                    //  then save the name to the user's profile. Stubbed: any valid form signs up.
                    onAuthenticated()
                },
                onGoogleSignIn = {
                    // TODO: same Google flow as on the login screen.
                    onAuthenticated()
                },
                onGoToLogin = {
                    // Go back to Login instead of stacking a second copy of it.
                    if (!navController.popBackStack(Routes.LOGIN, inclusive = false)) {
                        navController.navigate(Routes.LOGIN) { launchSingleTop = true }
                    }
                }
            )
        }
    }
}
