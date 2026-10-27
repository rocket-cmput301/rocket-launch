package com.example.rl_v1.search

import com.example.rl_v1.model.Campsite
import org.junit.Assert.assertEquals
import org.junit.Test

class CampsiteSearchTest {

    private val lakeside = Campsite(id = "1", name = "Lakeside", location = "Banff, AB")
    private val pines = Campsite(id = "2", name = "Pines", location = "Banff, AB")
    private val hinton = Campsite(id = "3", name = "Hinton Cookhouse", location = "Hinton, AB")
    private val all = listOf(lakeside, pines, hinton)

    @Test fun blankQueryReturnsEverything() {
        assertEquals(all, all.search("   "))
    }

    @Test fun matchesNameIgnoringCase() {
        assertEquals(listOf(pines), all.search("PINES"))
    }

    @Test fun matchesLocation() {
        assertEquals(listOf(lakeside, pines), all.search("banff"))
    }

    @Test fun partialWordMatches() {
        assertEquals(listOf(lakeside, pines), all.search("ban"))
    }

    @Test fun punctuationIsIgnored() {
        assertEquals(listOf(lakeside, pines, hinton), all.search("Banff & Hinton"))
    }

    @Test fun moreMatchedWordsRankFirst() {
        assertEquals(listOf(pines, lakeside), all.search("banff pines"))
    }

    @Test fun noMatchReturnsEmpty() {
        assertEquals(emptyList<Campsite>(), all.search("jasper"))
    }
}