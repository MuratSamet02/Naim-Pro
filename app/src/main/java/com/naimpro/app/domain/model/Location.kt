package com.naimpro.app.domain.model

import java.time.LocalDate
import java.time.ZoneId

/** A place the user can compute prayer times for. */
data class Location(
    val id: String,
    val name: String,
    val region: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val zoneId: ZoneId,
    val timeZoneId: String = zoneId.id,
    val isGps: Boolean = false,
) {
    val displayName: String
        get() =
            when {
                region.isBlank() || region.equals(name, ignoreCase = true) -> "$name, $country"
                else -> "$name, $region"
            }

    /** Short label used by widgets and notifications. */
    val shortLabel: String get() = name
}

/** Persisted Hi/By date and location selection. */
data class SavedLocationState(
    val location: Location,
    val updatedAt: LocalDate,
)
