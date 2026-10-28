package com.example.rl_v1.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.rl_v1.model.Campsite
import java.util.Locale

/** "No ratings yet" or e.g. "★ 4.9 (28)" */
fun ratingText(rating: Double, ratingCount: Int): String =
    if (ratingCount <= 0) "No ratings yet"
    else "★ ${String.format(Locale.US, "%.1f", rating)} ($ratingCount)"

/** Name, location, maximum occupancy and rating. */
@Composable
fun CampsiteSummary(campsite: Campsite, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(campsite.name, style = MaterialTheme.typography.titleMedium)
        Text(campsite.location)
        Text("Up to ${campsite.maxOccupancy} guests")
        Text(ratingText(campsite.rating, campsite.ratingCount))
    }
}