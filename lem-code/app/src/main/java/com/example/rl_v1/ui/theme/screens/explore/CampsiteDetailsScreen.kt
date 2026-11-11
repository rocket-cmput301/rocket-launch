package com.example.rl_v1.ui.theme.screens.explore

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rl_v1.model.Campsite
import com.example.rl_v1.repository.fakeLottery.FakeLotteryRepository
import com.example.rl_v1.ui.theme.components.LargeButtonPrimary
import com.example.rl_v1.ui.theme.components.priceText
import com.example.rl_v1.ui.theme.components.ratingText
import java.util.Locale

/** Link that opens the location in any maps app or browser. */
fun mapsUrl(latitude: Double, longitude: Double): String =
    "https://www.google.com/maps/search/?api=1&query=" +
            String.format(Locale.US, "%.6f,%.6f", latitude, longitude)

/** The text itself, or "N/A" when it is blank. */
fun naIfBlank(text: String): String = text.ifBlank { "N/A" }

/** Every detail we store about one campsite; missing ones show "N/A". */
@Composable
fun CampsiteDetailsScreen(
    campsite: Campsite,
    onBack: () -> Unit,
    onBook: (() -> Unit)? = null,
    onJoin: () -> Unit
) {
    // The system back gesture returns to the list instead of closing the app.
    BackHandler(onBack = onBack)

    val uriHandler = LocalUriHandler.current
    val latitude = campsite.latitude
    val longitude = campsite.longitude
    val waitlistButtonText by remember { mutableStateOf("Join Waiting List") }
    var daysLeft = ""

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("Back") }

        Text(
            naIfBlank(campsite.name),
            style = MaterialTheme.typography.headlineSmall
        )

        DetailSection("Location", naIfBlank(campsite.location))
        DetailSection("Availability", naIfBlank("${campsite.availability?.startDate} - ${campsite.availability?.endDate}"))

        DetailSection(
            "Rating",
            if (campsite.ratingCount <= 0) "N/A"
            else ratingText(campsite.rating, campsite.ratingCount)
        )

        DetailSection(
            "Maximum occupancy",
            if (campsite.maxOccupancy <= 0) "N/A"
            else "${campsite.maxOccupancy} guests"
        )

        DetailSection("Price", priceText(campsite.pricePerNight))

        DetailSection("About", naIfBlank(campsite.description))

        DetailSection(
            "Amenities",
            if (campsite.amenities.isEmpty()) "N/A"
            else campsite.amenities.joinToString("\n") { "• $it" }
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Map location", style = MaterialTheme.typography.titleMedium)
            if (latitude != null && longitude != null) {
                Text(String.format(Locale.US, "%.5f, %.5f", latitude, longitude))
                OutlinedButton(onClick = {
                    try {
                        uriHandler.openUri(mapsUrl(latitude, longitude))
                    } catch (e: Exception) {
                        // No app can open the link; nothing to do.
                    }
                }) { Text("View on map") }
            } else {
                Text("N/A")
            }
        }
        var tags = ""
        for (tag in campsite.geographicFeature){ tags += "${tag}, " }
        DetailSection("Tags", naIfBlank(tags))

        DetailSection("Rules", naIfBlank(campsite.rules))
        DetailSection("Cancellation policy", naIfBlank(campsite.cancellationPolicy))


         /*
            original text: if (campsite.lottery?.registrationPeriod?.startDate != "" || campsite.lottery.registrationPeriod.endDate != "") "$waitlistButtonText ($daysLeft)" else "Waitlist Closed"
            i want the 'join waitlist' button to say: "Join Waitlist (x Days left)"
            and then the button disables if that threshold has passed already - thus the lottery is 'COMPLETED'
         */
        LargeButtonPrimary(
            text = waitlistButtonText,
            onClick = onJoin,       //US 01.01.06, go to activity > lotteries screen
            //enabled = if (campsite.lottery?.registrationPeriod?.startDate != "" || campsite.lottery.registrationPeriod.endDate != "") true else false
            enabled = true
        )
        if (onBook != null) {                                           //only possible if won lottery, may comment
            LargeButtonPrimary("Choose dates", onClick = onBook)
        }
    }
}

/** A titled block of text. */
@Composable
private fun DetailSection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(body)
    }
}


