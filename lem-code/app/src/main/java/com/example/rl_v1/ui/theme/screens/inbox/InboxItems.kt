package com.example.rl_v1.ui.theme.screens.inbox

import com.example.rl_v1.model.Campsite
import com.example.rl_v1.model.Lottery
import com.example.rl_v1.model.Reservation

/** One Inbox card. [label] is what happened, such as "SELECTED"; [text] gives the details. */
data class InboxItem(val label: String, val text: String)

/**
 * Builds the entrant's Inbox from data the app already listens to, so nothing extra is saved:
 * reservations (confirmed, cancelled or rejected) first, then lotteries won, then lotteries lost.
 */
fun inboxItems(
    reservations: List<Reservation>,
    won: List<Lottery>,
    lost: List<Lottery>,
    campsites: List<Campsite>
): List<InboxItem> {
    fun name(id: String) = campsites.firstOrNull { it.id == id }?.name ?: "Campsite"
    fun names(lottery: Lottery) = campsites.filter { it.id in lottery.campsiteIds }.joinToString { it.name }

    val booked = reservations.map { r ->
        val label = when (r.status) {
            "cancelled" -> "CANCELLED"
            "rejected" -> "REJECTED"
            else -> "RESERVED"
        }
        InboxItem(label, "${name(r.campsiteId)} · ${r.checkInDate} to ${r.checkOutDate} · Confirmation ${r.id}")
    }
    val selected = won.map {
        InboxItem("SELECTED", "${names(it)} · Choose your dates before ${it.bookingPeriod.endDate}")
    }
    val notSelected = lost.map {
        InboxItem("NOT SELECTED", "${names(it)} · You were not picked this time")
    }
    return booked + selected + notSelected
}
