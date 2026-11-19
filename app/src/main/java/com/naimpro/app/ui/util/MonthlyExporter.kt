package com.naimpro.app.ui.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.naimpro.app.domain.hijri.HijriCalendar
import com.naimpro.app.domain.model.Location
import com.naimpro.app.domain.model.PrayerTimes
import java.io.File
import java.util.Locale

/**
 * Creates a monthly CSV or PDF table in the app cache and returns the file so
 * the caller can hand it to the system share sheet (FileProvider).
 */
object MonthlyExporter {
    /** Column keys shared by the CSV and PDF exports. */
    private val columnKeys =
        listOf(
            "monthly_hijri",
            "prayer_imsak",
            "prayer_gunes",
            "prayer_ogle",
            "prayer_ikindi",
            "prayer_aksam",
            "prayer_yatsi",
        )

    fun csv(
        context: Context,
        location: Location,
        times: List<PrayerTimes>,
        languageTag: String,
    ): File {
        val directory = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(directory, "naim-pro-${fileStamp(times)}.csv")
        val builder = StringBuilder()
        builder.appendLine("city;${location.displayName}")
        val header = listOf("date") + columnKeys.map { TableLabels.of(it, languageTag) }
        builder.appendLine(header.joinToString(";"))
        for (day in times) {
            val hijri = HijriCalendar.fromGregorian(day.date)
            val row =
                listOf(
                    day.date.toString(),
                    "${hijri.day}.${hijri.month}.${hijri.year}",
                    day.imsak.clock(),
                    day.gunes.clock(),
                    day.ogle.clock(),
                    day.ikindi.clock(),
                    day.aksam.clock(),
                    day.yatsi.clock(),
                )
            builder.appendLine(row.joinToString(";"))
        }
        file.writeText(builder.toString(), Charsets.UTF_8)
        return file
    }

    fun pdf(
        context: Context,
        location: Location,
        times: List<PrayerTimes>,
        languageTag: String,
    ): File {
        val directory = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(directory, "naim-pro-${fileStamp(times)}.pdf")

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paints = paints()

        var y = 48f
        canvas.drawText("NAİM PRO", MARGIN, y, paints.title)
        y += 18f
        canvas.drawText(location.displayName, MARGIN, y, paints.subtitle)
        y += 14f
        val monthLabel =
            times.firstOrNull()?.date?.let { date ->
                date.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag(languageTag)))
            }.orEmpty()
        canvas.drawText(monthLabel, MARGIN, y, paints.subtitle)
        y += 20f

        val headers =
            listOf(
                headerFor("monthly_day", languageTag),
                headerFor("monthly_hijri", languageTag),
                headerFor("prayer_imsak", languageTag),
                headerFor("prayer_gunes", languageTag),
                headerFor("prayer_ogle", languageTag),
                headerFor("prayer_ikindi", languageTag),
                headerFor("prayer_aksam", languageTag),
                headerFor("prayer_yatsi", languageTag),
            )
        val columnWidths = floatArrayOf(42f, 72f, 56f, 56f, 56f, 56f, 56f, 56f)
        val tableWidth = columnWidths.sum()
        val headerHeight = 18f

        canvas.drawRect(
            MARGIN,
            y,
            MARGIN + tableWidth,
            y + headerHeight,
            Paint().apply {
                color = Color.parseColor("#17606A")
            },
        )
        var x = MARGIN
        headers.forEachIndexed { index, header ->
            canvas.drawText(header, x + 5f, y + 12.5f, paints.header)
            x += columnWidths[index]
        }
        y += headerHeight

        times.forEachIndexed { index, day ->
            if (y + 16f > PAGE_HEIGHT - MARGIN) return@forEachIndexed
            if (index % 2 == 0) {
                canvas.drawRect(
                    MARGIN,
                    y,
                    MARGIN + tableWidth,
                    y + 16f,
                    Paint().apply {
                        color = Color.parseColor("#F1F6F5")
                    },
                )
            }
            val hijri = HijriCalendar.fromGregorian(day.date)
            val cells =
                listOf(
                    day.date.dayOfMonth.toString(),
                    "${hijri.day}.${hijri.month}",
                    day.imsak.clock(),
                    day.gunes.clock(),
                    day.ogle.clock(),
                    day.ikindi.clock(),
                    day.aksam.clock(),
                    day.yatsi.clock(),
                )
            x = MARGIN
            cells.forEachIndexed { cellIndex, cell ->
                canvas.drawText(cell, x + 5f, y + 11.5f, paints.cell)
                x += columnWidths[cellIndex]
            }
            canvas.drawRect(MARGIN, y + 16f, MARGIN + tableWidth, y + 16f, paints.line)
            y += 16f
        }

        y += 16f
        if (y < PAGE_HEIGHT - MARGIN) {
            canvas.drawText(
                "Generated offline by Naim Pro · GPL-3.0 · no tracking",
                MARGIN,
                y,
                paints.subtitle,
            )
        }

        document.finishPage(page)
        file.outputStream().use { document.writeTo(it) }
        document.close()
        return file
    }

    private class PdfPaints(
        val title: Paint,
        val subtitle: Paint,
        val header: Paint,
        val cell: Paint,
        val line: Paint,
    )

    private fun paints(): PdfPaints =
        PdfPaints(
            title =
                Paint().apply {
                    isAntiAlias = true
                    textSize = 20f
                    color = Color.parseColor("#0B2A2E")
                    isFakeBoldText = true
                },
            subtitle =
                Paint().apply {
                    isAntiAlias = true
                    textSize = 11f
                    color = Color.parseColor("#3F4949")
                },
            header =
                Paint().apply {
                    isAntiAlias = true
                    textSize = 9.5f
                    color = Color.WHITE
                    isFakeBoldText = true
                },
            cell =
                Paint().apply {
                    isAntiAlias = true
                    textSize = 9.5f
                    color = Color.parseColor("#161D1D")
                },
            line =
                Paint().apply {
                    color = Color.parseColor("#DAE5E4")
                    strokeWidth = 0.6f
                },
        )

    private fun headerFor(
        key: String,
        languageTag: String,
    ): String = TableLabels.of(key, languageTag)

    private fun fileStamp(times: List<PrayerTimes>): String {
        val first = times.firstOrNull()?.date ?: java.time.LocalDate.now()
        return "${first.year}-${"%02d".format(first.monthValue)}"
    }

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 36f
}
