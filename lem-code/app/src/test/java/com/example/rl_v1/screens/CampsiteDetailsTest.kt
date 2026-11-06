package com.example.rl_v1.screens

import com.example.rl_v1.ui.theme.screens.explore.mapsUrl
import com.example.rl_v1.ui.theme.screens.explore.naIfBlank
import org.junit.Assert.assertEquals
import org.junit.Test

class CampsiteDetailsTest {

    @Test fun mapsLinkUsesTheCoordinates() {
        assertEquals(
            "https://www.google.com/maps/search/?api=1&query=53.400000,-117.583300",
            mapsUrl(53.4, -117.5833)
        )
    }

    @Test fun blankTextBecomesNA() {
        assertEquals("N/A", naIfBlank(""))
        assertEquals("N/A", naIfBlank("   "))
    }

    @Test fun otherTextStaysTheSame() {
        assertEquals("Cookhouse", naIfBlank("Cookhouse"))
    }
}
