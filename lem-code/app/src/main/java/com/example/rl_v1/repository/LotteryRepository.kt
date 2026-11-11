package com.example.rl_v1.repository

import androidx.compose.ui.input.pointer.PointerId
import com.example.rl_v1.model.Lottery
import kotlinx.coroutines.flow.Flow


/** Reads and writes Campsite Lotteries. */

interface LotteryRepository {

    /** Creates a WaitlistEntry instance corresponding to a Lottery */
    suspend fun joinWaitlist(lotteryId: String, entrantId: String): Unit

    /** Terminates a WaitlistEntry for a Lottery */
    suspend fun leaveWaitlist(lotteryId: String, entrantId: String): Unit

    /** Show all lotteries a User has involvement in by type (selected, joined, not selected) */
    fun observeLotteriesByUser(userId: String, type: String): Flow<List<Lottery>>

    /** Admin command? May not be necessary honestly */
    fun observeAllLotteries(): Flow<List<Lottery>>

    /** Lists Lotteries currently owned by [ownerId] updated live,  */
    fun observeLotteriesByOwner(ownerId: String): Flow<List<Lottery>>

    /** Look at all the Users in one of [ownerId]'s campsite lotteries (US 02.09.01) */
    fun observeWaitlistEntries(campsiteId: String, ownerId: String): Flow<Lottery>

    /** Send notifications / announcements to all Users in one of [ownerId]'s campsite lotteries (US 02.10.02); Can only be done by Campsite Owners hence the ownerId parameter*/
    fun notifyWaitlistEntries(campsiteId: String, ownerId: String): Unit

    /** Randomly selects a number of Users in a Lottery waitlist to categorize as lottery winners; lottery.maxWinners is the attribute to use to indicate limit */
    fun selectWinners(lotteryId: String): Int


}