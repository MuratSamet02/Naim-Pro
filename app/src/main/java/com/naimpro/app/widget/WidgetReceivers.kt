package com.naimpro.app.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll

class SmallPrayerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SmallPrayerWidget()
}

class MediumPrayerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MediumPrayerWidget()
}

class LargePrayerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LargePrayerWidget()
}

/** Refreshes every placed widget. Cheap: three Glance updates per call. */
object WidgetUpdater {
    suspend fun refreshAll(context: Context) {
        val appContext = context.applicationContext
        runCatching { SmallPrayerWidget().updateAll(appContext) }
        runCatching { MediumPrayerWidget().updateAll(appContext) }
        runCatching { LargePrayerWidget().updateAll(appContext) }
    }

    suspend fun refreshToday(context: Context) = refreshAll(context)
}
