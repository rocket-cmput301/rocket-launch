package com.example.rl_v1.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class DrawLotteryTest {

    private val waiting = listOf("a", "b", "c", "d", "e", "f", "g")

    @Test fun picksAtMostMaxWinnersAndEveryoneElseLoses() {
        val (winners, losers) = drawLottery(waiting, 3, Random(1))

        assertEquals(3, winners.size)
        assertEquals(4, losers.size)
        assertTrue(winners.intersect(losers.toSet()).isEmpty())
        assertEquals(waiting.toSet(), (winners + losers).toSet())
    }

    @Test fun everyoneWinsWhenThereAreFewerEntrantsThanSpots() {
        val (winners, losers) = drawLottery(listOf("a", "b"), 5)

        assertEquals(setOf("a", "b"), winners.toSet())
        assertTrue(losers.isEmpty())
    }

    @Test fun zeroOrNegativeSpotsMeansEveryoneLoses() {
        assertTrue(drawLottery(waiting, 0).first.isEmpty())
        assertEquals(waiting.size, drawLottery(waiting, -2).second.size)
    }

    @Test fun emptyLotteryHasNoWinnersOrLosers() {
        val (winners, losers) = drawLottery(emptyList<String>(), 3)

        assertTrue(winners.isEmpty())
        assertTrue(losers.isEmpty())
    }
}
