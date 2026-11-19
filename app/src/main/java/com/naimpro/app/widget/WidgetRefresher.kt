package com.naimpro.app.widget

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Fire-and-forget widget refresh used by ViewModels and settings callbacks. */
class WidgetRefresher(
    private val context: Context,
    private val scope: CoroutineScope,
) {
    fun refreshAsync() {
        scope.launch { WidgetUpdater.refreshAll(context) }
    }
}
