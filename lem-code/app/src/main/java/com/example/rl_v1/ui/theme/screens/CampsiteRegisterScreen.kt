package com.example.rl_v1.ui.theme.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rl_v1.ui.theme.CampsiteViewModel
import com.example.rl_v1.ui.theme.comp.AppInputTextField
import com.example.rl_v1.ui.theme.comp.AppBottomBar
import com.example.rl_v1.ui.theme.comp.ScreenHeader
import com.example.rl_v1.ui.theme.comp.LargeButtonPrimary
import com.example.rl_v1.ui.theme.comp.LargeButtonSecondary


@Composable
fun CampsiteRegistrationScreen(vm: CampsiteViewModel = viewModel()){

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var amenities by remember { mutableStateOf("") }
    var guestsAndPrice by remember { mutableStateOf("") }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize()) {
        item {
            ScreenHeader(text = "Campsite Details")
        }
        item {
            AppInputTextField(value = name, onValueChange = { name = it }, label = "Name", placeholder = "Campsite name")
        }
        item {
            AppInputTextField(value = location, onValueChange = { location = it }, label = "Location", placeholder = "Location")
        }
        item {
            AppInputTextField(value = amenities, onValueChange = { amenities = it }, label = "Amenities and tags", placeholder = "Amenities")
        }
        item {
            AppInputTextField(value = guestsAndPrice, onValueChange = { guestsAndPrice = it }, label = "Maximum guests / nightly price", placeholder = "Price")
        }
        item {
            LargeButtonPrimary("Publish Campsite", onClick = {Unit})
        }

    }
}

@Preview(showBackground = true)
@Composable
fun CampsiteRegistrationScreenPreview() {
    CampsiteRegistrationScreen()
}