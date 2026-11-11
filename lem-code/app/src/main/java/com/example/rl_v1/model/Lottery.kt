/**
 * Lottery Class to hold and apply all relevant details to a lottery
 *
 * Lotteries can only be joined from a Campsite Details screen, or via QR Code
 * note: US 02.04.02 says campsite owners can list multiple campsites in a lottery, thus campsiteIds is a list of all the campsites included in that lottery
 *
 * @property id Unique ID to differentiate between lotteries, assigned when lottery is created
 * @property campsiteIds IDs of the corresponding campsites
 * @property ownerId ID of the Campsite's owner, copied from the campsite
 * @property registrationPeriod The dates the lottery window is open for (is separate from Campsite availability dates)
 * @property bookingPeriod The dates Users are entering to book for
 * @property maxWinners The max size of the Lottery's winning pool (set by the Owner via US 02.04.05)
 * @property capacity An option to the owner to set a limit on how many people can join the lottery
 * @property status The current state of the Lottery (OPEN, FULL, COMPLETED)
 *      - 'OPEN' - The lottery is free to join by Users
 *      - 'FULL' - The lottery has not concluded, but is currently not joinable due to capacity constraints
 *      - 'COMPLETED' - The lottery has already drawn its winners and the window is closed, is only stored for historical purposes
 */

package com.example.rl_v1.model

data class Lottery(
    val id: String = "",
    val campsiteIds: List<String> = emptyList(),
    val ownerId: String = "",
    val registrationPeriod: DateAvailability = DateAvailability("",""),
    val bookingPeriod: DateAvailability = DateAvailability("",""),
    val maxWinners: Int = 5,
    val capacity: Int? = null,
    var status: String = "OPEN",
){
}