package com.example.rl_v1.search

import com.example.rl_v1.ui.theme.viewmodel.CampsiteViewModel.Filters
import com.example.rl_v1.model.Campsite

/**
 * Returns the campsites whose name or location contains any word of [query].
 * Campsites matching more words come first; ties keep their original order.
 * A blank query returns the list unchanged.
 */
fun List<Campsite>.search(query: String): List<Campsite> {
    val words = query.lowercase()
        .split(Regex("[^\\p{L}\\p{N}]+"))
        .filter { it.isNotEmpty() }
        .distinct()
    if (words.isEmpty()) return this

    return map { it to it.matchCount(words) }
        .filter { (_, count) -> count > 0 }
        .sortedByDescending { (_, count) -> count }
        .map { (campsite, _) -> campsite }
}

private fun Campsite.matchCount(words: List<String>): Int {
    val haystack = "$name $location".lowercase()
    return words.count { it in haystack }
}

/**
 *  Returns the campsites that have not been filtered out by a User's selected filters.
 *  No filters selected returns the list unchanged
 *
 *  note: distance and date functionality not implemented yet
 */
fun List<Campsite>.filterBy(filter: Filters): List<Campsite>{
    val filteredList = mutableListOf<Campsite>()
    for (site in this){     //probably not the most efficient for many sites
        var notEnoughOccupancy = false
        var matchingPreferences = true


        //distance = deviceLocation - site.location -> then add (distance > filter.distanceFilter)

        //val matchingDates = (Date(site.availability.first) > Date(filter.dates?.first)) &&
        //        (Date(site.availability.second) < Date(filter.dates?.second))

        val numGuests = filter.numGuests
        if (numGuests != null && numGuests != 0){     //default value
            notEnoughOccupancy = site.maxOccupancy < numGuests
        }

        val preferences = filter.preferences
        if (!preferences.isNullOrEmpty()){     //default value
            matchingPreferences = site.geographicFeature.any { preferences.contains(it) }
        }

        if (notEnoughOccupancy || !matchingPreferences ) {   //don't add site
            continue
        } else {
            filteredList.add(site)
        }
    }
    return filteredList
}