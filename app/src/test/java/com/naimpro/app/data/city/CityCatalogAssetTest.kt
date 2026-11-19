package com.naimpro.app.data.city

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File
import java.util.zip.GZIPInputStream

/**
 * Guards the bundled city catalogue.
 *
 * The release crash of 1.0.0 happened because the build repackaged
 * `cities.csv.gz` as `cities.csv`, which the loader could not find. These tests
 * make sure the data file itself stays intact and that the gzip sniffing used
 * for both packaged names behaves correctly.
 */
class CityCatalogAssetTest {
    private fun assetFile(): File {
        val candidates =
            listOf(
                File("src/main/assets/cities.csv.gz"),
                File("app/src/main/assets/cities.csv.gz"),
            )
        return candidates.firstOrNull { it.isFile }
            ?: error("cities.csv.gz not found, cwd=${File(".").absolutePath}")
    }

    @Test
    @DisplayName("The repository asset is a valid gzip file")
    fun assetIsGzip() {
        val file = assetFile()
        assertTrue(file.length() > 100_000, "unexpected asset size: ${file.length()}")
        file.inputStream().use { stream ->
            val head = stream.readNBytes(2)
            assertTrue(CityRepository.isGzip(head), "asset must start with the gzip magic bytes")
        }
    }

    @Test
    @DisplayName("Gzip sniffing distinguishes plain text from gzip")
    fun gzipDetection() {
        assertTrue(CityRepository.isGzip(byteArrayOf(0x1F, 0x8B.toByte())))
        assertFalse(CityRepository.isGzip("v1\nTR|Ankara".toByteArray()))
        assertFalse(CityRepository.isGzip(ByteArray(0)))
        assertFalse(CityRepository.isGzip(byteArrayOf(0x1F)))
    }

    @Test
    @DisplayName("The catalogue has at least 19,000 valid rows including Ankara")
    fun catalogueContents() {
        val rows = ArrayList<String>(20_000)
        GZIPInputStream(assetFile().inputStream(), 64 * 1024).bufferedReader(Charsets.UTF_8).use { reader ->
            var line = reader.readLine()
            if (line != null && line.startsWith("v1")) line = reader.readLine()
            while (line != null) {
                if (line.isNotBlank()) rows.add(line)
                line = reader.readLine()
            }
        }

        assertTrue(rows.size >= 19_000, "only ${rows.size} rows in the catalogue")
        assertTrue(rows.all { it.split('|').size >= 7 }, "every row needs 7 pipe separated fields")
        assertTrue(rows.all { it.split('|')[3].toDoubleOrNull() != null }, "latitude must parse")
        assertTrue(rows.all { it.split('|')[4].toDoubleOrNull() != null }, "longitude must parse")
        assertEquals(1, rows.count { it.startsWith("TR|Ankara|Ankara|") }, "Ankara must be present once")
    }
}
