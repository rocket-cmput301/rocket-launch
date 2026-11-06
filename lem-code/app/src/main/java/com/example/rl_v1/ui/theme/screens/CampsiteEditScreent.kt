package com.example.rl_v1.ui.theme.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.rl_v1.model.Campsite

/** Lets an owner change the details of one of their campsites. */
@Composable
fun CampsiteEditScreen(
    campsite: Campsite,
    onSave: (Campsite) -> Unit,
    onCancel: () -> Unit
) {
    BackHandler(onBack = onCancel)

    // Keyed by id so live updates to the campsite don't wipe what's being typed.
    var form by remember(campsite.id) { mutableStateOf(CampsiteForm.from(campsite)) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Edit campsite", style = MaterialTheme.typography.headlineSmall)

        FormField("Name", form.name) { form = form.copy(name = it) }
        FormField("Location", form.location) { form = form.copy(location = it) }
        FormField("Maximum occupancy", form.maxOccupancy, KeyboardType.Number) {
            form = form.copy(maxOccupancy = it)
        }
        FormField("Price per night", form.pricePerNight, KeyboardType.Decimal) {
            form = form.copy(pricePerNight = it)
        }
        FormField("Description", form.description, minLines = 3) {
            form = form.copy(description = it)
        }
        FormField("Amenities (separate with commas)", form.amenities) {
            form = form.copy(amenities = it)
        }
        FormField("Latitude (optional)", form.latitude, KeyboardType.Decimal) {
            form = form.copy(latitude = it)
        }
        FormField("Longitude (optional)", form.longitude, KeyboardType.Decimal) {
            form = form.copy(longitude = it)
        }
        FormField("Rules", form.rules, minLines = 3) { form = form.copy(rules = it) }
        FormField("Cancellation policy", form.cancellationPolicy, minLines = 3) {
            form = form.copy(cancellationPolicy = it)
        }

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                form.applyTo(campsite)
                    .onSuccess { error = null; onSave(it) }
                    .onFailure { error = it.message }
            }) { Text("Save") }
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = minLines == 1,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}