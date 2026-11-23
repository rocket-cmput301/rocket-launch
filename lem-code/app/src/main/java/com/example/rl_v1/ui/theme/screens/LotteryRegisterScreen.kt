package com.example.rl_v1.ui.theme.screens


import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rl_v1.model.DateAvailability
import com.example.rl_v1.ui.theme.components.AppChecklistBox
import com.example.rl_v1.ui.theme.components.AppInputTextField
import com.example.rl_v1.ui.theme.components.LargeButtonPrimary
import com.example.rl_v1.ui.theme.components.ScreenHeader
import com.example.rl_v1.ui.theme.screens.explore.DateRangePickerModal
import com.example.rl_v1.ui.theme.theme.Forest
import com.example.rl_v1.ui.theme.theme.LightGreen
import com.example.rl_v1.ui.theme.viewmodel.CampsiteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Creates the screen where a Campsite Owner customizes the Lottery settings to their liking for a selection of their campsites
 *
 * Based on Fig. 20 in Figma, covers the following User Stories: US 02.04.01 to 02.04.07
 *
 * @returns nothing
 */

@Composable
fun LotteryRegistrationScreen(
    vm: CampsiteViewModel,
    onBack: () -> Unit,
    onDone: () -> Unit
){
    BackHandler(onBack = onBack)

    //val campsites by vm.ownerCampsites.collectAsStateWithLifecycle()          //the actual line we want
    val placeholderOwnerId = "8bkEYENWDCWqz4mRcdTnWGeLmbv2"
    val campsites by vm.specificOwnerSite(placeholderOwnerId).collectAsStateWithLifecycle() //test fetch

    var selectedCampsites by remember { mutableStateOf(setOf<String>()) }
    val formatter = SimpleDateFormat("MM/dd/yyyy",Locale.getDefault())
    var registrationPeriod by remember { mutableStateOf(DateAvailability("")) }
    var bookingPeriod by remember { mutableStateOf(DateAvailability("")) }
    var numWinners by remember { mutableStateOf(value = "0") }
    var waitlistCapacity by remember { mutableStateOf("-1") }  //make it -1
    var error by remember {mutableStateOf<String?>(null)}

    val message by vm.message.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.clearMessage() }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {

        item{ ScreenHeader(text = "Lottery Creation") }                 //'Lottery Settings' is for updating an already existing Lottery
        if (campsites.isEmpty()) {
            item { Text("You haven't published any campsites yet.") }
        }
        else {
            item{
                Text("Note: Selecting multiple campsites will have the following settings apply to all of the campsites selected. If you wish for campsites to have different lottery settings, make individual lotteries.", fontSize = 12.sp)
                AppChecklistBox(label = "Campsites", items = campsites.map { it.name }, selectedItems = selectedCampsites, onSelectionChange = {
                    selectedCampsites = it
                })
            }
        }

        item{
            var openRegWindow by remember { mutableStateOf(false) }
            val interactionSource = remember { MutableInteractionSource() }
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(LightGreen)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {openRegWindow = true }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(
                    text = "Registration Period (Lottery Window)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Forest
                )
                Spacer(Modifier.height(4.dp))
                Text("${registrationPeriod.startDate} to ${registrationPeriod.endDate}")
            }
            if (openRegWindow){
                DateRangePickerModal(onDateRangeSelected = { valid, pair ->
                    if (valid){
                        val milsInADay = 86400000
                        registrationPeriod = DateAvailability(
                            startDate = formatter.format(Date(pair.first!! + milsInADay)),
                            endDate = formatter.format(Date(pair.second!! + milsInADay))
                            )
                    }
                    else{
                        registrationPeriod = DateAvailability(startDate = "INVALID", endDate = "INVALID")
                    }
                },
                    onDismiss = {openRegWindow = false}
                )
            }
        }

        item{
            var openBookWindow by remember { mutableStateOf(false) }
            val interactionSource = remember { MutableInteractionSource() }
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(LightGreen)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                       openBookWindow = true
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(
                    text = "Booking Period",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Forest
                )
                Spacer(Modifier.height(4.dp))
                Text("${bookingPeriod.startDate} to ${bookingPeriod.endDate}")
            }
            if (openBookWindow){
                DateRangePickerModal(onDateRangeSelected = { valid, pair ->
                    if (valid){
                        val milsInADay = 86400000
                        bookingPeriod = DateAvailability(
                            startDate = formatter.format(Date(pair.first!! + milsInADay)),
                            endDate = formatter.format(Date(pair.second!! + milsInADay))
                        )
                    }
                    else{
                        bookingPeriod = DateAvailability(startDate = "INVALID", endDate = "INVALID")
                    }
                },
                    onDismiss = {openBookWindow = false}
                )
            }
            Text("Booking must be after registration period.\nBooking and Registration periods must not overlap.", fontSize = 12.sp)
        }
        item{
            AppInputTextField(
                value = numWinners,
                onValueChange = { numWinners = it},
                label = "Number of Winners"
            )
        }
        item{
            AppInputTextField(
                value = waitlistCapacity,
                onValueChange = { waitlistCapacity = it},
                label = "Waitlist Capacity (-1 for no capacity limit)"
            )
            Text("Winners are drawn automatically when registration closes", fontSize = 12.sp)
        }

        (error ?: message).let { text ->
            item {
                if (text != null) {
                    Text(text, color = androidx.compose.ui.graphics.Color.Red)
                }
            }
        }

        item{
            LargeButtonPrimary(text = "Save Lottery", onClick = {
                //fetch corresponding campsite IDs of the Campsites chosen
                val selectedSitesObjects = campsites.filter { it.name in selectedCampsites }

                //validate the info
                error = when {
                    registrationPeriod.startDate.isBlank() || registrationPeriod.endDate.isBlank() || registrationPeriod.startDate == "INVALID" || registrationPeriod.endDate == "INVALID" -> "Enter a valid and complete registration date."
                    bookingPeriod.startDate.isBlank() || bookingPeriod.endDate.isBlank() || bookingPeriod.startDate == "INVALID" || bookingPeriod.endDate == "INVALID" -> "Enter a valid and complete booking date."
                    numWinners.isEmpty() || numWinners.toInt() <= 0 -> "Enter the number of winners a lottery can have (greater than 0)"
                    waitlistCapacity.isEmpty() || waitlistCapacity.toInt() < -1 -> "Enter the number of Users that can join the waitlist (or -1 if no limit)."
                    else -> null
                    /*also need to check for:
                    - registration and booking overlapping
                    - booking BEFORE registration
                     */
                }

                println("${selectedSitesObjects} and ${selectedSitesObjects.map {it.id}}")

                //if all passes, then publish to firebase
                if (error == null){
                    println("saved successfully")
                    vm.callPublishLottery(
                        campsiteIDs = selectedSitesObjects.map { it.id },
                        ownerID = placeholderOwnerId,
                        registrationPeriod = registrationPeriod,
                        bookingPeriod = bookingPeriod,
                        maxWinners = numWinners.toInt(),
                        capacity = if (waitlistCapacity.toInt() == -1) null else waitlistCapacity.toInt(),
                        onDone = onDone
                    )
                }
            })
        }
    }
}

