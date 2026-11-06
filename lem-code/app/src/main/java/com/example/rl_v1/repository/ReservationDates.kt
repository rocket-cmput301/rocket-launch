package com.example.rl_v1.repository

import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Longest stay one reservation can cover. */
const val MAX_NIGHTS = 30

/**
 * The nights of a stay: every date from [checkIn] up to, but not including,
 * [checkOut]. Dates are ISO strings such as "2027-05-07".
 * Fails when a date can't be read, check-out isn't after check-in,
 * or the stay is longer than [MAX_NIGHTS].
 */
fun stayNights(checkIn: String, checkOut: String): Result<List<LocalDate>> {
    val start = parseDate(checkIn)
        ?: return Result.failure(IllegalArgumentException("Dates must look like 2027-05-07"))
    val end = parseDate(checkOut)
        ?: return Result.failure(IllegalArgumentException("Dates must look like 2027-05-07"))

    if (!end.isAfter(start)) {
        return Result.failure(IllegalArgumentException("Check-out must be after check-in"))
    }

    val nights = generateSequence(start) { it.plusDays(1) }
        .takeWhile { it.isBefore(end) }
        .take(MAX_NIGHTS + 1)
        .toList()

    if (nights.size > MAX_NIGHTS) {
        return Result.failure(
            IllegalArgumentException("Stays are limited to $MAX_NIGHTS nights")
        )
    }
    return Result.success(nights)
}

/**
 * True when every night falls inside the campsite's availability window.
 * A blank or unreadable [availableFrom] or [availableTo] means no limit on that side.
 */
fun withinAvailability(
    nights: List<LocalDate>,
    availableFrom: String,
    availableTo: String
): Boolean {
    if (nights.isEmpty()) return false
    val from = parseDate(availableFrom)
    val to = parseDate(availableTo)
    if (from != null && nights.first().isBefore(from)) return false
    if (to != null && nights.last().isAfter(to)) return false
    return true
}

private fun parseDate(text: String): LocalDate? =
    try {
        LocalDate.parse(text.trim())
    } catch (e: DateTimeParseException) {
        null
    }
