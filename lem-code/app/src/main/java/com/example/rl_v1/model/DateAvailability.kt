/**
 * Date selections made by a User
 *
 * Data class created to ensure 'Date' data types carry over to Firestore properly
 *
 *  @property startDate the first day a Campsite is available to be reserved ; required
 *  @property endDate the last day a Campsite is available to be reserved ; is empty if Campsite is available for one day
 */

package com.example.rl_v1.model
data class DateAvailability(
    val startDate: String = "",
    val endDate: String = ""
){
}