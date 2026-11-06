package com.example.rl_v1.ui.theme.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rl_v1.ui.theme.components.AppInputTextField
import com.example.rl_v1.ui.theme.components.LargeButtonPrimary
import com.example.rl_v1.ui.theme.components.ScreenHeader
import com.example.rl_v1.ui.theme.viewmodel.CampsiteViewModel
import java.time.LocalDate
import java.time.format.DateTimeParseException


@Composable
fun CampsiteRegistrationScreen(
    vm: CampsiteViewModel,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    BackHandler(onBack = onBack)

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var amenities by remember { mutableStateOf("") }
    var maxGuests by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var availableFrom by remember { mutableStateOf("") }
    var availableTo by remember { mutableStateOf("") }
    var rules by remember { mutableStateOf("") }
    var cancellationPolicy by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val message by vm.message.collectAsStateWithLifecycle()

    // Don't show a message left over from another screen.
    LaunchedEffect(Unit) { vm.clearMessage() }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item { ScreenHeader(text = "Campsite Details") }
        item {
            AppInputTextField(value = name, onValueChange = { name = it },
                label = "Name", placeholder = "Campsite name")
        }
        item {
            AppInputTextField(value = location, onValueChange = { location = it },
                label = "Location", placeholder = "Canmore, Alberta")
        }
        item {
            AppInputTextField(value = description, onValueChange = { description = it },
                label = "Description", placeholder = "What makes it special", singleLine = false)
        }
        item {
            AppInputTextField(value = amenities, onValueChange = { amenities = it },
                label = "Amenities and tags", placeholder = "Firepit, Cookhouse (separate with commas)")
        }
        item {
            AppInputTextField(value = maxGuests, onValueChange = { maxGuests = it },
                label = "Maximum guests", placeholder = "6",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        }
        item {
            AppInputTextField(value = price, onValueChange = { price = it },
                label = "Nightly price (CAD)", placeholder = "65.00",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        }
        item {
            AppInputTextField(value = availableFrom, onValueChange = { availableFrom = it },
                label = "Available from (optional)", placeholder = "2027-05-01")
        }
        item {
            AppInputTextField(value = availableTo, onValueChange = { availableTo = it },
                label = "Available to (optional)", placeholder = "2027-09-30")
        }
        item {
            AppInputTextField(value = rules, onValueChange = { rules = it },
                label = "Rules", placeholder = "Quiet hours 10 pm to 7 am", singleLine = false)
        }
        item {
            AppInputTextField(value = cancellationPolicy, onValueChange = { cancellationPolicy = it },
                label = "Cancellation policy", placeholder = "Free cancellation up to 7 days before",
                singleLine = false)
        }

        (error ?: message)?.let { text ->
            item { Text(text, color = Color.Red) }
        }

        item {
            LargeButtonPrimary("Publish Campsite", onClick = {
                val guests = maxGuests.trim().toIntOrNull()
                val nightly = price.trim().toDoubleOrNull()
                error = when {
                    name.isBlank() -> "Enter a campsite name"
                    location.isBlank() -> "Enter a location"
                    guests == null || guests <= 0 -> "Maximum guests must be a whole number above 0"
                    nightly == null || nightly < 0 -> "Price must be a number"
                    !isBlankOrIsoDate(availableFrom) -> "Available from must look like 2027-05-01"
                    !isBlankOrIsoDate(availableTo) -> "Available to must look like 2027-09-30"
                    else -> null
                }
                if (error == null && guests != null && nightly != null) {
                    vm.publish(
                        name = name,
                        location = location,
                        maxOccupancy = guests,
                        pricePerNight = nightly,
                        description = description,
                        amenities = amenities.split(","),
                        rules = rules,
                        cancellationPolicy = cancellationPolicy,
                        availableFrom = availableFrom,
                        availableTo = availableTo,
                        onDone = onDone
                    )
                }
            })
        }
    }
}

/** True when [text] is empty or a date such as "2027-05-01". */
private fun isBlankOrIsoDate(text: String): Boolean {
    if (text.isBlank()) return true
    return try {
        LocalDate.parse(text.trim())
        true
    } catch (e: DateTimeParseException) {
        false
    }
}
