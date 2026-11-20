package com.naimpro.app.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naimpro.app.R
import com.naimpro.app.domain.hijri.HijriCalendar
import com.naimpro.app.domain.model.PrayerTimes
import com.naimpro.app.ui.AppViewModel
import com.naimpro.app.ui.util.MonthlyExporter
import com.naimpro.app.ui.util.TableLabels
import com.naimpro.app.ui.util.clock
import com.naimpro.app.ui.util.shareFile
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyScreen(
    vm: AppViewModel,
    onBack: () -> Unit,
) {
    val state by vm.home.collectAsStateWithLifecycle()
    val settings = state.settings
    val location = state.location
    val language = settings.language.tag
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val shareTitle = stringResource(R.string.monthly_share_title)

    var month by remember { mutableStateOf(YearMonth.now()) }
    var days by remember { mutableStateOf<List<PrayerTimes>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(month, location, settings.method, settings.madhab, settings.corrections) {
        loading = true
        days = runCatching { vm.monthTimes(month.year, month.monthValue) }.getOrDefault(emptyList())
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.monthly_title)) },
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
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { month = month.minusMonths(1) }) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = stringResource(R.string.monthly_previous),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text =
                            month.format(
                                DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag(language)),
                            ),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = location?.displayName.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { month = month.plusMonths(1) }) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = stringResource(R.string.monthly_next),
                    )
                }
            }

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val currentLocation = location ?: return@OutlinedButton
                        scope.launch {
                            runCatching {
                                val file = MonthlyExporter.pdf(context, currentLocation, days, language)
                                shareFile(context, file, "application/pdf", shareTitle)
                            }.onFailure {
                                Toast.makeText(context, R.string.monthly_export_error, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.monthly_export_pdf), style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val currentLocation = location ?: return@OutlinedButton
                        scope.launch {
                            runCatching {
                                val file = MonthlyExporter.csv(context, currentLocation, days, language)
                                shareFile(context, file, "text/csv", shareTitle)
                            }.onFailure {
                                Toast.makeText(context, R.string.monthly_export_error, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.monthly_export_csv), style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    item { TableHeader(language = language) }
                    items(days, key = { it.date.toString() }) { day ->
                        TableRow(day = day, language = language)
                    }
                }
            }
        }
    }
}

@Composable
private fun TableHeader(language: String) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 6.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeaderCell(stringResource(R.string.monthly_day), weight = 0.8f)
        HeaderCell(stringResource(R.string.monthly_hijri), weight = 1.2f)
        listOf(
            "prayer_imsak",
            "prayer_gunes",
            "prayer_ogle",
            "prayer_ikindi",
            "prayer_aksam",
            "prayer_yatsi",
        ).forEach { key ->
            HeaderCell(headerLabel(key, language), weight = 1f)
        }
    }
}

private fun headerLabel(
    key: String,
    language: String,
): String = TableLabels.of(key, language)

@Composable
private fun androidx.compose.foundation.layout.RowScope.HeaderCell(
    text: String,
    weight: Float,
) {
    Text(
        text = text,
        modifier = Modifier.weight(weight),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onPrimary,
        textAlign = TextAlign.Center,
        maxLines = 1,
    )
}

@Composable
private fun TableRow(
    day: PrayerTimes,
    language: String,
) {
    val hijri = HijriCalendar.fromGregorian(day.date)
    val isEven = day.date.dayOfMonth % 2 == 0
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isEven) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                )
                .padding(horizontal = 6.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BodyCell(day.date.dayOfMonth.toString(), weight = 0.8f, bold = true)
        BodyCell("${hijri.day}.${hijri.month}", weight = 1.2f)
        BodyCell(day.imsak.clock(), weight = 1f)
        BodyCell(day.gunes.clock(), weight = 1f)
        BodyCell(day.ogle.clock(), weight = 1f)
        BodyCell(day.ikindi.clock(), weight = 1f)
        BodyCell(day.aksam.clock(), weight = 1f)
        BodyCell(day.yatsi.clock(), weight = 1f)
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.BodyCell(
    text: String,
    weight: Float,
    bold: Boolean = false,
) {
    Text(
        text = text,
        modifier = Modifier.weight(weight),
        style = MaterialTheme.typography.bodySmall,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        textAlign = TextAlign.Center,
        maxLines = 1,
    )
}
