package com.naimpro.app.data.city

import android.content.Context
import com.naimpro.app.domain.model.Location
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStream
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
            val parsed = load()
            // The catalogue must never be able to break the app: if the asset is
            // unreadable the built in shortlist keeps times and search working.
            val source = if (parsed.isEmpty()) fallbackEntries() else parsed
            val loaded =
                source.sortedWith(
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
        val raw = openCatalog() ?: return result
        raw.use { stream ->
            val buffered = BufferedInputStream(stream, BUFFER_SIZE)
            val body: InputStream =
                if (isGzip(buffered.peekTwo())) {
                    GZIPInputStream(buffered, BUFFER_SIZE)
                } else {
                    buffered
                }
            body.use { input ->
                BufferedReader(InputStreamReader(input, Charsets.UTF_8), BUFFER_SIZE).use { reader ->
                    reader.forEachCityLine { line -> parse(line)?.let(result::add) }
                }
            }
        }
        return result
    }

    /**
     * AAPT2 gunzips assets whose name ends in `.gz` while packaging, so a real
     * APK usually contains `cities.csv` even though the repository stores
     * `cities.csv.gz`. Both names are accepted and the payload is sniffed
     * instead of trusting the file name.
     */
    private fun openCatalog(): InputStream? {
        for (name in CATALOG_NAMES) {
            val stream = runCatching { context.assets.open(name) }.getOrNull()
            if (stream != null) return stream
        }
        return null
    }

    private fun BufferedInputStream.peekTwo(): ByteArray {
        mark(2)
        val head = ByteArray(2)
        val read = read(head)
        reset()
        return if (read == 2) head else ByteArray(0)
    }

    private fun fallbackEntries(): List<Entry> = FALLBACK_LINES.mapNotNull(::parse)

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
        /** Name used in the repository; AAPT2 may repackage it as [PLAIN_ASSET_NAME]. */
        const val ASSET_NAME = "cities.csv.gz"
        const val PLAIN_ASSET_NAME = "cities.csv"

        private val CATALOG_NAMES = listOf(ASSET_NAME, PLAIN_ASSET_NAME)
        private const val HEADER_PREFIX = "v1"
        private const val BUFFER_SIZE = 64 * 1024

        /** True when [head] starts with the gzip magic bytes (`1f 8b`). */
        fun isGzip(head: ByteArray): Boolean {
            if (head.size < 2) return false
            return head[0] == 0x1F.toByte() && head[1] == 0x8B.toByte()
        }

        /**
         * Minimal built in catalogue, used only when the packaged asset cannot be
         * read. Keeping the app usable matters more than completeness here.
         */
        private val FALLBACK_LINES =
            listOf(
                "TR|Ankara|Ankara|39.9334|32.8597|Europe/Istanbul|5663000",
                "TR|İstanbul|İstanbul|41.0082|28.9784|Europe/Istanbul|15462452",
                "TR|İzmir|İzmir|38.4237|27.1428|Europe/Istanbul|4438829",
                "TR|Bursa|Bursa|40.1956|29.0600|Europe/Istanbul|3101833",
                "TR|Antalya|Antalya|36.8969|30.7133|Europe/Istanbul|2619832",
                "TR|Konya|Konya|37.8746|32.4932|Europe/Istanbul|2312954",
                "TR|Adana|Adana|37.0000|35.3213|Europe/Istanbul|2267016",
                "TR|Gaziantep|Gaziantep|37.0662|37.3833|Europe/Istanbul|2118561",
                "TR|Kayseri|Kayseri|38.7312|35.4787|Europe/Istanbul|1428516",
                "TR|Trabzon|Trabzon|41.0027|39.7168|Europe/Istanbul|808974",
                "TR|Diyarbakır|Diyarbakır|37.9144|40.2306|Europe/Istanbul|1798309",
                "TR|Şanlıurfa|Şanlıurfa|37.1591|38.7969|Europe/Istanbul|2079037",
                "TR|Van|Van|38.4942|43.3800|Europe/Istanbul|1137019",
                "TR|Erzurum|Erzurum|39.9043|41.2679|Europe/Istanbul|767848",
                "TR|Samsun|Samsun|41.2867|36.3300|Europe/Istanbul|1376989",
                "TR|Denizli|Denizli|37.7765|29.0864|Europe/Istanbul|1056829",
                "TR|Eskişehir|Eskişehir|39.7767|30.5206|Europe/Istanbul|898369",
                "TR|Muğla|Muğla|37.2153|28.3636|Europe/Istanbul|1007734",
                "DE|Berlin|Berlin|52.5200|13.4050|Europe/Berlin|3644826",
                "GB|London|England|51.5074|-0.1278|Europe/London|8961989",
                "US|New York|New York|40.7128|-74.0060|America/New_York|8419000",
                "SA|Makkah|Makkah al Mukarramah|21.4225|39.8262|Asia/Riyadh|1325000",
                "SA|Medina|Al Madinah al Munawwarah|24.4672|39.6111|Asia/Riyadh|1186000",
                "AZ|Bakı|Bakı|40.4093|49.8671|Asia/Baku|2300500",
                "NL|Amsterdam|Noord-Holland|52.3676|4.9041|Europe/Amsterdam|1149000",
                "FR|Paris|Île-de-France|48.8566|2.3522|Europe/Paris|2148000",
                "AT|Viyana|Wien|48.2082|16.3738|Europe/Vienna|1911191",
                "CH|Zürich|Zürich|47.3769|8.5417|Europe/Zurich|421878",
            )

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
