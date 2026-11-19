package com.naimpro.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naimpro.app.R
import com.naimpro.app.data.repo.SalahStatus
import com.naimpro.app.domain.model.Prayer
import com.naimpro.app.ui.AppViewModel
import com.naimpro.app.ui.components.prayerLabel
import com.naimpro.app.ui.theme.BrandGold
import com.naimpro.app.ui.util.formatWeekday
import java.time.LocalDate

private val trackedPrayers =
    listOf(
        Prayer.IMSAK,
        Prayer.OGLE,
        Prayer.IKINDI,
        Prayer.AKSAM,
        Prayer.YATSI,
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    vm: AppViewModel,
    onBack: () -> Unit,
) {
    val state by vm.home.collectAsStateWithLifecycle()
    val statuses by vm.salahStatuses.collectAsStateWithLifecycle()
    val language = state.settings.language.tag
    val today = LocalDate.now()

    val streak = rememberStreak(statuses, today)
    val week = (6 downTo 0).map { today.minusDays(it.toLong()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tracking_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        ),
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.tracking_streak),
                                style = MaterialTheme.typography.labelLarge,
                            )
                            Text(
                                text = stringResource(R.string.tracking_streak_days, streak),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Box(
                            modifier =
                                Modifier
                                    .size(58.dp)
                                    .clip(CircleShape)
                                    .background(BrandGold.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "🔥",
                                style = MaterialTheme.typography.headlineSmall,
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    week.forEach { date ->
                        val prayed =
                            trackedPrayers.count { prayer ->
                                statuses["$date|${prayer.name}"] == SalahStatus.PRAYED
                            }
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = formatWeekday(date, language).take(3),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier =
                                    Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                prayed >= 5 -> MaterialTheme.colorScheme.primary
                                                prayed > 0 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            },
                                        ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (prayed >= 5) Color.White else MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.home_today_times),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            trackedPrayers.forEach { prayer ->
                item(key = prayer.name) {
                    val key = "$today|${prayer.name}"
                    StatusRow(
                        prayer = prayer,
                        status = statuses[key] ?: SalahStatus.PENDING,
                        onStatus = { status -> vm.setSalahStatus(today, prayer, status) },
                    )
                }
            }

            item {
                Text(
                    text = stringResource(R.string.tracking_only_device),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun StatusRow(
    prayer: Prayer,
    status: SalahStatus,
    onStatus: (SalahStatus) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = prayerLabel(prayer),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
        )
        StatusButton(
            selected = status == SalahStatus.PRAYED,
            icon = Icons.Default.Check,
            description = stringResource(R.string.tracking_mark_prayed),
            tint = MaterialTheme.colorScheme.primary,
            onClick = { onStatus(if (status == SalahStatus.PRAYED) SalahStatus.PENDING else SalahStatus.PRAYED) },
        )
        Spacer(modifier = Modifier.width(4.dp))
        StatusButton(
            selected = status == SalahStatus.QADA,
            icon = Icons.Default.Replay,
            description = stringResource(R.string.tracking_mark_qada),
            tint = MaterialTheme.colorScheme.secondary,
            onClick = { onStatus(if (status == SalahStatus.QADA) SalahStatus.PENDING else SalahStatus.QADA) },
        )
        Spacer(modifier = Modifier.width(4.dp))
        StatusButton(
            selected = status == SalahStatus.MISSED,
            icon = Icons.Default.Close,
            description = stringResource(R.string.tracking_mark_missed),
            tint = MaterialTheme.colorScheme.error,
            onClick = { onStatus(if (status == SalahStatus.MISSED) SalahStatus.PENDING else SalahStatus.MISSED) },
        )
    }
}

@Composable
private fun StatusButton(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (selected) tint.copy(alpha = 0.18f) else Color.Transparent),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(38.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                tint = if (selected) tint else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun rememberStreak(
    statuses: Map<String, SalahStatus>,
    today: LocalDate,
): Int {
    val complete = { date: LocalDate ->
        trackedPrayers.all { prayer ->
            val status = statuses["$date|${prayer.name}"]
            status == SalahStatus.PRAYED || status == SalahStatus.QADA
        }
    }
    var date = if (complete(today)) today else today.minusDays(1)
    var count = 0
    while (count < 3650 && complete(date)) {
        count++
        date = date.minusDays(1)
    }
    return count
}
