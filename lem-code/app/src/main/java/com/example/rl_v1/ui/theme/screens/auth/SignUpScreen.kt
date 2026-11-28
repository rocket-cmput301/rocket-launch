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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
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
import com.example.rl_v1.ui.theme.theme.Forest
import com.example.rl_v1.ui.theme.theme.GreyGreen
import com.example.rl_v1.ui.theme.theme.Rlv1Theme

/**
 * Account creation: name, email, password and confirm password, plus Google sign-up.
 * Only checks the form; the callbacks decide what actually happens.
 */
@Composable
fun SignUpScreen(
    onSignUp: (name: String, email: String, password: String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onGoToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf(false) }

    val nameError = if (submitted && name.isBlank()) "Enter your name" else null
    val emailError = if (submitted) AuthValidation.emailError(email) else null
    val passwordError = if (submitted) AuthValidation.passwordError(password) else null
    val confirmError = if (submitted) AuthValidation.confirmError(password, confirm) else null

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
            text = "Create an account",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Forest
        )
        Text(
            text = "Sign up to reserve campsites and join lotteries.",
            style = MaterialTheme.typography.bodyLarge,
            color = GreyGreen
        )
        Spacer(Modifier.height(8.dp))

        Column {
            AppInputTextField(
                value = name,
                onValueChange = { name = it },
                label = "Name",
                placeholder = "Your full name",
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            FieldError(nameError)
        }

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
            placeholder = "At least ${AuthValidation.MIN_PASSWORD_LENGTH} characters",
            errorText = passwordError
        )

        AppPasswordTextField(
            value = confirm,
            onValueChange = { confirm = it },
            label = "Confirm password",
            placeholder = "Type it again",
            errorText = confirmError
        )

        LargeButtonPrimary(
            text = "Sign up",
            onClick = {
                submitted = true
                val valid = name.isNotBlank() &&
                    AuthValidation.emailError(email) == null &&
                    AuthValidation.passwordError(password) == null &&
                    AuthValidation.confirmError(password, confirm) == null
                if (valid) onSignUp(name.trim(), email.trim(), password)
            }
        )

        OrDivider()

        GoogleSignInButton(onClick = onGoogleSignIn, text = "Sign up with Google")

        Spacer(Modifier.height(8.dp))
        AuthSwitchRow(
            prompt = "Already have an account?",
            action = "Log in",
            onClick = onGoToLogin
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignUpScreenPreview() {
    Rlv1Theme(dynamicColor = false) {
        SignUpScreen(onSignUp = { _, _, _ -> }, onGoogleSignIn = {}, onGoToLogin = {})
    }
}
