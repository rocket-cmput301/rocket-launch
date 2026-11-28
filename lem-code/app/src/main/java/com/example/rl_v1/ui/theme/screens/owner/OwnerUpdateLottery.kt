package com.example.rl_v1.ui.theme.screens.owner

import android.R
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
import com.example.rl_v1.model.Lottery
import com.example.rl_v1.ui.theme.components.AppChecklistBox
import com.example.rl_v1.ui.theme.components.AppInputTextField
import com.example.rl_v1.ui.theme.components.LargeButtonPrimary
import com.example.rl_v1.ui.theme.components.ScreenHeader
import com.example.rl_v1.ui.theme.screens.explore.DateRangePickerModal
import com.example.rl_v1.ui.theme.theme.Forest
import com.example.rl_v1.ui.theme.theme.LightGreen
import com.example.rl_v1.ui.theme.viewmodel.CampsiteViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/**
 *  A screen for an Owner to update the settings of an already existing lottery of theirs
 *
 *  Not based on a current Figma figure, but rather a varied copy of Fig. 20
 *  Not just reusing LotteryRegistrationScreen because that is hard coded to publishing a Lottery rather than updating an already existing one
 */

@Composable
fun OwnerLotteryUpdateScreen(
    vm: CampsiteViewModel,
    targetLottery: Lottery,
    onBack: () -> Unit,
    onDone: () -> Unit,
){
    BackHandler(onBack = onBack)

    //val campsites by vm.ownerCampsites.collectAsStateWithLifecycle()          //the actual line we want
    val placeholderOwnerId = "8bkEYENWDCWqz4mRcdTnWGeLmbv2"
    val campsites by vm.specificOwnerSite(placeholderOwnerId).collectAsStateWithLifecycle() //test fetch


    var selectedCampsites by remember { mutableStateOf(targetLottery.campsiteIds.toSet()) }
    var registrationPeriod by remember { mutableStateOf(targetLottery.registrationPeriod) }
    var bookingPeriod by remember { mutableStateOf(targetLottery.bookingPeriod) }
    var numWinners by remember { mutableStateOf(targetLottery.maxWinners.toString()) }
    var waitlistCapacity by remember { mutableStateOf(targetLottery.capacity.toString()) }

    val formatter = SimpleDateFormat("MM/dd/yyyy",Locale.getDefault())
    var regPeriodValid by remember { mutableStateOf(false) }
    val message by vm.message.collectAsStateWithLifecycle()
    var error by remember {mutableStateOf<String?>(null)}

    LaunchedEffect(Unit) { vm.clearMessage() }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {

        item { ScreenHeader("Lottery Updating") }

        //don't have to null check for empty campsite since this screen is only accessible for existing lotteries -> implies existing campsites
        item{
            val selectedCampsiteNames = campsites       //fetch names for display purposes
                .filter { it.id in selectedCampsites }
                .map { it.name }
                .toSet()

            AppChecklistBox(
                label = "Add/Drop Campsites in Lottery",
                items = campsites.map { it.name },

                selectedItems = selectedCampsiteNames,      //display campsite names
                onSelectionChange = { selectedNames ->      //dynamically update campsite IDs
                    selectedCampsites = campsites
                        .filter { it.name in selectedNames }
                        .map { it.id }
                        .toMutableSet()
                    println(selectedCampsites)
                }
            )
        }

        item{
            var openRegWindow by remember { mutableStateOf(false) }
            val interactionSource = remember { MutableInteractionSource() }
            Column(     //HARD CODED COLUMN FOR CLICKABILITY INSTEAD OF PRE-MADE UI ASSET
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(LightGreen)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {openRegWindow = true }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ){
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
                DateRangePickerModal(
                    minDate = LocalDate.now(),
                    maxDate = LocalDate.now().plusYears(1),
                    onDateRangeSelected = { valid, pair ->
                        if (valid){
                            val milsInADay = 86400000
                            registrationPeriod = DateAvailability(
                                startDate = formatter.format(Date(pair.first!! + milsInADay)),
                                endDate = formatter.format(Date(pair.second!! + milsInADay))
                            )
                            regPeriodValid = true
                        }
                        else{
                            registrationPeriod = DateAvailability(startDate = "INVALID", endDate = "INVALID")
                        }
                    },
                    onDismiss = {openRegWindow = false}
                )
            }
        }
        if (regPeriodValid){
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
                if (openBookWindow) {
                    val restrictionFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
                    DateRangePickerModal(
                        minDate = LocalDate.parse(registrationPeriod.endDate,restrictionFormatter),        //restrict Booking Period to start ON or after the Registration Period
                        maxDate = LocalDate.now().plusYears(1),
                        onDateRangeSelected = { valid, pair ->
                            if (valid) {
                                val milsInADay = 86400000
                                bookingPeriod = DateAvailability(
                                    startDate = formatter.format(Date(pair.first!! + milsInADay)),  //added because displayed date is 1 day behind what is actually selected
                                    endDate = formatter.format(Date(pair.second!! + milsInADay))
                                )
                            }
                        },
                        onDismiss = {
                            openBookWindow = false
                        }
                    )
                }
                Text("Booking must be after registration period. \nBooking and Registration periods must not overlap. (Unless Booking starts on the same day Registration Ends).", fontSize = 12.sp)
            }
        }
        else{
            item{
                Text("Please Enter a Registration Period before a Booking Period.")
            }
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
                val selectedSitesObjects = campsites.filter { it.id in selectedCampsites }
                println("sel: ${selectedSitesObjects}")

                //validate the new info
                error = when {
                    registrationPeriod.startDate.isBlank() || registrationPeriod.endDate.isBlank() || registrationPeriod.startDate == "INVALID" || registrationPeriod.endDate == "INVALID" -> "Enter a valid and complete registration date."
                    bookingPeriod.startDate.isBlank() || bookingPeriod.endDate.isBlank() || bookingPeriod.startDate == "INVALID" || bookingPeriod.endDate == "INVALID" -> "Enter a valid and complete booking date."
                    numWinners.isEmpty() || numWinners.toInt() <= 0 -> "Enter the number of winners a lottery can have (greater than 0)"
                    waitlistCapacity.isEmpty() || waitlistCapacity.toInt() < -1 -> "Enter the number of Users that can join the waitlist (or -1 if no limit)."
                    else -> null
                }

                //if all passes, UPDATE existing lottery in firebase
                if (error == null){
                    vm.callUpdateLottery(
                        lotteryID = targetLottery.id,
                        newCampsiteIDs = selectedSitesObjects.map { it.id },
                        ownerID = placeholderOwnerId,
                        newRegistrationPeriod = registrationPeriod,
                        newBookingPeriod = bookingPeriod,
                        newMaxWinners = numWinners.toInt(),
                        newCapacity = if (waitlistCapacity.toInt() == -1) null else waitlistCapacity.toInt()
                    )
                }

            })
        }
    }




}