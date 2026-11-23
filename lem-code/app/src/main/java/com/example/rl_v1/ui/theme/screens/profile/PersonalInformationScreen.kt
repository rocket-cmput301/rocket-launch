package com.example.rl_v1.ui.theme.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rl_v1.ui.theme.components.AppInputTextField
import com.example.rl_v1.ui.theme.components.FieldError
import com.example.rl_v1.ui.theme.components.LargeButtonPrimary
import com.example.rl_v1.ui.theme.components.LargeButtonSecondary
import com.example.rl_v1.ui.theme.components.ScreenHeader
import com.example.rl_v1.ui.theme.screens.auth.AuthValidation
import com.example.rl_v1.ui.theme.theme.Cream
import com.example.rl_v1.ui.theme.theme.Forest
import com.example.rl_v1.ui.theme.theme.GreyGreen
import com.example.rl_v1.ui.theme.theme.Rlv1Theme

/** What the Personal Information screen edits. [phone] is empty when the user has none. */
data class PersonalInfo(
    val name: String = "",
    val email: String = "",
    val phone: String = ""
)

/** Checks for the Personal Information form. Each returns an error message, or null if OK. */
internal object PersonalInfoValidation {
    const val MAX_NAME_LENGTH = 50
    private const val MIN_PHONE_DIGITS = 10
    private const val MAX_PHONE_DIGITS = 15 // E.164 maximum

    // Digits plus the usual separators, with an optional leading "+".
    private val PHONE_CHARS = Regex("^\\+?[0-9 ()\\-.]+$")

    fun nameError(name: String): String? = when {
        name.isBlank() -> "Enter your name"
        name.trim().length > MAX_NAME_LENGTH -> "Name must be $MAX_NAME_LENGTH characters or fewer"
        else -> null
    }

    // Same rules as sign-up, so a saved email is always one the user could log in with.
    fun emailError(email: String): String? = AuthValidation.emailError(email)

    /** Phone is optional: blank is fine, anything else must look like a real number. */
    fun phoneError(phone: String): String? {
        val trimmed = phone.trim()
        if (trimmed.isEmpty()) return null
        val digits = trimmed.count { it.isDigit() }
        return when {
            !PHONE_CHARS.matches(trimmed) -> "Use only numbers, spaces, ( ) - or +"
            digits < MIN_PHONE_DIGITS || digits > MAX_PHONE_DIGITS -> "Enter a valid phone number"
            else -> null
        }
    }
}

/**
 * Lets the user change their name and email, and add, change or remove an optional phone number.
 *
 * Frontend only: loading and saving are stubbed with TODOs. Save is enabled once something has
 * changed; errors are shown after the first Save tap so the user isn't nagged while typing.
 *
 * @param initialInfo the user's current details, used to fill the form.
 * @param onBack leave without saving (back arrow and Cancel).
 * @param onSave called with the trimmed, validated details.
 */
@Composable
fun PersonalInformationScreen(
    initialInfo: PersonalInfo = PersonalInfo(name = "Sample Camper", email = "camper@example.com"),
    onBack: () -> Unit = {},
    onSave: (PersonalInfo) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // TODO: load initialInfo from the signed-in user (FirebaseAuth / users collection) in a ViewModel.
    var name by rememberSaveable(initialInfo) { mutableStateOf(initialInfo.name) }
    var email by rememberSaveable(initialInfo) { mutableStateOf(initialInfo.email) }
    var phone by rememberSaveable(initialInfo) { mutableStateOf(initialInfo.phone) }
    var showErrors by rememberSaveable { mutableStateOf(false) }

    val nameError = PersonalInfoValidation.nameError(name)
    val emailError = PersonalInfoValidation.emailError(email)
    val phoneError = PersonalInfoValidation.phoneError(phone)

    val current = PersonalInfo(name = name.trim(), email = email.trim(), phone = phone.trim())
    val hasChanges = current != initialInfo.copy(
        name = initialInfo.name.trim(),
        email = initialInfo.email.trim(),
        phone = initialInfo.phone.trim()
    )
    val emailChanged = current.email != initialInfo.email.trim()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Forest
                )
            }
            Spacer(Modifier.width(4.dp))
            ScreenHeader("Personal information")
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = "Hosts see your name on reservations. Your email and phone are only used to contact you about your trips.",
            style = MaterialTheme.typography.bodyMedium,
            color = GreyGreen,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column {
                AppInputTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Full name",
                    placeholder = "Your name",
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )
                FieldError(if (showErrors) nameError else null)
            }

            Column {
                AppInputTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    placeholder = "you@example.com",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )
                FieldError(if (showErrors) emailError else null)
            }

            Column {
                AppInputTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone number (optional)",
                    placeholder = "+1 780 555 0123",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    )
                )
                FieldError(if (showErrors) phoneError else null)
            }
        }

        if (emailChanged) {
            Spacer(Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Cream,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "Changing your email",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Forest
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "You'll use the new email to log in. We'll send it a link to confirm the change.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GreyGreen
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        LargeButtonPrimary(
            text = "Save changes",
            enabled = hasChanges,
            onClick = {
                showErrors = true
                if (nameError == null && emailError == null && phoneError == null) {
                    // TODO: save name/phone to the user's Firestore document, and if the email
                    //  changed call FirebaseUser.verifyBeforeUpdateEmail (may need re-authentication).
                    onSave(current)
                }
            }
        )
        Spacer(Modifier.height(12.dp))
        LargeButtonSecondary(text = "Cancel", onClick = onBack)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "Current details")
@Composable
private fun PersonalInformationScreenPreview() {
    Rlv1Theme(dynamicColor = false) {
        PersonalInformationScreen()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "With phone number")
@Composable
private fun PersonalInformationScreenWithPhonePreview() {
    Rlv1Theme(dynamicColor = false) {
        PersonalInformationScreen(
            initialInfo = PersonalInfo(
                name = "Sample Camper",
                email = "camper@example.com",
                phone = "+1 780 555 0123"
            )
        )
    }
}
