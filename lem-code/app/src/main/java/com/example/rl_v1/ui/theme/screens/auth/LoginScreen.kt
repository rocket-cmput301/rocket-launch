package com.example.rl_v1.ui.theme.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rl_v1.ui.theme.components.AppInputTextField
import com.example.rl_v1.ui.theme.components.AppPasswordTextField
import com.example.rl_v1.ui.theme.components.AuthSwitchRow
import com.example.rl_v1.ui.theme.components.FieldError
import com.example.rl_v1.ui.theme.components.GoogleSignInButton
import com.example.rl_v1.ui.theme.components.LargeButtonPrimary
import com.example.rl_v1.ui.theme.components.OrDivider
import com.example.rl_v1.ui.theme.components.PrimaryClickableText
import com.example.rl_v1.ui.theme.theme.DarkGreen
import com.example.rl_v1.ui.theme.theme.Forest
import com.example.rl_v1.ui.theme.theme.GreyGreen
import com.example.rl_v1.ui.theme.theme.Rlv1Theme

/**
 * Email/password login with a "Continue with Google" option.
 * Only checks the form; the callbacks decide what actually happens.
 */
@Composable
fun LoginScreen(
    onLogin: (email: String, password: String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onGoToSignUp: () -> Unit,
    onForgotPassword: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf(false) }

    val emailError = if (submitted) AuthValidation.emailError(email) else null
    val passwordError = if (submitted && password.isEmpty()) "Enter your password" else null

    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Welcome back",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Forest
        )
        Text(
            text = "Log in to find and book your next campsite.",
            style = MaterialTheme.typography.bodyLarge,
            color = GreyGreen
        )
        Spacer(Modifier.height(8.dp))

        Column {
            AppInputTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "you@example.com",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            FieldError(emailError)
        }

        AppPasswordTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            placeholder = "Your password",
            errorText = passwordError
        )

        PrimaryClickableText(
            text = "Forgot password?",
            textColor = DarkGreen,
            onClick = onForgotPassword,
            modifier = Modifier.align(Alignment.End)
        )

        LargeButtonPrimary(
            text = "Log in",
            onClick = {
                submitted = true
                if (AuthValidation.emailError(email) == null && password.isNotEmpty()) {
                    onLogin(email.trim(), password)
                }
            }
        )

        OrDivider()

        GoogleSignInButton(onClick = onGoogleSignIn)

        Spacer(Modifier.height(8.dp))
        AuthSwitchRow(
            prompt = "Don't have an account?",
            action = "Sign up",
            onClick = onGoToSignUp
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun LoginScreenPreview() {
    Rlv1Theme(dynamicColor = false) {
        LoginScreen(onLogin = { _, _ -> }, onGoogleSignIn = {}, onGoToSignUp = {})
    }
}
