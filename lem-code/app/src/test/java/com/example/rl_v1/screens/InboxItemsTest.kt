package com.example.rl_v1.screens

import com.example.rl_v1.model.Campsite
import com.example.rl_v1.model.DateAvailability
import com.example.rl_v1.model.Lottery
import com.example.rl_v1.model.Reservation
import com.example.rl_v1.ui.theme.screens.inbox.InboxItem
import com.example.rl_v1.ui.theme.screens.inbox.inboxItems
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InboxItemsTest {

    private val campsites = listOf(
        Campsite(id = "hinton", name = "Hinton Pines"),
        Campsite(id = "lake", name = "Lake View")
    )
    private val reservation = Reservation(
        id = "res1", campsiteId = "hinton", checkInDate = "2027-05-07", checkOutDate = "2027-05-09"
    )
    private val lottery = Lottery(
        id = "lot1", campsiteIds = listOf("hinton", "lake"),
        bookingPeriod = DateAvailability("2027-04-01", "2027-05-01")
    )

    @Test fun confirmedReservationIsReservedWithConfirmationNumber() {
        val items = inboxItems(listOf(reservation), emptyList(), emptyList(), campsites)

        assertEquals(
            listOf(InboxItem("RESERVED", "Hinton Pines · 2027-05-07 to 2027-05-09 · Confirmation res1")),
            items
        )
    }

    @Test fun cancelledAndRejectedReservationsGetTheirOwnLabels() {
        val items = inboxItems(
            listOf(reservation.copy(status = "cancelled"), reservation.copy(id = "res2", status = "rejected")),
            emptyList(), emptyList(), campsites
        )

        assertEquals(listOf("CANCELLED", "REJECTED"), items.map { it.label })
    }

    @Test fun wonAndLostLotteriesNameTheirCampsites() {
        val items = inboxItems(emptyList(), listOf(lottery), listOf(lottery.copy(id = "lot2")), campsites)

        assertEquals(
            listOf(
                InboxItem("SELECTED", "Hinton Pines, Lake View · Choose your dates before 2027-05-01"),
                InboxItem("NOT SELECTED", "Hinton Pines, Lake View · You were not picked this time")
            ),
            items
        )
    }

    @Test fun reservationsComeBeforeLotteryResults() {
        val items = inboxItems(listOf(reservation), listOf(lottery), listOf(lottery), campsites)

        assertEquals(listOf("RESERVED", "SELECTED", "NOT SELECTED"), items.map { it.label })
    }

    @Test fun unknownCampsiteFallsBackToAGenericName() {
        val items = inboxItems(listOf(reservation.copy(campsiteId = "gone")), emptyList(), emptyList(), campsites)

        assertTrue(items.single().text.startsWith("Campsite · "))
    }

    @Test fun nothingToShowGivesAnEmptyInbox() {
        assertTrue(inboxItems(emptyList(), emptyList(), emptyList(), campsites).isEmpty())
    }
}
