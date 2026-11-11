/**
 *  An entry in a Lottery's waitlist of a particular User to a Campsite Posting
 *
 *  @property id The id of this particular entry - is unique across each waitlist and User
 *  @property lotteryId The id of the corresponding lottery this waitlist exists for
 *  @property entrantId The id of the corresponding User
 *  @property joinedAt The date the User joined the waitlist
 *  @property status The current status of the entry (WAITING, TERMINATED)
 *      'WAITING' - This Entrant is currently in the waitlist just waiting for the lottery to be drawn
 *      'TERMINATED' - This Waitlist Entry did not win the lottery but is saved for history purposes
 */


package com.example.rl_v1.model

data class WaitlistEntry(
    val id: String = "",
    val lotteryId: String = "",
    val entrantId: String = "",
    val joinedAt: String = "",
    var status: String = "WAITING"
){
}