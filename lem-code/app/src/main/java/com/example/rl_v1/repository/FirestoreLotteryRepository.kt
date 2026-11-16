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
import com.google.type.DateTime
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FirestoreLotteryRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
): LotteryRepository {
    private val lotteries = firestore.collection("lotteries")
    private val waitlistEntries = firestore.collection("waitlistEntries")   //almost implemented

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
                waitlistEntries.add(newEntry)
                // TODO: notify the user of successful join via UI: banner pop-up notification? or switch to Activity Screen?
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
     *  Not specified in LotteryRepository Interface
     *
     *  @returns a Flow list of WaitlistEntries
     */
    fun observeEntriesByUser(userId: String): Flow<List<WaitlistEntry>> = callbackFlow{
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
                    emptyList()
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
                    emptyList()
                }
                else -> emptyList()
            }
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

    override fun selectWinners(lotteryId: String): Int {
        TODO("Not yet implemented")
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
    suspend fun updateAndCheckStatus(lotteryDocument: DocumentReference): Boolean{
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

    /**
     *  Searches the LotteryRepo Lottery list for a lottery that contains the given campID in its campsiteIds list
     *
     *  Called in CampsiteViewModel's callJoinLottery()
     *
     *  @returns the 'id' attribute of the specified Lottery Class
     */
    suspend fun getLotteryID(campID: String): String?{
        val snapshot = lotteries
            .whereArrayContains("campsiteIds", campID)
            .limit(1)
            .get()
            .await()
        return snapshot.documents.firstOrNull()?.id
    }
}