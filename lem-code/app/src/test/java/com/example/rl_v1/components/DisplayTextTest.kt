package com.example.rl_v1.components

import com.example.rl_v1.ui.theme.components.priceText
import com.example.rl_v1.ui.theme.components.ratingText
import org.junit.Assert.assertEquals
import org.junit.Test

class DisplayTextTest {

    @Test fun priceHasTwoDecimals() {
        assertEquals("$65.00 / night", priceText(65.0))
        assertEquals("$12.50 / night", priceText(12.5))
    }

    @Test fun freeCampsiteShowsZero() {
        assertEquals("$0.00 / night", priceText(0.0))
    }

    @Test fun noRatingsYet() {
        assertEquals("No ratings yet", ratingText(0.0, 0))
    }

    @Test fun ratingHasOneDecimalAndCount() {
        assertEquals("★ 4.9 (28)", ratingText(4.9, 28))
        assertEquals("★ 4.0 (1)", ratingText(4.0, 1))
    }
}
