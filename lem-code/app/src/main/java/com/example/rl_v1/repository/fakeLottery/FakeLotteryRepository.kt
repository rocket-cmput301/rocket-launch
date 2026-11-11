/**
 *  Fake Lottery Repository for testing lottery implementations, UI and hard coded back end
 *
 *  will be converted to a Firestore Compatible Repository after initial fake commit
 *
 *  @property _lotteries A private hard-coded (for test purposes) Lottery document collection -- will eventually be replaced
 *  @property lotteries  A public dynamic container of the corresponding lottery collection
 *  @property _entries A private list to hold all waitlist entries in the app
 *  @property entries A public dynamic container of the corresponding entries
 *
 */

package com.example.rl_v1.repository.fakeLottery

import com.example.rl_v1.model.DateAvailability
import com.example.rl_v1.model.Lottery
import com.example.rl_v1.model.WaitlistEntry
import com.example.rl_v1.repository.LotteryRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

class FakeLotteryRepository(

    private val _lotteries: MutableStateFlow<List<Lottery>> = MutableStateFlow<List<Lottery>>(
        listOf(
            Lottery(
                id = "lot1",
                campsiteIds = mutableListOf<String>("7lHDTEmnngKp0vE3YU4p"),        //ff database for testing
                registrationPeriod = DateAvailability("11/10/2026","11/12/2026"),
                bookingPeriod = DateAvailability("11/15/2026","11/16/2026"),
                maxWinners = 4,
                capacity = 10,
                status = "OPEN"
            ),
            Lottery(
                id = "lot2",
                campsiteIds = mutableListOf<String>("HOz04ctjcI2qXgTlM89O"),        //'a' database for testing
                registrationPeriod = DateAvailability("11/10/2026","11/12/2026"),
                bookingPeriod = DateAvailability("11/15/2026","11/16/2026"),
                maxWinners = 5,
                capacity = 12,
                status = "OPEN"
            )
        ),
    ),
    var lotteries: StateFlow<List<Lottery>> = _lotteries,

    private val _entries: MutableStateFlow<List<WaitlistEntry>> = MutableStateFlow<List<WaitlistEntry>>(emptyList()),
    var entries: StateFlow<List<WaitlistEntry>> = _entries

    //private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()            //the real firebase link later

): LotteryRepository {
    override suspend fun joinWaitlist(lotteryId: String, entrantId: String) {
        val chosenLottery = lotteries.value.find { it.id == lotteryId }                     //locate the lottery the User is joining
        if (chosenLottery?.updateAndCheckStatus() == true){                                 //if conditions are fulfilled: (capacity is not full AND current date within registration period) -> lottery is still open
            val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
            val newEntry = WaitlistEntry(
                id = entrantId,
                lotteryId = lotteryId,
                entrantId = entrantId,
                joinedAt = LocalDate.now().format(formatter),
                status = "WAITING"                                                          //technically don't need to specify, but it's easier for completeness
            )
            _entries.update { currentEntries ->
                currentEntries + newEntry
            }
        }
    }

    override suspend fun leaveWaitlist(lotteryId: String, entrantId: String) {
        _entries.update { currentEntries ->
            currentEntries.filterNot { entry ->                                 //remove the specified entry from entries list since we don't need to retain history
                entry.lotteryId == lotteryId && entry.entrantId == entrantId    //locate the entry
            }
        }
    }

    override fun observeLotteriesByUser(userId: String, type: String): Flow<List<Lottery>> {
        return combine(_lotteries, _entries) { lotteries, entries ->
            when (type) {
                "Selected" -> { //return a Flow List of Lotteries the User has won (been randomly selected in)
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

                "Not Selected" -> { //return a Flow List of Lotteries the User has lost
                    emptyList()
                }

                else -> emptyList()
            }
        }
    }

    override fun observeAllLotteries(): Flow<List<Lottery>> {
        TODO("Not yet implemented")
    }

    override fun observeLotteriesByOwner(ownerId: String): Flow<List<Lottery>> {
        TODO("Not yet implemented")
    }

    override fun observeWaitlistEntries(campsiteId: String, ownerId: String): Flow<Lottery> {
        TODO("Not yet implemented")
    }

    override fun notifyWaitlistEntries(campsiteId: String, ownerId: String) {
        TODO("Not yet implemented")
    }

    override fun selectWinners(lotteryId: String): Int {
        TODO("Not yet implemented")
    }

    /* External Functions
        -----------------------------------------------------------------------------------*/
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
    fun Lottery.updateAndCheckStatus(): Boolean{

        //check capacity eligibility
        val entryCount: List<WaitlistEntry> = entries.value.filter {it.lotteryId == this.id}
        if (this.capacity != null && entryCount.size >= this.capacity){                     //only check if capacity is optionally set by the Owner
            this.status = "FULL"
            return false
        }

        //check registration eligibility
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        val registrationStart = LocalDate.parse(this.registrationPeriod.startDate, formatter)
        val registrationEnd = LocalDate.parse(this.registrationPeriod.endDate, formatter)
        val currentDate = LocalDate.now()
        val todayEligible = (currentDate >= registrationStart) && (currentDate <= registrationEnd)
        if (!todayEligible){
            this.status = "CLOSED"
            return false
        }

        //else, the lottery is open
        this.status = "OPEN"
        return true
    }

    /**
     *  Searches the LotteryRepo Lottery list for a lottery that contains the given campId in its campsiteIds list
     *
     *  Used in CampsiteViewModel
     *
     *  @returns The "id" attribute of the specified Lottery Class
     */
    fun getLotteryId(campId: String): String? {
        return lotteries.value.find { lottery ->
            campId in lottery.campsiteIds       //scour all campsites in the list
        }?.id
    }
}