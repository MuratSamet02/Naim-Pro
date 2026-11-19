package com.naimpro.app.data.repo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.naimpro.app.domain.model.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.ZoneId

private val Context.locationStore: DataStore<Preferences> by preferencesDataStore(name = "naim_location")

@Serializable
private data class StoredLocation(
    val id: String,
    val name: String,
    val region: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String,
    val isGps: Boolean = false,
)

/** Persists the selected location so widgets and receivers can read it synchronously. */
class LocationRepository(private val context: Context, private val json: Json) {
    private object Keys {
        val location = stringPreferencesKey("selected_location")
    }

    val location: Flow<Location?> =
        context.locationStore.data
            .map { prefs -> prefs[Keys.location]?.let(::decode) }
            .distinctUntilChanged()

    suspend fun current(): Location? = location.first()

    suspend fun save(location: Location) {
        context.locationStore.edit { prefs ->
            prefs[Keys.location] =
                json.encodeToString(
                    StoredLocation(
                        id = location.id,
                        name = location.name,
                        region = location.region,
                        country = location.country,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        timeZoneId = location.zoneId.id,
                        isGps = location.isGps,
                    ),
                )
        }
    }

    private fun decode(raw: String): Location? =
        runCatching {
            val stored = json.decodeFromString<StoredLocation>(raw)
            Location(
                id = stored.id,
                name = stored.name,
                region = stored.region,
                country = stored.country,
                latitude = stored.latitude,
                longitude = stored.longitude,
                zoneId = runCatching { ZoneId.of(stored.timeZoneId) }.getOrDefault(ZoneId.systemDefault()),
                isGps = stored.isGps,
            )
        }.getOrNull()
}
