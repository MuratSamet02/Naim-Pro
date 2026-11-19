package com.naimpro.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naimpro.app.R
import com.naimpro.app.domain.hijri.HijriCalendar
import com.naimpro.app.domain.hijri.IslamicEvent
import com.naimpro.app.ui.AppViewModel
import com.naimpro.app.ui.components.SectionCard
import com.naimpro.app.ui.components.SectionTitle
import com.naimpro.app.ui.components.SwitchRow
import com.naimpro.app.ui.components.VerticalGap
import com.naimpro.app.ui.util.formatDate
import com.naimpro.app.ui.util.formatWeekday
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    vm: AppViewModel,
    onBack: () -> Unit,
) {
    val state by vm.home.collectAsStateWithLifecycle()
    val settings = state.settings
    val language = settings.language.tag
    var events by remember { mutableStateOf<List<IslamicEvent>>(emptyList()) }

    LaunchedEffect(settings.hicriOffsetDays) {
        events = runCatching { vm.upcomingEvents(10) }.getOrDefault(emptyList())
    }

    val today = LocalDate.now()
    val hijriToday = HijriCalendar.fromGregorian(today, settings.hicriOffsetDays)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.calendar_title)) },
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
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        ),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = stringResource(R.string.calendar_hijri_today),
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${hijriToday.day} ${HijriCalendar.monthName(hijriToday.month, language)} ${hijriToday.year}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatDate(today, language),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item {
                SectionCard {
                    SectionTitle(stringResource(R.string.calendar_offset))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.calendar_offset_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (settings.hicriOffsetDays >= 0) "+${settings.hicriOffsetDays}" else "${settings.hicriOffsetDays}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Slider(
                        value = settings.hicriOffsetDays.toFloat(),
                        onValueChange = { raw ->
                            vm.updateSettings { it.copy(hicriOffsetDays = raw.roundToInt()) }
                        },
                        valueRange = -2f..2f,
                        steps = 3,
                    )
                    SwitchRow(
                        title = stringResource(R.string.calendar_notifications),
                        subtitle = stringResource(R.string.calendar_notifications_desc),
                        checked = settings.diniGunNotifications,
                        onCheckedChange = { enabled ->
                            vm.updateSettings { it.copy(diniGunNotifications = enabled) }
                        },
                    )
                }
            }

            item {
                SectionTitle(stringResource(R.string.calendar_upcoming), modifier = Modifier.padding(top = 4.dp))
            }

            items(events, key = { it.key + it.gregorian.toString() }) { event ->
                EventRow(event = event, language = language)
            }

            item { VerticalGap(16.dp) }
        }
    }
}

@Composable
private fun EventRow(
    event: IslamicEvent,
    language: String,
) {
    val daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), event.gregorian).toInt()
    val title = stringResource(eventTitleRes(event.key))
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier =
                Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = event.gregorian.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = formatWeekday(event.gregorian, language).take(3),
                style = MaterialTheme.typography.labelSmall,
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                text = formatDate(event.gregorian, language),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = if (daysUntil <= 0) "•" else "$daysUntil",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun eventTitleRes(key: String): Int =
    when (key) {
        "hijri_new_year" -> R.string.event_hijri_new_year
        "ashura" -> R.string.event_ashura
        "mawlid" -> R.string.event_mawlid
        "raghaib" -> R.string.event_raghaib
        "miraj" -> R.string.event_miraj
        "baraah" -> R.string.event_baraah
        "ramadan_start" -> R.string.event_ramadan_start
        "qadr_night" -> R.string.event_qadr_night
        "eid_al_fitr" -> R.string.event_eid_al_fitr
        "arafah" -> R.string.event_arafah
        else -> R.string.event_eid_al_adha
    }
