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
 * @property description Free-text description of the campsite.
 * @property amenities What the campsite offers, e.g. "Cookhouse".
 * @property latitude Map location latitude, or null if not set.
 * @property longitude Map location longitude, or null if not set.
 * @property imageUrl Optional promotional image URL (not displayed yet).
 * @property rules House rules guests must follow.
 * @property cancellationPolicy Cancellation terms.
 * @property availability Dates the campsite posting is available to be reserved.
 * @property geographicFeatures Features near / around the Campsite
 */
package com.example.rl_v1.model

data class Campsite(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val location: String = "",
    val maxOccupancy: Int = 0,
    val pricePerNight: Double = 0.0,
    val rating: Double = 0.0,
    val ratingCount: Int = 0,
    val description: String = "",
    val amenities: List<String> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val imageUrl: String? = null,
    val rules: String = "",
    val cancellationPolicy: String = "",
    val availability: Pair<String, String> = Pair("",""), //written in mm/dd/yyyy, the date formatting is weird in Compose
    val geographicFeature: List<String> = emptyList()
)