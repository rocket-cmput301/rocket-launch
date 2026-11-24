package com.example.rl_v1.ui.theme.screens.reservation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.ui.Alignment
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
import androidx.compose.runtime.key
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
import com.example.rl_v1.ui.theme.components.DropdownBox
import com.example.rl_v1.ui.theme.components.LargeButtonPrimary
import com.example.rl_v1.ui.theme.components.PrimaryTextBox
import com.example.rl_v1.ui.theme.components.ScreenHeader
import com.example.rl_v1.ui.theme.components.priceText
import kotlinx.coroutines.selects.select
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Dates and reservation review (Fig. 06): pick check-in and check-out,
 * the number of guests, and confirm. [message] shows booking progress or errors.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectDatesScreen(
    campsites: List<Campsite>,
    message: String?,
    onBack: () -> Unit,
    onConfirm: (selectedSite: Campsite, checkIn: String, checkOut: String, guests: Int) -> Unit
) {
    BackHandler(onBack = onBack)

    var selectedCampsite by remember (campsites) { mutableStateOf(campsites.firstOrNull()) }        //initialize selected campsite to the first one first


    key(selectedCampsite?.id){      //change settings according to selected campsite

        // The picker works in UTC midnight milliseconds.
        val todayUtc = remember {
            LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }

        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        val availableFrom = LocalDate.parse(selectedCampsite!!.availability!!.startDate, formatter)
        val availableTo = LocalDate.parse(selectedCampsite!!.availability!!.endDate, formatter)


        val pickerState = rememberDateRangePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val date = Instant.ofEpochMilli(utcTimeMillis)
                        .atZone(ZoneOffset.UTC)
                        .toLocalDate()
                    return utcTimeMillis >= todayUtc &&
                            !date.isBefore(availableFrom) &&
                            !date.isAfter(availableTo)
                }

                override fun isSelectableYear(year: Int): Boolean {
                    return year in availableFrom.year..availableTo.year
                }
            }
        )

        var guests by remember { mutableStateOf("1") }
        var agreedToRules by remember { mutableStateOf(false) }

        val checkIn = pickerState.selectedStartDateMillis?.let(::toIsoDate)
        val checkOut = pickerState.selectedEndDateMillis?.let(::toIsoDate)
        val nights = if (checkIn != null && checkOut != null) {
            stayNights(checkIn, checkOut).getOrNull()
        } else null
        val guestCount = guests.trim().toIntOrNull()
        //val campsite = null

        val guestsValid = guestCount != null && guestCount in 1..selectedCampsite!!.maxOccupancy

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

            DropdownBox(label = "CHOOSE CAMPSITE ↓", value = selectedCampsite!!.name ,items = campsites.map { it.name }, onItemSelected = { selectedName ->     //original header modified for multiple campsites (US 01.04.02)
                selectedCampsite = campsites.find { it.name == selectedName }
            })

            Text("Note: You can only choose one campsite to make a reservation for (?)")      //ask about this one

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
                label = "Guests (up to ${selectedCampsite!!.maxOccupancy})",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            val summary = when {
                checkIn == null || checkOut == null -> "Pick a check-in and a check-out date"
                nights == null -> "Check-out must be after check-in (at most 30 nights)"
                else -> "$checkIn to $checkOut • ${nights.size} night" +
                        (if (nights.size == 1) "" else "s") +
                        " • $" + String.format(Locale.US, "%.2f", nights.size * selectedCampsite!!.pricePerNight)
            }
            PrimaryTextBox(value = summary, label = "YOUR RESERVATION • " + priceText(selectedCampsite!!.pricePerNight))

            // Show the campsite's rules and policies so the entrant can read them before booking.
            PrimaryTextBox(value = rulesToShow(selectedCampsite!!), label = "CAMPSITE RULES")
            PrimaryTextBox(value = policyToShow(selectedCampsite!!), label = "CANCELLATION POLICY")

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = agreedToRules,
                    onCheckedChange = { agreedToRules = it }
                )
                Text("I have read and agree to the campsite rules and policies")
            }

            message?.let { Text(it, color = Color.Red) }

            LargeButtonPrimary(
                text = "Confirm Reservation",
                enabled = canConfirmReservation(nights != null, guestsValid, agreedToRules),
                onClick = {
                    if (checkIn != null && checkOut != null && guestCount != null) {
                        onConfirm(selectedCampsite!!,checkIn, checkOut, guestCount)
                    }
                }
            )
        }
    }
}

/** The campsite's rules, or a short note if the owner didn't write any. */
fun rulesToShow(campsite: Campsite): String {
    if (campsite.rules.isBlank()) {
        return "This campsite has no extra rules."
    }
    return campsite.rules
}

/** The campsite's cancellation policy, or a short note if the owner didn't write one. */
fun policyToShow(campsite: Campsite): String {
    if (campsite.cancellationPolicy.isBlank()) {
        return "This campsite has no cancellation policy."
    }
    return campsite.cancellationPolicy
}

/** The reservation can only be confirmed once the dates and guests are valid and the rules are agreed to. */
fun canConfirmReservation(datesValid: Boolean, guestsValid: Boolean, agreedToRules: Boolean): Boolean {
    return datesValid && guestsValid && agreedToRules
}

/** UTC midnight milliseconds from the picker to "2027-05-07". */
private fun toIsoDate(utcMillis: Long): String =
    Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate().toString()
