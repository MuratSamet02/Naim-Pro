package com.naimpro.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naimpro.app.R
import com.naimpro.app.data.content.ContentLibrary
import com.naimpro.app.domain.model.Prayer
import com.naimpro.app.domain.model.PrayerTimes
import com.naimpro.app.ui.AppViewModel
import com.naimpro.app.ui.components.VerticalGap
import com.naimpro.app.ui.components.prayerLabel
import com.naimpro.app.ui.theme.BrandDeep
import com.naimpro.app.ui.theme.BrandGold
import com.naimpro.app.ui.theme.BrandGoldLight
import com.naimpro.app.ui.theme.BrandTeal
import com.naimpro.app.ui.theme.BrandTealLight
import com.naimpro.app.ui.util.clock
import com.naimpro.app.ui.util.formatCountdown
import com.naimpro.app.ui.util.rememberNow
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@Composable
fun HomeScreen(
    vm: AppViewModel,
    onPickLocation: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenQibla: () -> Unit,
    onOpenMonthly: () -> Unit,
    onOpenTracking: () -> Unit,
    onOpenZikir: () -> Unit,
    onOpenContent: () -> Unit,
) {
    val state by vm.home.collectAsStateWithLifecycle()
    val settings = state.settings
    val location = state.location
    val zone: ZoneId = location?.zoneId ?: ZoneId.systemDefault()
    val now = rememberNow(zone)
    val times = state.times
    val language = settings.language.tag

    val next: Pair<Prayer, LocalDateTime>? =
        remember(times, state.tomorrow, now) {
            times?.asList()?.firstOrNull { it.second.isAfter(now) }
                ?: state.tomorrow?.asList()?.firstOrNull()
        }

    val hijriLabel =
        state.hijri?.let { hijri ->
            "${hijri.day} ${com.naimpro.app.domain.hijri.HijriCalendar.monthName(hijri.month, language)} ${hijri.year}"
        }.orEmpty()

    val dailyDua =
        remember {
            ContentLibrary.dailyDuas(LocalDate.now().dayOfYear - 1)
        }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        onClick = onPickLocation,
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = location?.shortLabel ?: stringResource(R.string.home_location),
                                style = MaterialTheme.typography.labelLarge,
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = stringResource(R.string.home_change_location),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.nav_home),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = hijriLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.home_settings),
                    )
                }
            }
        }

        item {
            NextPrayerCard(
                next = next,
                now = now,
                times = times,
                language = language,
                remainingLabel = stringResource(R.string.home_remaining),
                nextLabel = stringResource(R.string.home_next_prayer),
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickAction(
                    label = stringResource(R.string.nav_qibla),
                    icon = Icons.Outlined.AccessTime,
                    onClick = onOpenQibla,
                    modifier = Modifier.weight(1f),
                )
                QuickAction(
                    label = stringResource(R.string.nav_monthly),
                    icon = Icons.Outlined.CalendarMonth,
                    onClick = onOpenMonthly,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickAction(
                    label = stringResource(R.string.tracking_title),
                    icon = Icons.Outlined.TrackChanges,
                    onClick = onOpenTracking,
                    modifier = Modifier.weight(1f),
                )
                QuickAction(
                    label = stringResource(R.string.zikir_title),
                    icon = Icons.Outlined.MenuBook,
                    onClick = onOpenZikir,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            Text(
                text = stringResource(R.string.home_today_times),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        if (times != null) {
            items(times.asList(), key = { it.first.name }) { (prayer, time) ->
                PrayerRow(
                    prayer = prayer,
                    time = time,
                    isNext = next?.first == prayer,
                    isPassed = time.isBefore(now),
                )
            }
        } else {
            item {
                Text(
                    text = stringResource(R.string.common_error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        item {
            VerticalGap(4.dp)
            Card(
                onClick = onOpenContent,
                shape = RoundedCornerShape(20.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                    ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.content_dua),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = dailyDua.arabic,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = dailyDua.text, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.content_source, dailyDua.source),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun NextPrayerCard(
    next: Pair<Prayer, LocalDateTime>?,
    now: LocalDateTime,
    times: PrayerTimes?,
    language: String,
    remainingLabel: String,
    nextLabel: String,
) {
    val previous: LocalDateTime? =
        times?.asList()
            ?.lastOrNull { it.second.isBefore(now) }
            ?.second

    val fraction =
        if (next != null) {
            val start = previous ?: next.second.minusHours(4)
            val total = Duration.between(start, next.second).seconds.coerceAtLeast(1)
            val elapsed = Duration.between(start, now).seconds.coerceIn(0, total)
            (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.linearGradient(listOf(BrandDeep, BrandTeal))),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = nextLabel,
                style = MaterialTheme.typography.labelLarge,
                color = BrandTealLight,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = next?.first?.let { prayerLabel(it) } ?: "—",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = next?.second?.clock() ?: "--:--",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = BrandGoldLight,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text =
                    if (next != null) {
                        "${formatCountdown(next.second, now, language)} $remainingLabel"
                    } else {
                        ""
                    },
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.85f),
            )
            Spacer(modifier = Modifier.height(14.dp))
            LinearProgressIndicator(
                progress = { fraction },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                color = BrandGold,
                trackColor = Color.White.copy(alpha = 0.18f),
            )
        }
    }
}

@Composable
private fun QuickAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun PrayerRow(
    prayer: Prayer,
    time: LocalDateTime,
    isNext: Boolean,
    isPassed: Boolean,
) {
    val container =
        when {
            isNext -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            else -> MaterialTheme.colorScheme.surface
        }
    val contentColor =
        when {
            isNext -> MaterialTheme.colorScheme.onPrimaryContainer
            isPassed -> MaterialTheme.colorScheme.onSurfaceVariant
            else -> MaterialTheme.colorScheme.onSurface
        }
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(container)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = time.clock(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = contentColor,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = prayerLabel(prayer),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            color = contentColor,
        )
        if (isNext) {
            Text(
                text = stringResource(R.string.home_upcoming),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        } else if (isPassed) {
            Text(
                text = stringResource(R.string.home_passed),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
