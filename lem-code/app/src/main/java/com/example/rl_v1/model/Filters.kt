/**
 * Filter selections made by a User
 *
 * Data class created just because tuples in Kotlin exist only for two or three values (Pair, Triple respectively)
 * Also created just to be consistent with OOP principles and the UML diagram
 *
 *  @property distanceFilter Indicates maximum kilometer radius of campsites to be shown
 *  @property dates Indicates date range of Campsites available to be shown
 *  @property numGuests Indicates max number of guests a Campsite can accommodate
 *  @property preferences Indicates geographical preferences a User prefers
 */

package com.example.rl_v1.model

data class Filters(
    val distanceFilter: Int = 10,
    val dates: DateAvailability? = null,
    val numGuests: Int? = 0,
    val preferences: List<String>? = emptyList()
) {
}