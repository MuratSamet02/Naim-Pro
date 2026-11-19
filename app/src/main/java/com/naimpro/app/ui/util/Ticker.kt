package com.naimpro.app.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.ZoneId

/** A wall clock that recomposes once per second inside the given time zone. */
@Composable
fun rememberNow(
    zone: ZoneId,
    intervalMs: Long = 1_000,
): LocalDateTime {
    val state by produceState(initialValue = LocalDateTime.now(zone), zone, intervalMs) {
        while (true) {
            value = LocalDateTime.now(zone)
            delay(intervalMs)
        }
    }
    return state
}

/** Simple mutable holder used by screens that need local, non persisted state. */
@Composable
fun <T> rememberMutableState(initial: T) = remember { mutableStateOf(initial) }
