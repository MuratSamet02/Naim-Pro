package com.naimpro.app.domain.prayer

import com.naimpro.app.domain.model.Prayer
import com.naimpro.app.domain.model.PrayerTimes
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs

/**
 * Compares the built-in engine with a snapshot of the official Diyanet times
 * captured from the public API that backs Diyanet's own application.
 *
 * The snapshot contains 10 cities x 32 days for November-December 2026. The
 * acceptance criterion is that no single prayer is off by more than one minute.
 */
class DiyanetComparisonTest {
    private val calculator = PrayerTimeCalculator()
    private val zone: ZoneId = ZoneId.of("Europe/Istanbul")

    data class City(val name: String, val latitude: Double, val longitude: Double)

    companion object {
        val cities =
            listOf(
                City("Ankara", 39.91987, 32.85427),
                City("Antalya", 36.90812, 30.69556),
                City("Diyarbakir", 37.91363, 40.21721),
                City("Hakkari", 37.57444, 43.74083),
                City("Istanbul", 41.01384, 28.94966),
                City("Izmir", 38.41273, 27.13838),
                City("Konya", 37.87135, 32.48464),
                City("Samsun", 41.27976, 36.3361),
                City("Trabzon", 41.005, 39.72694),
                City("Van", 38.49457, 43.38323),
            )

        @JvmStatic
        fun citySource() = cities.stream()
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("citySource")
    @DisplayName("Diyanet comparison stays within one minute for every prayer")
    fun comparesWithOfficialSnapshot(city: City) {
        val rows = fixtureRows(city.name)
        assertTrue(rows.size >= 30, "fixture should contain at least 30 days for ${city.name}")

        var worst = 0L
        var worstLabel = ""

        for (row in rows) {
            val expected = row.times
            val actual =
                calculator.calculate(
                    date = row.date,
                    latitude = city.latitude,
                    longitude = city.longitude,
                    zone = zone,
                    parameters = CalculationParameters(method = CalculationMethod.DIYANET),
                )
            for (prayer in Prayer.ALL) {
                val diff =
                    abs(
                        Duration.between(expected.timeOf(prayer), actual.timeOf(prayer)).toMinutes(),
                    )
                if (diff > worst) {
                    worst = diff
                    worstLabel = "${row.date} ${prayer.name}"
                }
            }
        }

        assertTrue(
            worst <= 1,
            "${city.name}: worst deviation was $worst minute(s) at $worstLabel",
        )
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("citySource")
    @DisplayName("Average absolute deviation stays below 30 seconds")
    fun averageDeviationIsSmall(city: City) {
        val rows = fixtureRows(city.name)
        var totalSeconds = 0L
        var count = 0
        for (row in rows) {
            val actual =
                calculator.calculate(
                    date = row.date,
                    latitude = city.latitude,
                    longitude = city.longitude,
                    zone = zone,
                    parameters = CalculationParameters(method = CalculationMethod.DIYANET),
                )
            for (prayer in Prayer.ALL) {
                val seconds =
                    abs(
                        Duration.between(row.times.timeOf(prayer), actual.timeOf(prayer)).seconds,
                    )
                totalSeconds += seconds
                count++
            }
        }
        val averageSeconds = totalSeconds.toDouble() / count
        assertTrue(
            averageSeconds <= 30.0,
            "${city.name}: average deviation was ${"%.1f".format(averageSeconds)} s",
        )
    }

    private data class Row(val date: LocalDate, val times: PrayerTimes)

    private fun fixtureRows(city: String): List<Row> {
        val stream =
            javaClass.classLoader!!.getResourceAsStream("diyanet-official-snapshot.csv")
                ?: error("missing diyanet-official-snapshot.csv")
        return stream.bufferedReader().useLines { lines ->
            lines.drop(1)
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    val parts = line.split(',')
                    if (parts.size < 8 || parts[0] != city) return@mapNotNull null
                    val date = LocalDate.parse(parts[1])
                    Row(
                        date = date,
                        times =
                            PrayerTimes(
                                date = date,
                                imsak = date.atTime(parseTime(parts[2])),
                                gunes = date.atTime(parseTime(parts[3])),
                                ogle = date.atTime(parseTime(parts[4])),
                                ikindi = date.atTime(parseTime(parts[5])),
                                aksam = date.atTime(parseTime(parts[6])),
                                yatsi = date.atTime(parseTime(parts[7])),
                            ),
                    )
                }
                .toList()
        }
    }

    private fun parseTime(value: String): java.time.LocalTime = java.time.LocalTime.parse(value.trim())
}
