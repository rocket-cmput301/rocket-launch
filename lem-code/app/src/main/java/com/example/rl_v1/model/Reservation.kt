/**
 * A booking made by an entrant for a campsite.
 *
 * This class is immutable and holds all the details of a booking;
 *
 * @property id Unique id, assigned when the reservation is created.
 * @property campsiteId Id of the booked campsite.
 * @property ownerId Id of the campsite's owner, copied from the campsite.
 * @property entrantId Id of the entrant who booked.
 * @property checkInDate First night, as an ISO date such as "2027-05-07".
 * @property checkOutDate Departure day, as an ISO date such as "2027-05-09".
 * @property numberOfGuests Number of people in the booking.
 * @property totalPrice Total cost of the stay in dollars.
 */
package com.example.rl_v1.model

data class Reservation(
    val id: String = "",
    val campsiteId: String = "",
    val ownerId: String = "",
    val entrantId: String = "",
    val checkInDate: String = "",
    val checkOutDate: String = "",
    val numberOfGuests: Int = 0,
    val totalPrice: Double = 0.0

)