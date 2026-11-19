package com.naimpro.app.widget

import android.content.Context
import android.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.naimpro.app.MainActivity

private val Background = ColorProvider(Color.parseColor("#0B2A2E"))
private val Accent = ColorProvider(Color.parseColor("#C9A227"))
private val OnSurface = ColorProvider(Color.parseColor("#F2F6F5"))
private val Muted = ColorProvider(Color.parseColor("#8FA9A5"))

/** 2x2 widget: next prayer and the remaining time. */
class SmallPrayerWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val data =
            runCatching { WidgetDataLoader.load(context) }
                .getOrElse { WidgetDataLoader.placeholder() }
        provideContent {
            GlanceTheme {
                Column(
                    modifier =
                        GlanceModifier
                            .fillMaxSize()
                            .background(Background)
                            .cornerRadius(20.dp)
                            .padding(12.dp)
                            .clickable(actionStartActivity(android.content.Intent(context, MainActivity::class.java))),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = data.nextPrayerName,
                        style =
                            TextStyle(
                                color = Accent,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                            ),
                        maxLines = 1,
                    )
                    Text(
                        text = data.nextPrayerTime,
                        style =
                            TextStyle(
                                color = OnSurface,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                            ),
                        maxLines = 1,
                    )
                    Text(
                        text = data.countdown,
                        style = TextStyle(color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center),
                        maxLines = 1,
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = data.locationName,
                        style = TextStyle(color = Muted, fontSize = 11.sp, textAlign = TextAlign.Center),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/** 4x2 widget: next prayer plus a compact list of the six times. */
class MediumPrayerWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val data =
            runCatching { WidgetDataLoader.load(context) }
                .getOrElse { WidgetDataLoader.placeholder() }
        provideContent {
            GlanceTheme {
                Column(
                    modifier =
                        GlanceModifier
                            .fillMaxSize()
                            .background(Background)
                            .cornerRadius(20.dp)
                            .padding(12.dp)
                            .clickable(actionStartActivity(android.content.Intent(context, MainActivity::class.java))),
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = GlanceModifier.defaultWeight()) {
                            Text(
                                text = data.nextPrayerName,
                                style =
                                    TextStyle(
                                        color = Accent,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                maxLines = 1,
                            )
                            Text(
                                text = data.locationName,
                                style = TextStyle(color = Muted, fontSize = 11.sp),
                                maxLines = 1,
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = data.nextPrayerTime,
                                style =
                                    TextStyle(
                                        color = OnSurface,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                maxLines = 1,
                            )
                            Text(
                                text = data.countdown,
                                style = TextStyle(color = Muted, fontSize = 11.sp),
                                maxLines = 1,
                            )
                        }
                    }
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        data.times.take(6).forEach { (name, time) ->
                            Column(
                                modifier = GlanceModifier.defaultWeight(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = name,
                                    style = TextStyle(color = Muted, fontSize = 10.sp),
                                    maxLines = 1,
                                )
                                Text(
                                    text = time,
                                    style =
                                        TextStyle(
                                            color = OnSurface,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                        ),
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** 4x4 widget: full day overview with hijri date. */
class LargePrayerWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val data =
            runCatching { WidgetDataLoader.load(context) }
                .getOrElse { WidgetDataLoader.placeholder() }
        provideContent {
            GlanceTheme {
                Column(
                    modifier =
                        GlanceModifier
                            .fillMaxSize()
                            .background(Background)
                            .cornerRadius(24.dp)
                            .padding(16.dp)
                            .clickable(actionStartActivity(android.content.Intent(context, MainActivity::class.java))),
                ) {
                    Text(
                        text = "NAÄ°M PRO",
                        style =
                            TextStyle(
                                color = Accent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        maxLines = 1,
                    )
                    Spacer(modifier = GlanceModifier.height(6.dp))
                    Text(
                        text = data.nextPrayerName + " Â· " + data.nextPrayerTime,
                        style =
                            TextStyle(
                                color = OnSurface,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        maxLines = 1,
                    )
                    Text(
                        text = data.countdown + " kaldÄ±",
                        style = TextStyle(color = Muted, fontSize = 13.sp),
                        maxLines = 1,
                    )
                    Spacer(modifier = GlanceModifier.height(10.dp))
                    data.times.forEach { (name, time) ->
                        Row(
                            modifier = GlanceModifier.fillMaxWidth().padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = name,
                                modifier = GlanceModifier.defaultWeight(),
                                style = TextStyle(color = Muted, fontSize = 13.sp),
                                maxLines = 1,
                            )
                            Text(
                                text = time,
                                style =
                                    TextStyle(
                                        color = OnSurface,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                    ),
                                maxLines = 1,
                            )
                        }
                    }
                    Spacer(modifier = GlanceModifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = data.locationDetail,
                            modifier = GlanceModifier.defaultWeight(),
                            style = TextStyle(color = Muted, fontSize = 11.sp),
                            maxLines = 1,
                        )
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        Text(
                            text = data.hijriDate,
                            style = TextStyle(color = Accent, fontSize = 11.sp),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

/** Responsive helper kept for documentation of the supported cell sizes. */
internal val supportedSizes: Set<DpSize> =
    setOf(
        DpSize(110.dp, 110.dp),
        DpSize(250.dp, 110.dp),
        DpSize(250.dp, 250.dp),
    )
