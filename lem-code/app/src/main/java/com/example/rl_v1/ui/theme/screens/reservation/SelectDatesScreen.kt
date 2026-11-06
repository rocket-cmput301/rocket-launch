package com.example.rl_v1.ui.theme.screens.reservation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.rl_v1.model.Campsite
import com.example.rl_v1.repository.stayNights
import com.example.rl_v1.ui.theme.components.AppInputTextField
import com.example.rl_v1.ui.theme.components.LargeButtonPrimary
import com.example.rl_v1.ui.theme.components.PrimaryTextBox
import com.example.rl_v1.ui.theme.components.ScreenHeader
import com.example.rl_v1.ui.theme.components.priceText
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale

/**
 * Dates and reservation review (Fig. 06): pick check-in and check-out,
 * the number of guests, and confirm. [message] shows booking progress or errors.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectDatesScreen(
    campsite: Campsite,
    message: String?,
    onBack: () -> Unit,
    onConfirm: (checkIn: String, checkOut: String, guests: Int) -> Unit
) {
    BackHandler(onBack = onBack)

    // The picker works in UTC midnight milliseconds.
    val todayUtc = remember {
        LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    }
    val pickerState = rememberDateRangePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis >= todayUtc
        }
    )
    var guests by remember { mutableStateOf("1") }

    val checkIn = pickerState.selectedStartDateMillis?.let(::toIsoDate)
    val checkOut = pickerState.selectedEndDateMillis?.let(::toIsoDate)
    val nights = if (checkIn != null && checkOut != null) {
        stayNights(checkIn, checkOut).getOrNull()
    } else null
    val guestCount = guests.trim().toIntOrNull()
    val guestsValid = guestCount != null && guestCount in 1..campsite.maxOccupancy

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("Back") }
        ScreenHeader("Select Dates")

        PrimaryTextBox(value = campsite.name, label = "CHOSEN CAMPSITE")

        DateRangePicker(
            state = pickerState,
            showModeToggle = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
        )

        AppInputTextField(
            value = guests,
            onValueChange = { guests = it },
            label = "Guests (up to ${campsite.maxOccupancy})",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        val summary = when {
            checkIn == null || checkOut == null -> "Pick a check-in and a check-out date"
            nights == null -> "Check-out must be after check-in (at most 30 nights)"
            else -> "$checkIn to $checkOut • ${nights.size} night" +
                    (if (nights.size == 1) "" else "s") +
                    " • $" + String.format(Locale.US, "%.2f", nights.size * campsite.pricePerNight)
        }
        PrimaryTextBox(value = summary, label = "YOUR RESERVATION • " + priceText(campsite.pricePerNight))

        message?.let { Text(it, color = Color.Red) }

        LargeButtonPrimary(
            text = "Confirm Reservation",
            enabled = nights != null && guestsValid,
            onClick = {
                if (checkIn != null && checkOut != null && guestCount != null) {
                    onConfirm(checkIn, checkOut, guestCount)
                }
            }
        )
    }
}

/** UTC midnight milliseconds from the picker to "2027-05-07". */
private fun toIsoDate(utcMillis: Long): String =
    Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate().toString()
