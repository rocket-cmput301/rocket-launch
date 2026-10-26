/**
 *  Contains Composable Functions to provide a screen and interactions for a user to select their campsite filter preferences
 *
 *  Filter selections are returned as a tuple of (distance: Int, dates: Pair<String,String>, guestNumber: Int, preferences: ArrayList<String> and a boolean to close the filter screen)
 *  Doesn't have a corresponding test class because this isn't a class file, just a bunch of functions for providing a filter
 */

package com.example.rl_v1.ui.theme

import android.R
import android.renderscript.ScriptIntrinsicYuvToRGB
import android.widget.Button
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rl_v1.ui.theme.MakeFilterSection
import java.text.ChoiceFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.truncate
import kotlin.reflect.typeOf

@Composable
/**
 *  Displays a new filter screen for a User to browse campsites via filters and returns the selected filters to MainScreen
 *
 *  @returns nothing
 */
public fun MakeFilterScreen(
    confirmFilters: (Int, Pair<String,String>, Int, ArrayList<String>, Boolean) -> Unit
){
    val deviceLocation by remember {mutableStateOf("Canmore")}

    //save these filters to get pushed up
    var selectedDistance by remember { mutableIntStateOf(10) }
    var selectedDates by remember {mutableStateOf<Pair<String,String>>(Pair("mm/dd/yyyy", "mm/dd/yyyy"))}
    var numGuests by remember {mutableIntStateOf(4)}

    var preferences = listOf<String>("Forest", "Hiking", "Lake")        //can add more if desired
    var selectedPreferences = remember {mutableStateListOf<Boolean>(false, false, false)}

    var daysBooked by remember { mutableLongStateOf(0) }
    val selectedChoiceColor = Color(18,92,70)
    val choiceColor = Color(231,238,229)
    val activeFilterBooleans = remember { mutableStateListOf(false, false, false)}

    //may want to turn this into a LazyColumn if more filters are added
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.Start){
        Text(
            text = "Filter Campsites",
            fontSize = 20.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )
        MakeFilterSection("Location", "Near my device - $deviceLocation", buttonAction = {
            activeFilterBooleans[0] = !activeFilterBooleans[0]          //expand to allow the choice of 10, 50 or 100km
        })
        if (activeFilterBooleans[0]){                                                       //filter choices appear directly under the filter button when its selected
            val distanceOptions = listOf<Int>(10, 50, 100)                                  //in km
            var selected by remember {mutableIntStateOf(0)}
            Row(modifier = Modifier.fillMaxWidth()){
                for (distance in distanceOptions){
                    val isSelected = distanceOptions.indexOf(distance) == selected
                    Button(
                        onClick = { selected = distanceOptions.indexOf(distance); selectedDistance = distance},         //button formatting
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) selectedChoiceColor else choiceColor,
                            contentColor = if (isSelected) Color.White else selectedChoiceColor),
                        contentPadding = PaddingValues(horizontal = 6.dp),  //button padding
                        modifier = Modifier.padding(horizontal = 4.dp)      //padding between buttons
                    ){Text("$distance km", fontSize = 12.sp)}
                }
            }
        }

        MakeFilterSection("Stay date(s)", "${selectedDates.first} - ${selectedDates.second}\n$daysBooked days", buttonAction = {
            activeFilterBooleans[1] = !activeFilterBooleans[1]
        })
        if (activeFilterBooleans[1]){
            DateRangePickerModal(
                onDateRangeSelected = {
                    val formatter = SimpleDateFormat("MM/dd/yyyy",Locale.getDefault())
                    val difference = it.second!! - it.first!!
                    daysBooked = difference / 86400000   //millisecond calculation, 86400000 milliseconds in a day
                    selectedDates = Pair(formatter.format(Date(it.first!!)),formatter.format(Date(it.second!!)))
                    activeFilterBooleans[1] = true
                },
                onDismiss = {activeFilterBooleans[1] = false}
            )
        }

        MakeFilterSection("Guests", "$numGuests", buttonAction = {
            activeFilterBooleans[2] = !activeFilterBooleans[2]
        })
        if (activeFilterBooleans[2]){
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically){
                Button(
                    onClick = {numGuests-=1},
                    colors = ButtonDefaults.buttonColors(containerColor = choiceColor, contentColor = selectedChoiceColor),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ){Text("-")}
                Text("$numGuests", modifier = Modifier.padding(horizontal = 8.dp), textAlign = TextAlign.Center)
                Button(
                    onClick = {numGuests+=1},
                    colors = ButtonDefaults.buttonColors(containerColor = choiceColor, contentColor = selectedChoiceColor),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ){Text("+")}
            }
        }

        Text(
            text = "Campsite Preferences",
            fontSize = 16.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )
        Row(modifier = Modifier.fillMaxWidth()){
            for (pref in preferences){
                Button(
                    onClick = {
                        selectedPreferences[preferences.indexOf(pref)] = !selectedPreferences[preferences.indexOf(pref)]
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedPreferences[preferences.indexOf(pref)]) selectedChoiceColor else choiceColor,
                        contentColor = if (selectedPreferences[preferences.indexOf(pref)]) Color.White else selectedChoiceColor
                    ),
                    contentPadding = PaddingValues(horizontal = 2.dp),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ){Text(pref, fontSize = 12.sp)}
            }
        }
        //WOW Factor #2 goes here if we select it (Match Score according to User's preferences?)

        Button(
            onClick = {
                var chosenPreferences = arrayListOf<String>()
                for (pref in preferences){
                    if (selectedPreferences[preferences.indexOf(pref)]){ chosenPreferences.add(pref)}
                }
                confirmFilters(selectedDistance, selectedDates, numGuests, chosenPreferences,false)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = selectedChoiceColor,
                contentColor = Color.White
            )
        ){ Text("Show Campsites")}      // original says: "Show 12 campsites" for campsites that filter properly, will have to come back to this
    }
}

@Composable
/**
 *  Displays a section on the screen for a designated filter (helper function)
 *
 *  @returns nothing
 */
private fun MakeFilterSection(header: String, subheader: String, buttonAction: () -> Unit){
    //colors are based off Fig.02 in UI Screen Mockup
    val boxColor = Color(231,238,229)
    val buttonHeaderTextColor = Color(24,47,39)
    val buttonSubheadTextColor = Color(128,139,129)

    Button(onClick = {buttonAction()},  // signal to expand and pop up more button options via function callback
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = boxColor),
        shape = RoundedCornerShape(14.dp),

        ){
        Column(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)){
            Text(text = header, color = buttonHeaderTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = FontFamily.SansSerif, textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
            Text(text = subheader, color = buttonSubheadTextColor, fontSize = 10.sp, fontFamily = FontFamily.SansSerif, textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
        }
    }
}

/**
 *  Code taken from Android Compose documentation to implement a Calendar Window for a User to select a range of dates
 *
 *  source: https://developer.android.com/develop/ui/compose/components/datepickers#range
 */
@Composable
fun DateRangePickerModal(
    onDateRangeSelected: (Pair<Long?, Long?>) -> Unit,
    onDismiss: () -> Unit
) {
    val dateRangePickerState = rememberDateRangePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateRangeSelected(
                        Pair(
                            dateRangePickerState.selectedStartDateMillis,
                            dateRangePickerState.selectedEndDateMillis
                        )
                    )
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            title = {
                Text(
                    text = "Select date range"
                )
            },
            showModeToggle = false,
            modifier = Modifier
                .height(500.dp)
                .padding(16.dp)
        )
    }
}