/**
 * A campsite listing published by an owner.
 *
 * This class is immutable and holds all the details of a campsite;
 *
 * @property id Unique id, assigned when the campsite is published.
 * @property ownerId Id of the owner who published it.
 * @property name Display name, e.g. "Hinton Pines".
 * @property location Where the campsite is, e.g. "Hinton, AB".
 * @property maxOccupancy Maximum number of guests.
 * @property pricePerNight Price for one night, in dollars.
 * @property rating Average rating of the campsite, e.g. 4.9.
 * @property ratingCount Number of ratings; 0 means no ratings yet.
 */
package com.example.rl_v1.model

data class Campsite(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val location: String = "",
    val maxOccupancy: Int = 0,
    val pricePerNight: Double = 0.0,
    val rating: Double = 0.0,   // average rating
    val ratingCount: Int = 0,   // 0 means no ratings yet
)