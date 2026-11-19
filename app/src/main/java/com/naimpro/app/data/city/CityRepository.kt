package com.naimpro.app.data.city

import android.content.Context
import com.naimpro.app.domain.model.Location
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.ZoneId
import java.util.zip.GZIPInputStream

/**
 * Offline city catalogue built from the GeoNames dump (CC BY 4.0).
 *
 * The asset is a gzipped, pipe separated file. It is parsed once on a
 * background dispatcher and kept in memory (~20k entries, well under a
 * megabyte of heap), which makes search instant and needs no network call.
 */
class CityRepository(private val context: Context) {
    private class Entry(
        val location: Location,
        val normalizedName: String,
        val population: Int,
        val isTurkey: Boolean,
    )

    @Volatile
    private var entries: List<Entry>? = null

    private val lock = Any()

    fun size(): Int = all().size

    private fun all(): List<Entry> {
        entries?.let { return it }
        synchronized(lock) {
            entries?.let { return it }
            val loaded =
                load().sortedWith(
                    compareByDescending<Entry> { it.isTurkey }.thenByDescending { it.population },
                )
            entries = loaded
            return loaded
        }
    }

    fun search(
        query: String,
        limit: Int = 40,
    ): List<Location> {
        val normalized = normalize(query)
        if (normalized.length < 2) return emptyList()
        val source = all()
        val starts = ArrayList<Location>(limit)
        val contains = ArrayList<Location>(limit)
        for (entry in source) {
            if (entry.normalizedName.startsWith(normalized)) {
                starts.add(entry.location)
                if (starts.size >= limit) break
            } else if (contains.size < limit && entry.normalizedName.contains(normalized)) {
                contains.add(entry.location)
            }
        }
        return (starts + contains).distinctBy { it.id }.take(limit)
    }

    fun byId(id: String): Location? = all().firstOrNull { it.location.id == id }?.location

    fun defaultLocation(): Location {
        all().firstOrNull {
            it.isTurkey && it.location.name == "Ankara" && it.location.region == "Ankara"
        }?.let { return it.location }
        return all().firstOrNull { it.isTurkey }?.location
            ?: Location(
                id = "fallback",
                name = "Ankara",
                region = "Ankara",
                country = "Türkiye",
                latitude = 39.9334,
                longitude = 32.8597,
                zoneId = ZoneId.of("Europe/Istanbul"),
            )
    }

    /** Nearest known settlement, used to label a GPS derived location. */
    fun nearest(
        latitude: Double,
        longitude: Double,
    ): Location? {
        var best: Entry? = null
        var bestDistance = Double.MAX_VALUE
        for (entry in all()) {
            val dLat = entry.location.latitude - latitude
            val dLon =
                (entry.location.longitude - longitude) *
                    Math.cos(Math.toRadians(latitude))
            val distance = dLat * dLat + dLon * dLon
            if (distance < bestDistance) {
                bestDistance = distance
                best = entry
            }
        }
        return best?.location
    }

    /** Cities shown before the user types anything (largest Turkish cities). */
    fun suggestions(limit: Int = 12): List<Location> =
        all()
            .asSequence()
            .filter { it.isTurkey }
            .take(limit)
            .map { it.location }
            .toList()

    private fun load(): List<Entry> {
        val result = ArrayList<Entry>(20_000)
        context.assets.open(ASSET_NAME).use { raw ->
            GZIPInputStream(raw, BUFFER_SIZE).use { gz ->
                BufferedReader(InputStreamReader(gz, Charsets.UTF_8), BUFFER_SIZE).use { reader ->
                    reader.forEachCityLine { line -> parse(line)?.let(result::add) }
                }
            }
        }
        return result
    }

    /** Streams the CSV body, skipping the leading format/header lines. */
    private inline fun BufferedReader.forEachCityLine(action: (String) -> Unit) {
        var line = readLine()
        if (line != null && line.startsWith(HEADER_PREFIX)) {
            line = readLine()
        }
        while (line != null) {
            action(line)
            line = readLine()
        }
    }

    private fun parse(line: String): Entry? {
        if (line.isBlank()) return null
        val parts = line.split('|')
        if (parts.size < 7) return null
        val latitude = parts[3].toDoubleOrNull() ?: return null
        val longitude = parts[4].toDoubleOrNull() ?: return null
        val countryCode = parts[0]
        val name = parts[1].trim()
        val region = parts[2].trim()
        val zone = runCatching { ZoneId.of(parts[5].trim()) }.getOrDefault(ZoneId.systemDefault())
        return Entry(
            location =
                Location(
                    id = "$countryCode/$name/$latitude/$longitude",
                    name = name,
                    region = region,
                    country = countryName(countryCode),
                    latitude = latitude,
                    longitude = longitude,
                    zoneId = zone,
                ),
            normalizedName = normalize(name),
            population = parts[6].toIntOrNull() ?: 0,
            isTurkey = countryCode == "TR",
        )
    }

    companion object {
        const val ASSET_NAME = "cities.csv.gz"
        private const val HEADER_PREFIX = "v1"
        private const val BUFFER_SIZE = 64 * 1024

        fun normalize(input: String): String =
            input
                .lowercase()
                .replace('ı', 'i')
                .replace('İ', 'i')
                .replace('ş', 's')
                .replace('ğ', 'g')
                .replace('ü', 'u')
                .replace('ö', 'o')
                .replace('ç', 'c')
                .replace('â', 'a')
                .replace('î', 'i')
                .replace('û', 'u')
                .trim()

        /** Turkish display names for the countries in the bundled data. */
        private val countryNames =
            mapOf(
                "TR" to "Türkiye",
                "CY" to "Kıbrıs",
                "AZ" to "Azerbaycan",
                "DE" to "Almanya",
                "NL" to "Hollanda",
                "FR" to "Fransa",
                "GB" to "Birleşik Krallık",
                "US" to "ABD",
                "AT" to "Avusturya",
                "BE" to "Belçika",
                "CH" to "İsviçre",
                "SE" to "İsveç",
                "DK" to "Danimarka",
                "NO" to "Norveç",
                "IT" to "İtalya",
                "ES" to "İspanya",
                "RU" to "Rusya",
                "SA" to "Suudi Arabistan",
                "EG" to "Mısır",
                "IQ" to "Irak",
                "IR" to "İran",
                "SY" to "Suriye",
                "BG" to "Bulgaristan",
                "GR" to "Yunanistan",
                "AL" to "Arnavutluk",
                "BA" to "Bosna-Hersek",
                "XK" to "Kosova",
                "GE" to "Gürcistan",
                "KZ" to "Kazakistan",
                "PK" to "Pakistan",
                "ID" to "Endonezya",
                "MY" to "Malezya",
                "CA" to "Kanada",
                "AU" to "Avustralya",
                "MA" to "Fas",
                "DZ" to "Cezayir",
                "TN" to "Tunus",
                "JO" to "Ürdün",
                "QA" to "Katar",
                "AE" to "BAE",
                "UA" to "Ukrayna",
                "RO" to "Romanya",
            )

        fun countryName(code: String): String = countryNames[code] ?: code
    }
}
