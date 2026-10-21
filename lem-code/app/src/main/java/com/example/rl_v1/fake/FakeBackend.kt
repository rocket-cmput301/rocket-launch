package com.example.rl_v1.fake

import com.example.rl_v1.model.Campsite
import com.example.rl_v1.model.Reservation
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory stand-in for the real database, shared by all fake repositories.
 * Data is lost when the app process closes.
 */
object FakeBackend {

    const val OWNER_1 = "owner1"
    const val ENTRANT_1 = "entrant1"
    const val ENTRANT_2 = "entrant2"

    val campsites = MutableStateFlow(
        listOf(
            Campsite(
                id = "c1",
                ownerId = OWNER_1,
                name = "Hinton Pines",
                location = "Hinton, AB",
                maxOccupancy = 8,
                pricePerNight = 65.0
            )
        )
    )

    val reservations = MutableStateFlow<List<Reservation>>(emptyList())
}