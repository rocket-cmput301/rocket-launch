package com.example.rl_v1.ui.theme

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.example.rl_v1.model.Campsite
import java.util.Locale

/** Link that opens the location in any maps app or browser. */
fun mapsUrl(latitude: Double, longitude: Double): String =
    "https://www.google.com/maps/search/?api=1&query=" +
            String.format(Locale.US, "%.6f,%.6f", latitude, longitude)

/** The text itself, or "N/A" when it is blank. */
fun naIfBlank(text: String): String = text.ifBlank { "N/A" }

/** Every detail we store about one campsite; missing ones show "N/A". */
@Composable
fun CampsiteDetailsScreen(campsite: Campsite, onBack: () -> Unit) {
    // The system back gesture returns to the list instead of closing the app.
    BackHandler(onBack = onBack)

    val uriHandler = LocalUriHandler.current
    val latitude = campsite.latitude
    val longitude = campsite.longitude

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

        DetailSection("Availability", naIfBlank("${campsite.availability.first} - ${campsite.availability.second}"))
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

        DetailSection("Rules", naIfBlank(campsite.rules))
        DetailSection("Cancellation policy", naIfBlank(campsite.cancellationPolicy))
        var gFeatures: String = ""
        for (feature in campsite.geographicFeature){
            gFeatures += ("$feature, ")
        }
        DetailSection("Geographic Features",naIfBlank(gFeatures))
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