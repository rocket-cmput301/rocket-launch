package com.example.rl_v1.ui.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rl_v1.model.Campsite
import java.util.Locale

/** e.g. "$65.00 / night". */
fun priceText(pricePerNight: Double): String =
    "\$" + String.format(Locale.US, "%.2f", pricePerNight) + " / night"

/**
 * One campsite in its own outlined box. The caller sets the size through
 * [modifier], so the same card works full-width in a list or fixed-width in
 * a carousel. [onClick] makes the whole card tappable, and [trailing] adds an
 * optional action (such as a Book button) inside the box.
 */
@Composable
fun CampsiteCard(
    campsite: Campsite,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val cardModifier =
        if (onClick != null) modifier.clickable(onClick = onClick) else modifier

    OutlinedCard(modifier = cardModifier) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CampsiteSummary(campsite)
                Text(
                    priceText(campsite.pricePerNight),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            trailing?.invoke()
        }
    }
}