/**
 *  Actual Lottery Repository for actual Lottery implementations
 *
 *  Is compatible with Firestore now
 *
 *  @property firestore a reference to the Firestore Database
 *  @property lotteries A fetched collection of documents resembling Lottery objects from the Firestore
 *  @property waitlistEntries A fetched collection of documents resembling WaitlistEntry objects from the Firestore
 */

package com.example.rl_v1.repository

import com.example.rl_v1.model.DateAvailability
import com.example.rl_v1.model.Lottery
import com.example.rl_v1.model.WaitlistEntry
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.type.DateTime
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random
import kotlin.reflect.typeOf

/** Shuffles [waiting] and splits it into (winners, losers), with at most [maxWinners] winners. */
fun <T> drawLottery(waiting: List<T>, maxWinners: Int, random: Random = Random.Default): Pair<List<T>, List<T>> {
    val shuffled = waiting.shuffled(random)
    val count = maxWinners.coerceAtLeast(0)
    return shuffled.take(count) to shuffled.drop(count)
}

class FirestoreLotteryRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
): LotteryRepository {
    private val lotteries = firestore.collection("lotteries")
    private val waitlistEntries = firestore.collection("waitlistEntries")   //almost implemented

    /** Saves a new Lottery and returns it with its generated document ID */
    override suspend fun publishLottery(lottery: Lottery): Lottery {
        //require( what we want ) { what shows if the condition we want is not met }
        require(lottery.id.isBlank()) { "This lottery already exists, update instead." }
        require(lottery.ownerId.isNotBlank()) { "An owner is required" }
        require(lottery.campsiteIds.isNotEmpty()) { "No campsites have been selected for this lottery." }
        require(lottery.registrationPeriod.startDate.isNotEmpty() && lottery.registrationPeriod.endDate.isNotEmpty()) { "Registration Period invalid." }
        require(lottery.bookingPeriod.startDate.isNotEmpty() && lottery.bookingPeriod.endDate.isNotEmpty()) { "Booking period invalid." }
        require(lottery.maxWinners > 0) { "Requires positive (>0) number of lottery winners" }

        val document = lotteries.document()
        val savedLottery = lottery.copy(id = document.id)

        document.set(savedLottery).await()
        return savedLottery
    }

    override suspend fun updateLottery(lottery: Lottery) {
        TODO("Not yet implemented")
    }

    override suspend fun joinWaitlist(lotteryId: String, entrantId: String): Unit {
        val chosenLottery = lotteries.document(lotteryId)
        if (updateAndCheckStatus(chosenLottery)){
            val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
            val newEntry = WaitlistEntry(
                id = entrantId,
                lotteryId = lotteryId,
                entrantId = entrantId,
                joinedAt = LocalDate.now().format(formatter),
                status = "WAITING"                                                          //technically don't need to specify, but it's easier for completeness
            )
            //make sure the Entrant doesn't already have an entry for this Lottery
            val exists = waitlistEntries
                .whereEqualTo("entrantId", entrantId)
                .whereEqualTo("lotteryId", lotteryId)
                .limit(1)
                .get().await()
            if (!exists.isEmpty){
                println("Entrant already in lottery!")
                // TODO: notify the user they are already in the lottery via UI: banner pop-up notification maybe?
            }
            else{
                // one fixed id per entrant and lottery, so a double tap writes the same entry instead of a second one
                waitlistEntries.document("${lotteryId}_$entrantId").set(newEntry).await()
                // TODO: notify the user of successful join via UI: banner pop-up notification? or switch to Activity Screen? (US 01.03.01)
            }

        }
        else{   //the lottery doesn't exist (prints 'null'), is full (prints 'FULL'), or is closed (prints 'COMPLETED')
            println(chosenLottery.get().await().toObject(Lottery::class.java)?.status)
            // TODO: notify the user the reason the lottery cannot be joined via UI: banner pop-up notification?
        }
    }


    override suspend fun leaveWaitlist(lotteryId: String, entrantId: String): Unit {
        //make sure Entry exists
        val exists = waitlistEntries
            .whereEqualTo("entrantId", entrantId)
            .whereEqualTo("lotteryId", lotteryId)
            .limit(1)
            .get().await()
        if (exists.isEmpty){
            println("Entry does not exist")     //shouldn't expect to be printed at all
        }
        else{
            for (document in exists.documents){
                document.reference.delete().await()
            }
            //println("successfully deleted entry")       //doesn't need to notify User since it disappears on their UI --
        }
    }

    /**
     *  Helper function for observeLotteriesByUser
     *
     *  @returns a Flow list of WaitlistEntries
     */
    override fun observeEntriesByUser(userId: String): Flow<List<WaitlistEntry>> = callbackFlow{
        val query = waitlistEntries.whereEqualTo("entrantId", userId)
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null){
                close(error)
            }
            else if (snapshot != null){
                val entryList = snapshot.documents.mapNotNull { document ->
                    document.toObject(WaitlistEntry::class.java)?.copy(id = document.id)
                }
                trySend(entryList)
            }
        }
        awaitClose { listener.remove() }
    }

    override fun observeLotteriesByUser(userId: String, type: String): Flow<List<Lottery>>{
        return combine(observeAllLotteries(), observeEntriesByUser(userId)) { lotteries, entries ->
            when (type) {
                "Selected" -> { //TODO: return a Flow List of Lotteries the User has won (been randomly selected in) after Lottery Selection has been implemented
                    val openLotteries = lotteries.filter {it.status == "OPEN"}
                    val userWonEntries = entries.filter {
                        it.entrantId == userId &&
                                it.lotteryId in openLotteries.map { lottery -> lottery.id } &&
                                it.status == "WON"
                    }
                    userWonEntries.mapNotNull { entry ->
                        openLotteries.find { it.id == entry.lotteryId }
                    }
                }

                "Joined" -> {   //return a Flow List of Lotteries the User has Joined
                    val openLotteries = lotteries.filter { it.status == "OPEN" }

                    val userWaitlistEntries = entries.filter {
                        it.entrantId == userId &&
                                it.lotteryId in openLotteries.map { lottery -> lottery.id } &&
                                it.status == "WAITING"
                    }

                    userWaitlistEntries.mapNotNull { entry ->
                        openLotteries.find { it.id == entry.lotteryId }
                    }
                }
                "Not Selected" -> { //TODO: return a Flow List of Lotteries the User has lost after Lottery Selection has been implemented
                    val openLotteries = lotteries.filter { it.status == "OPEN" }
                    val userLostEntries = entries.filter {
                        it.entrantId == userId &&
                                it.lotteryId in openLotteries.map { lottery -> lottery.id } &&
                                it.status == "TERMINATED"
                    }
                    userLostEntries.mapNotNull { entry ->
                        openLotteries.find { it.id == entry.lotteryId }
                    }
                }
                else -> emptyList()
            }.distinctBy { it.id }  // an entrant with two entries in one lottery would list it twice and crash the Activity list
        }
    }

    override fun observeAllLotteries(): Flow<List<Lottery>> = callbackFlow{
        val listener = lotteries.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
            } else if (snapshot != null) {
                val lotteryList = snapshot.documents.mapNotNull { document ->
                    document.toObject(Lottery::class.java)
                        ?.copy(id = document.id)
                }
                trySend(lotteryList)
            }
        }
        awaitClose { listener.remove() }
    }

    override fun observeLotteriesByOwner(ownerId: String): Flow<List<Lottery>> {        //not needed by a user story
        TODO("Not yet implemented")
    }

    override fun observeWaitlistUsersByOwner(campsiteId: String, ownerId: String): Flow<Lottery> {       //US 02.09.01
        TODO("Not yet implemented")
    }

    override fun notifyWaitlistUsers(campsiteId: String, ownerId: String) {                       //US 02.10.01
        TODO("Not yet implemented")
    }

    /**
     * Draws [lotteryId]: winners become WON (US 01.03.01) and everyone else still waiting becomes TERMINATED (US 01.03.02).
     * Entrants are notified by these statuses: their Activity tab and Inbox listen to their waitlist entries.
     * Returns the number of winners.
     */
    override suspend fun selectWinners(lotteryId: String): Int {
        val chosenLottery = lotteries.document(lotteryId).get().await().toObject(Lottery::class.java)
            ?: error("Lottery $lotteryId not found")
        // only entries still in the draw, so drawing again can't re-pick a winner or a loser
        val waiting = waitlistEntries
            .whereEqualTo("lotteryId", lotteryId)
            .whereEqualTo("status", "WAITING")
            .get()
            .await()
            .documents
        val (winners, losers) = drawLottery(waiting, chosenLottery.maxWinners)

        // One batch, so every entry changes together or none do.
        // shortcut: a batch holds 500 writes; split it if a lottery ever has more than 500 waiting entrants.
        val batch = firestore.batch()
        for (winner in winners) batch.update(winner.reference, "status", "WON")
        for (loser in losers) batch.update(loser.reference, "status", "TERMINATED")
        batch.commit().await()

        return winners.size
    }

    /*
    EXTERNAL FUNCTIONS ----------
     */

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
    override suspend fun updateAndCheckStatus(lotteryDocument: DocumentReference): Boolean{
        val entryCount = waitlistEntries
            .whereEqualTo("lotteryId",lotteryDocument.id)    //find matching lotteries
            .get()
            .await()
            .size()

        //check capacity eligibility if owner optionally set capacity
        val actualLottery = lotteryDocument.get().await().toObject(Lottery::class.java)
        if (actualLottery?.capacity != null && entryCount >= actualLottery.capacity){
            actualLottery.status = "FULL"
            return false
        }
        else if (actualLottery == null){    //this lottery doesn't exist
            return false
        }

        //check registration eligibility
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        val registrationStart = LocalDate.parse(actualLottery.registrationPeriod.startDate, formatter)
        val registrationEnd = LocalDate.parse(actualLottery.registrationPeriod.endDate, formatter)
        val currentDate = LocalDate.now()
        val todayEligible = (currentDate >= registrationStart) && (currentDate <= registrationEnd)

        if (!todayEligible){
            actualLottery.status = "CLOSED"
            return false
        }

        //else, the lottery is open
        actualLottery.status = "OPEN"
        return true
    }

    override suspend fun getLotteryID(campID: String): String?{
        val snapshot = lotteries
            .whereArrayContains("campsiteIds", campID)
            .limit(1)
            .get()
            .await()
        return snapshot.documents.firstOrNull()?.id
    }

    /**
     *  Targets a User's WaitlistEntry for a lottery to decline the opportunity had they own the lottery
     *
     *  Called by ViewModel.callDeclineOp()
     *
     *  @returns nothing
     */
    override suspend fun declineOpportunity(lotteryID: String, entrantID: String): Unit{
        val chosenEntry = waitlistEntries                                                    //fetch the winning entry
            .whereEqualTo("lotteryId", lotteryID)
            .whereEqualTo("entrantId", entrantID)
            .limit(1)
            .get()
            .await()
        for (entry in chosenEntry.documents){                                               //for loop may not be the best way to do this but its the only way I know right now
            if (entry.toObject(WaitlistEntry::class.java)?.status == "WON"){    //check if its a winning entry (it should be)
                entry.reference.update("status","TERMINATED").await()       //only then, change it to terminated

                //OPTIONALLY let the User provide a reason
            }
            else{
                println("$entry is not a winning entry!")
            }
        }
    }

    override suspend fun resetEntries(userID: String){
        val toDelete = waitlistEntries
            .whereEqualTo("entrantId", userID)
            .get()
            .await()
        for (entry in toDelete.documents){
            //println("WILL DELETE: $entry")
            entry.reference.delete()
        }
    }

}