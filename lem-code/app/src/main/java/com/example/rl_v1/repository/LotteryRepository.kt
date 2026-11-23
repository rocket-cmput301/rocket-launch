package com.example.rl_v1.repository

import androidx.compose.ui.input.pointer.PointerId
import com.example.rl_v1.model.Lottery
import com.example.rl_v1.model.WaitlistEntry
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.flow.Flow


/** Reads and writes Campsite Lotteries. */

interface LotteryRepository {

    /** Creates a WaitlistEntry instance corresponding to a Lottery */
    suspend fun joinWaitlist(lotteryId: String, entrantId: String): Unit

    /** Terminates a WaitlistEntry for a Lottery */
    suspend fun leaveWaitlist(lotteryId: String, entrantId: String): Unit

    /** Show all WaitlistEntry objects a User has */
    fun observeEntriesByUser(userId: String): Flow<List<WaitlistEntry>>

    /** Show all lotteries a User has involvement in by type (selected, joined, not selected) */
    fun observeLotteriesByUser(userId: String, type: String): Flow<List<Lottery>>

    /** Helper function for other observing functions */
    fun observeAllLotteries(): Flow<List<Lottery>>

    /** Lists Lotteries currently owned by [ownerId] updated live,  */
    fun observeLotteriesByOwner(ownerId: String): Flow<List<Lottery>>

    /** Look at all the Users in one of [ownerId]'s campsite lotteries (US 02.09.01) */
    fun observeWaitlistUsersByOwner(campsiteId: String, ownerId: String): Flow<Lottery>

    /** Send notifications / announcements to all Users in one of [ownerId]'s campsite lotteries (US 02.10.02); Can only be done by Campsite Owners hence the ownerId parameter*/
    fun notifyWaitlistUsers(campsiteId: String, ownerId: String): Unit

    /** Randomly selects a number of Users in a Lottery waitlist to categorize as lottery winners; lottery.maxWinners is the attribute to use to indicate limit */
    suspend fun selectWinners(lotteryId: String): Int

    /* --- External Functions --- */

    /**
     *  Extension function that updates a Lottery's status and returns a boolean on if it is still possible to join
     *
     *  Defined here in LotteryRepository to preserve Lottery as a data class
     *  Currently checks eligibility by:
     *      - lottery size has less than lottery capacity
     *      - current date is within the registration period
     *
     *  @returns Boolean : true if the lottery is still eligible to join false otherwise
     */
    suspend fun updateAndCheckStatus(lotteryDocument: DocumentReference): Boolean

    /**
     *  Searches the LotteryRepo Lottery list for a lottery that contains the given campID in its campsiteIds list
     *
     *  Called in CampsiteViewModel's callJoinLottery()
     *
     *  @returns the 'id' attribute of the specified Lottery Class
     */
    suspend fun getLotteryID(campID: String): String?

    /**
     *  Targets a User's WaitlistEntry for a lottery to decline the opportunity had they own the lottery
     *
     *  Called by ViewModel.callDeclineOp()
     *
     *  @returns nothing
     */
    suspend fun declineOpportunity(lotteryID: String, entrantID: String)

    /** DEV TEST FUNCTION: used to reset all Waitlist Entries; won't exist in the actual implementation */
    suspend fun resetEntries(userID: String)
}