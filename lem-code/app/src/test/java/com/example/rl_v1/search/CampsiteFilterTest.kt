package com.example.rl_v1.search

import com.example.rl_v1.model.Campsite
import org.junit.Assert.assertEquals
import org.junit.Test
import com.example.rl_v1.ui.theme.viewmodel.CampsiteViewModel.Filters

class CampsiteFilterTest {

    private val small = Campsite(
        id = "1", name = "Small Forest", maxOccupancy = 2,
        geographicFeature = listOf("Forest")
    )
    private val medium = Campsite(
        id = "2", name = "Medium Lake", maxOccupancy = 6,
        geographicFeature = listOf("Lake", "Forest")
    )
    private val large = Campsite(
        id = "3", name = "Large Mountain", maxOccupancy = 10,
        geographicFeature = listOf("Mountain")
    )
    private val all = listOf(small, medium, large)

    /** Defaults mean "no filter selected". */
    private fun filters(
        numGuests: Int? = 0,
        preferences: List<String>? = emptyList()
    ) = Filters(numGuests = numGuests, preferences = preferences)

    // ---- no filters ----

    @Test fun noFiltersReturnsEverything() {
        assertEquals(all, all.filterBy(filters()))
    }

    @Test fun emptyListStaysEmpty() {
        assertEquals(emptyList<Campsite>(), emptyList<Campsite>().filterBy(filters(numGuests = 4)))
    }

    // ---- guests ----

    @Test fun guestsKeepsCampsitesWithEnoughRoom() {
        assertEquals(listOf(medium, large), all.filterBy(filters(numGuests = 5)))
    }

    @Test fun guestsEqualToMaxOccupancyIsKept() {
        assertEquals(listOf(medium, large), all.filterBy(filters(numGuests = 6)))
    }

    @Test fun guestsAboveEveryCampsiteReturnsEmpty() {
        assertEquals(emptyList<Campsite>(), all.filterBy(filters(numGuests = 11)))
    }

    // ---- preferences ----

    @Test fun singlePreferenceKeepsMatchingCampsites() {
        assertEquals(listOf(small, medium), all.filterBy(filters(preferences = listOf("Forest"))))
    }

    @Test fun anyOfSeveralPreferencesMatches() {
        assertEquals(
            listOf(small, medium, large),
            all.filterBy(filters(preferences = listOf("Forest", "Mountain")))
        )
    }

    @Test fun preferenceNobodyHasReturnsEmpty() {
        assertEquals(emptyList<Campsite>(), all.filterBy(filters(preferences = listOf("Desert"))))
    }

    @Test fun campsiteWithNoFeaturesIsExcludedWhenPreferencesSet() {
        val bare = Campsite(id = "4", name = "Bare", maxOccupancy = 4)
        assertEquals(emptyList<Campsite>(), listOf(bare).filterBy(filters(preferences = listOf("Forest"))))
    }

    // ---- combined ----

    @Test fun guestsAndPreferencesBothMustPass() {
        // Forest: small (2) and medium (6). Needs 4 guests: only medium.
        assertEquals(
            listOf(medium),
            all.filterBy(filters(numGuests = 4, preferences = listOf("Forest")))
        )
    }

    @Test fun originalOrderIsPreserved() {
        val reversed = listOf(large, medium, small)
        assertEquals(listOf(large, medium), reversed.filterBy(filters(numGuests = 5)))
    }

    @Test fun filteringDoesNotChangeTheInputList() {
        val input = all.toList()
        input.filterBy(filters(numGuests = 5))
        assertEquals(all, input)
    }
}