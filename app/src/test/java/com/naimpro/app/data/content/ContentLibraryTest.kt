package com.naimpro.app.data.content

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/** Guards the offline content library against empty or duplicated entries. */
class ContentLibraryTest {
    @Test
    @DisplayName("There are exactly 99 names of Allah")
    fun esmaCount() {
        assertEquals(99, EsmaulHusna.entries.size)
        assertEquals((1..99).toList(), EsmaulHusna.entries.map { it.index })
    }

    @Test
    @DisplayName("Every name has Arabic, Turkish and English fields")
    fun esmaFields() {
        for (entry in EsmaulHusna.entries) {
            assertTrue(entry.arabic.isNotBlank(), "arabic missing for ${entry.index}")
            assertTrue(entry.turkish.isNotBlank(), "turkish missing for ${entry.index}")
            assertTrue(entry.meaningTr.isNotBlank(), "meaningTr missing for ${entry.index}")
            assertTrue(entry.meaningEn.isNotBlank(), "meaningEn missing for ${entry.index}")
        }
    }

    @Test
    @DisplayName("Turkish names are unique")
    fun esmaUnique() {
        val names = EsmaulHusna.entries.map { it.turkish }
        assertEquals(names.size, names.distinct().size)
    }

    @Test
    @DisplayName("Prayers, verses and hadith all carry a source and unique ids")
    fun contentIntegrity() {
        val groups =
            mapOf(
                "dua" to ContentLibrary.allDuas(),
                "ayet" to ContentLibrary.allAyets(),
                "hadis" to ContentLibrary.allHadiths(),
            )
        for ((label, items) in groups) {
            assertTrue(items.size >= 20, "$label has only ${items.size} entries")
            assertEquals(items.size, items.map { it.id }.distinct().size, "$label ids are not unique")
            for (item in items) {
                assertTrue(item.arabic.isNotBlank(), "${item.id} has no Arabic text")
                assertTrue(item.text.isNotBlank(), "${item.id} has no translation")
                assertTrue(item.source.isNotBlank(), "${item.id} has no source")
            }
        }
    }

    @Test
    @DisplayName("Daily rotation is stable and cycles through the library")
    fun dailyRotation() {
        val first = ContentLibrary.dailyDuas(0)
        assertEquals(first, ContentLibrary.dailyDuas(ContentLibrary.duas.size))
        val distinct = (0 until ContentLibrary.duas.size).map { ContentLibrary.dailyDuas(it).id }.toSet()
        assertEquals(ContentLibrary.duas.size, distinct.size)
        // Negative seeds must not crash.
        assertTrue(ContentLibrary.dailyAyet(-5).id.isNotBlank())
    }
}
