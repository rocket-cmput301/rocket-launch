package com.example.rl_v1.search

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