package com.example.rl_v1.screens

import com.example.rl_v1.model.Campsite
import com.example.rl_v1.ui.theme.screens.CampsiteForm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CampsiteFormTest {

    private val original = Campsite(
        id = "1", ownerId = "owner", name = "Old", location = "Banff, AB",
        maxOccupancy = 4, pricePerNight = 50.0, rating = 4.5, ratingCount = 3
    )
    private val base = CampsiteForm.from(original)

    @Test fun untouchedFormReturnsTheSameCampsite() {
        assertEquals(original, base.applyTo(original).getOrThrow())
    }

    @Test fun editsAreAppliedAndTrimmed() {
        val result = base.copy(name = "  New name  ", maxOccupancy = " 8 ", pricePerNight = "65.5")
            .applyTo(original).getOrThrow()
        assertEquals("New name", result.name)
        assertEquals(8, result.maxOccupancy)
        assertEquals(65.5, result.pricePerNight, 0.0)
    }

    @Test fun idOwnerAndRatingAreNeverChanged() {
        val result = base.copy(name = "New").applyTo(original).getOrThrow()
        assertEquals("1", result.id)
        assertEquals("owner", result.ownerId)
        assertEquals(4.5, result.rating, 0.0)
        assertEquals(3, result.ratingCount)
    }

    @Test fun amenitiesSplitOnCommasAndSkipBlanks() {
        val result = base.copy(amenities = "Cookhouse, , Hiking trails ,").applyTo(original).getOrThrow()
        assertEquals(listOf("Cookhouse", "Hiking trails"), result.amenities)
    }

    @Test fun blankCoordinatesBecomeNull() {
        val withCoords = original.copy(latitude = 51.0, longitude = -115.0)
        val result = CampsiteForm.from(withCoords).copy(latitude = "", longitude = "")
            .applyTo(withCoords).getOrThrow()
        assertNull(result.latitude)
        assertNull(result.longitude)
    }

    @Test fun badNumbersFail() {
        assertTrue(base.copy(maxOccupancy = "six").applyTo(original).isFailure)
        assertTrue(base.copy(maxOccupancy = "2.5").applyTo(original).isFailure)
        assertTrue(base.copy(pricePerNight = "free").applyTo(original).isFailure)
        assertTrue(base.copy(latitude = "north").applyTo(original).isFailure)
    }
}