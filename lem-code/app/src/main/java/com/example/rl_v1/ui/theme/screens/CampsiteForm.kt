package com.example.rl_v1.ui.theme.screens

import com.example.rl_v1.model.Campsite

/** The text typed into the edit form. */
data class CampsiteForm(
    val name: String,
    val location: String,
    val maxOccupancy: String,
    val pricePerNight: String,
    val description: String,
    val amenities: String, // comma separated
    val latitude: String,
    val longitude: String,
    val rules: String,
    val cancellationPolicy: String
) {
    /** Returns [original] with the form's values applied, or why they're invalid. */
    fun applyTo(original: Campsite): Result<Campsite> {
        val occupancy = maxOccupancy.trim().toIntOrNull()
            ?: return Result.failure(
                IllegalArgumentException("Maximum occupancy must be a whole number")
            )
        val price = pricePerNight.trim().toDoubleOrNull()
            ?: return Result.failure(IllegalArgumentException("Price must be a number"))
        val lat = optionalNumber(latitude, "Latitude").getOrElse { return Result.failure(it) }
        val lng = optionalNumber(longitude, "Longitude").getOrElse { return Result.failure(it) }

        return Result.success(
            original.copy(
                name = name.trim(),
                location = location.trim(),
                maxOccupancy = occupancy,
                pricePerNight = price,
                description = description.trim(),
                amenities = amenities.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                latitude = lat,
                longitude = lng,
                rules = rules.trim(),
                cancellationPolicy = cancellationPolicy.trim()
            )
        )
    }

    companion object {
        fun from(c: Campsite) = CampsiteForm(
            name = c.name,
            location = c.location,
            maxOccupancy = c.maxOccupancy.toString(),
            pricePerNight = c.pricePerNight.toString(),
            description = c.description,
            amenities = c.amenities.joinToString(", "),
            latitude = c.latitude?.toString() ?: "",
            longitude = c.longitude?.toString() ?: "",
            rules = c.rules,
            cancellationPolicy = c.cancellationPolicy
        )
    }
}

private fun optionalNumber(text: String, label: String): Result<Double?> {
    if (text.isBlank()) return Result.success(null)
    val value = text.trim().toDoubleOrNull()
        ?: return Result.failure(IllegalArgumentException("$label must be a number"))
    return Result.success(value)
}