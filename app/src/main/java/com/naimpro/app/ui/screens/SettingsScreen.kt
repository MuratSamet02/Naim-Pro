package com.naimpro.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naimpro.app.R
import com.naimpro.app.alarm.AlarmPrefs
import com.naimpro.app.alarm.CamiModeController
import com.naimpro.app.alarm.EzanPlayer
import com.naimpro.app.data.prefs.AppLanguage
import com.naimpro.app.data.prefs.CamiModeFilter
import com.naimpro.app.data.prefs.NotificationMode
import com.naimpro.app.data.prefs.ThemeMode
import com.naimpro.app.domain.model.Prayer
import com.naimpro.app.domain.prayer.AsrMadhab
import com.naimpro.app.domain.prayer.CalculationMethod
import com.naimpro.app.domain.prayer.HighLatitudeRule
import com.naimpro.app.ui.AppViewModel
import com.naimpro.app.ui.components.SectionTitle
import com.naimpro.app.ui.components.SwitchRow
import com.naimpro.app.ui.components.VerticalGap
import com.naimpro.app.ui.components.highLatLabel
import com.naimpro.app.ui.components.madhabLabel
import com.naimpro.app.ui.components.methodLabel
import com.naimpro.app.ui.components.prayerLabel
import com.naimpro.app.ui.util.applyLanguage
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    vm: AppViewModel,
    onBack: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var methodDialog by remember { mutableStateOf(false) }
    var madhabDialog by remember { mutableStateOf(false) }
    var highLatDialog by remember { mutableStateOf(false) }
    var themeDialog by remember { mutableStateOf(false) }
    var languageDialog by remember { mutableStateOf(false) }
    var correctionsExpanded by remember { mutableStateOf(false) }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted ->
            if (!granted) {
                Toast.makeText(context, R.string.settings_permission_missing, Toast.LENGTH_SHORT).show()
            }
        }

    val exportLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/json"),
        ) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            scope.launch {
                val result = vm.exportBackup(uri)
                Toast.makeText(
                    context,
                    if (result.isSuccess) R.string.settings_backup_export_done else R.string.settings_backup_error,
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }

    val importLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument(),
        ) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            scope.launch {
                val result = vm.importBackup(uri)
                Toast.makeText(
                    context,
                    if (result.isSuccess) R.string.settings_backup_import_done else R.string.settings_backup_error,
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }

    var soundTarget by remember { mutableStateOf(Prayer.IMSAK) }
    var soundVersion by remember { mutableStateOf(0) }

    val soundLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument(),
        ) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            AlarmPrefs(context).setEzanUri(soundTarget, uri.toString())
            soundVersion++
            Toast.makeText(context, R.string.settings_backup_export_done, Toast.LENGTH_SHORT).show()
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
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
            item { SectionTitle(stringResource(R.string.settings_calculation)) }

            item {
                SettingsCard {
                    ClickRow(
                        title = stringResource(R.string.settings_method),
                        value = stringResource(methodLabel(settings.method)),
                        onClick = { methodDialog = true },
                    )
                    ClickRow(
                        title = stringResource(R.string.settings_madhab),
                        value = stringResource(madhabLabel(settings.madhab)),
                        onClick = { madhabDialog = true },
                    )
                    ClickRow(
                        title = stringResource(R.string.settings_high_latitude),
                        value = stringResource(highLatLabel(settings.highLatitudeRule)),
                        onClick = { highLatDialog = true },
                    )
                    ClickRow(
                        title = stringResource(R.string.settings_corrections),
                        value = if (correctionsExpanded) "−" else "+",
                        onClick = { correctionsExpanded = !correctionsExpanded },
                    )
                    if (correctionsExpanded) {
                        Text(
                            text = stringResource(R.string.settings_corrections_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Prayer.ALL.forEach { prayer ->
                            val value = settings.corrections[prayer] ?: 0
                            Column(modifier = Modifier.padding(top = 6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = prayerLabel(prayer),
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Text(
                                        text = stringResource(R.string.settings_minutes_value, value),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                Slider(
                                    value = value.toFloat(),
                                    onValueChange = { raw ->
                                        val minutes = raw.roundToInt()
                                        vm.updateSettings { current ->
                                            val next = current.corrections.toMutableMap()
                                            if (minutes == 0) next.remove(prayer) else next[prayer] = minutes
                                            current.copy(corrections = next)
                                        }
                                    },
                                    valueRange = -30f..30f,
                                    steps = 59,
                                )
                            }
                        }
                    }
                }
            }

            item { SectionTitle(stringResource(R.string.settings_notifications)) }

            item {
                SettingsCard {
                    SwitchRow(
                        title = stringResource(R.string.settings_notifications_enabled),
                        checked = settings.notificationEnabled,
                        onCheckedChange = { enabled ->
                            vm.updateSettings { it.copy(notificationEnabled = enabled) }
                        },
                    )
                    Prayer.ALL.forEach { prayer ->
                        ClickRow(
                            title = prayerLabel(prayer),
                            value = stringResource(modeLabel(settings.notificationModes[prayer] ?: NotificationMode.SILENT)),
                            onClick = {
                                val current = settings.notificationModes[prayer] ?: NotificationMode.SILENT
                                val next = NotificationMode.entries[(current.ordinal + 1) % NotificationMode.entries.size]
                                vm.updateSettings { s ->
                                    s.copy(notificationModes = s.notificationModes + (prayer to next))
                                }
                            },
                        )
                    }
                    SwitchRow(
                        title = stringResource(R.string.settings_pre_reminder),
                        subtitle = stringResource(R.string.settings_pre_reminder_minutes, settings.preReminderMinutes),
                        checked = settings.preReminderEnabled,
                        onCheckedChange = { enabled ->
                            vm.updateSettings { it.copy(preReminderEnabled = enabled) }
                        },
                    )
                    if (settings.preReminderEnabled) {
                        Slider(
                            value = settings.preReminderMinutes.toFloat(),
                            onValueChange = { raw ->
                                vm.updateSettings { it.copy(preReminderMinutes = raw.roundToInt()) }
                            },
                            valueRange = 5f..30f,
                            steps = 24,
                        )
                    }
                    Text(
                        text = stringResource(R.string.settings_ezan_volume) + ": %${settings.ezanVolumePercent}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Slider(
                        value = settings.ezanVolumePercent.toFloat(),
                        onValueChange = { raw ->
                            vm.updateSettings { it.copy(ezanVolumePercent = raw.roundToInt()) }
                        },
                        valueRange = 10f..100f,
                        steps = 17,
                    )
                    SwitchRow(
                        title = stringResource(R.string.settings_vibration),
                        checked = settings.vibrationEnabled,
                        onCheckedChange = { enabled ->
                            vm.updateSettings { it.copy(vibrationEnabled = enabled) }
                        },
                    )
                    ClickRow(
                        title = stringResource(R.string.settings_custom_sound),
                        value = stringResource(R.string.settings_custom_sound_pick),
                        onClick = { soundLauncher.launch(arrayOf("audio/*")) },
                    )
                    ClickRow(
                        title = stringResource(R.string.settings_custom_sound_clear),
                        value = "",
                        onClick = {
                            Prayer.ALL.forEach { prayer -> AlarmPrefs(context).setEzanUri(prayer, null) }
                            AlarmPrefs(context).customEzanUri = null
                            soundVersion++
                        },
                    )
                    Text(
                        text = stringResource(R.string.settings_ezan_per_prayer),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Text(
                        text = stringResource(R.string.settings_ezan_per_prayer_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Prayer.ALL.forEach { prayer ->
                        val prefs = remember { AlarmPrefs(context) }
                        val hasCustom = remember(prefs, prayer, soundVersion) { prefs.hasCustomEzanFor(prayer) }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        ) {
                            ClickRow(
                                title = prayerLabel(prayer),
                                value =
                                    if (hasCustom) {
                                        stringResource(R.string.settings_custom_sound_pick)
                                    } else {
                                        stringResource(R.string.settings_ezan_default)
                                    },
                                onClick = {
                                    soundTarget = prayer
                                    soundLauncher.launch(arrayOf("audio/*"))
                                },
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(
                                onClick = {
                                    EzanPlayer.preview(
                                        context,
                                        AlarmPrefs(context).ezanUriFor(prayer),
                                        settings.ezanVolumePercent,
                                    )
                                },
                            ) {
                                Text(stringResource(R.string.settings_ezan_preview))
                            }
                        }
                    }
                    SwitchRow(
                        title = stringResource(R.string.settings_ongoing_notification),
                        subtitle = stringResource(R.string.settings_ongoing_notification_desc),
                        checked = settings.ongoingNotificationEnabled,
                        onCheckedChange = { enabled ->
                            vm.updateSettings { it.copy(ongoingNotificationEnabled = enabled) }
                        },
                    )
                    SwitchRow(
                        title = stringResource(R.string.settings_cami_mode),
                        subtitle = stringResource(R.string.settings_cami_mode_desc),
                        checked = settings.camiModeEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && !CamiModeController.canControlDnd(context)) {
                                openDndSettings(context)
                            }
                            vm.updateSettings { it.copy(camiModeEnabled = enabled) }
                        },
                    )
                    if (settings.camiModeEnabled) {
                        Text(
                            text =
                                stringResource(
                                    R.string.settings_cami_mode_minutes,
                                    settings.camiModeMinutes,
                                ),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        Slider(
                            value = settings.camiModeMinutes.toFloat(),
                            onValueChange = { raw ->
                                vm.updateSettings {
                                    it.copy(camiModeMinutes = raw.roundToInt().coerceIn(5, 60))
                                }
                            },
                            valueRange = 5f..60f,
                            steps = 10,
                        )
                        ClickRow(
                            title = stringResource(R.string.settings_cami_mode_filter),
                            value =
                                stringResource(
                                    if (settings.camiModeFilter == CamiModeFilter.ALARMS) {
                                        R.string.settings_cami_filter_alarms
                                    } else {
                                        R.string.settings_cami_filter_priority
                                    },
                                ),
                            onClick = {
                                val next =
                                    if (settings.camiModeFilter == CamiModeFilter.ALARMS) {
                                        CamiModeFilter.PRIORITY
                                    } else {
                                        CamiModeFilter.ALARMS
                                    }
                                vm.updateSettings { it.copy(camiModeFilter = next) }
                            },
                        )
                    }
                }
            }

            item { SectionTitle(stringResource(R.string.settings_theme)) }

            item {
                SettingsCard {
                    ClickRow(
                        title = stringResource(R.string.settings_theme_mode),
                        value = stringResource(themeLabel(settings.themeMode)),
                        onClick = { themeDialog = true },
                    )
                    SwitchRow(
                        title = stringResource(R.string.settings_dynamic_color),
                        checked = settings.dynamicColor,
                        onCheckedChange = { enabled ->
                            vm.updateSettings { it.copy(dynamicColor = enabled) }
                        },
                    )
                    ClickRow(
                        title = stringResource(R.string.settings_language),
                        value = languageLabel(settings.language),
                        onClick = { languageDialog = true },
                    )
                    Text(
                        text = stringResource(R.string.settings_font_scale),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    Slider(
                        value = settings.fontScale,
                        onValueChange = { scale ->
                            vm.updateSettings { it.copy(fontScale = scale) }
                        },
                        valueRange = 0.85f..1.5f,
                    )
                }
            }

            item { SectionTitle(stringResource(R.string.settings_permissions)) }

            item {
                SettingsCard {
                    PermissionRow(
                        title = stringResource(R.string.settings_notification_permission),
                        granted = NotificationManagerCompat.from(context).areNotificationsEnabled(),
                        explain = stringResource(R.string.settings_permission_explain_notification),
                        onClick = { openNotificationSettings(context) },
                    )
                    PermissionRow(
                        title = stringResource(R.string.settings_exact_alarm_permission),
                        granted = canScheduleExactAlarms(context),
                        explain = stringResource(R.string.settings_permission_explain_alarm),
                        onClick = { openExactAlarmSettings(context) },
                    )
                    PermissionRow(
                        title = stringResource(R.string.settings_location_permission),
                        granted = hasCoarseLocation(context),
                        explain = stringResource(R.string.settings_permission_explain_location),
                        onClick = {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                        },
                    )
                    PermissionRow(
                        title = stringResource(R.string.settings_dnd_permission),
                        granted = CamiModeController.canControlDnd(context),
                        explain = stringResource(R.string.settings_permission_explain_dnd),
                        onClick = { openDndSettings(context) },
                    )
                    PermissionRow(
                        title = stringResource(R.string.settings_battery),
                        granted = isIgnoringBatteryOptimizations(context),
                        explain = stringResource(R.string.settings_battery_desc),
                        onClick = { openBatterySettings(context) },
                    )
                }
            }

            item { SectionTitle(stringResource(R.string.settings_backup)) }

            item {
                SettingsCard {
                    ClickRow(
                        title = stringResource(R.string.settings_backup_export),
                        value = "",
                        onClick = { exportLauncher.launch("naim-pro-backup.json") },
                    )
                    ClickRow(
                        title = stringResource(R.string.settings_backup_import),
                        value = "",
                        onClick = { importLauncher.launch(arrayOf("application/json", "text/plain")) },
                    )
                }
            }

            item {
                SettingsCard {
                    ClickRow(
                        title = stringResource(R.string.settings_about),
                        value = stringResource(R.string.settings_about_version, com.naimpro.app.BuildConfig.VERSION_NAME),
                        onClick = onOpenAbout,
                    )
                    Text(
                        text = stringResource(R.string.settings_about_privacy) + " · GPL-3.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }

            item { VerticalGap(16.dp) }
        }
    }

    if (methodDialog) {
        ChoiceDialog(
            title = stringResource(R.string.settings_method),
            options = CalculationMethod.entries.map { it to stringResource(methodLabel(it)) },
            selected = settings.method,
            onDismiss = { methodDialog = false },
            onSelect = { method ->
                vm.updateSettings { it.copy(method = method) }
                methodDialog = false
            },
        )
    }
    if (madhabDialog) {
        ChoiceDialog(
            title = stringResource(R.string.settings_madhab),
            options = AsrMadhab.entries.map { it to stringResource(madhabLabel(it)) },
            selected = settings.madhab,
            onDismiss = { madhabDialog = false },
            onSelect = { madhab ->
                vm.updateSettings { it.copy(madhab = madhab) }
                madhabDialog = false
            },
        )
    }
    if (highLatDialog) {
        ChoiceDialog(
            title = stringResource(R.string.settings_high_latitude),
            options = HighLatitudeRule.entries.map { it to stringResource(highLatLabel(it)) },
            selected = settings.highLatitudeRule,
            onDismiss = { highLatDialog = false },
            onSelect = { rule ->
                vm.updateSettings { it.copy(highLatitudeRule = rule) }
                highLatDialog = false
            },
        )
    }
    if (themeDialog) {
        ChoiceDialog(
            title = stringResource(R.string.settings_theme_mode),
            options = ThemeMode.entries.map { it to stringResource(themeLabel(it)) },
            selected = settings.themeMode,
            onDismiss = { themeDialog = false },
            onSelect = { mode ->
                vm.updateSettings { it.copy(themeMode = mode) }
                themeDialog = false
            },
        )
    }
    if (languageDialog) {
        ChoiceDialog(
            title = stringResource(R.string.settings_language),
            options = AppLanguage.entries.map { it to languageLabel(it) },
            selected = settings.language,
            onDismiss = { languageDialog = false },
            onSelect = { language ->
                vm.updateSettings { it.copy(language = language) }
                applyLanguage(context, language.tag)
                languageDialog = false
            },
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            ),
    ) {
        Column(modifier = Modifier.padding(14.dp)) { content() }
    }
}

@Composable
private fun ClickRow(
    title: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Start,
            )
            if (value.isNotBlank()) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    granted: Boolean,
    explain: String,
    onClick: () -> Unit,
) {
    Column {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = explain,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            TextButton(onClick = onClick) {
                Text(
                    text =
                        stringResource(
                            if (granted) {
                                R.string.settings_permission_granted
                            } else {
                                R.string.settings_permission_missing
                            },
                        ),
                    style = MaterialTheme.typography.labelMedium,
                    color =
                        if (granted) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                )
            }
        }
    }
}

@Composable
private fun <T> ChoiceDialog(
    title: String,
    options: List<Pair<T, String>>,
    selected: T,
    onDismiss: () -> Unit,
    onSelect: (T) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (value, label) ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = value == selected,
                            onClick = { onSelect(value) },
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_close))
            }
        },
    )
}

@Composable
private fun themeLabel(mode: ThemeMode): Int =
    when (mode) {
        ThemeMode.SYSTEM -> R.string.settings_theme_system
        ThemeMode.LIGHT -> R.string.settings_theme_light
        ThemeMode.DARK -> R.string.settings_theme_dark
        ThemeMode.AMOLED -> R.string.settings_theme_amoled
    }

@Composable
private fun modeLabel(mode: NotificationMode): Int =
    when (mode) {
        NotificationMode.EZAN -> R.string.mode_ezan
        NotificationMode.SHORT_ALERT -> R.string.mode_short_alert
        NotificationMode.VIBRATE -> R.string.mode_vibrate
        NotificationMode.SILENT -> R.string.mode_silent
    }

private fun languageLabel(language: AppLanguage): String =
    when (language) {
        AppLanguage.TURKISH -> "Türkçe"
        AppLanguage.ENGLISH -> "English"
        AppLanguage.ARABIC -> "العربية"
    }

private fun hasCoarseLocation(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        android.content.pm.PackageManager.PERMISSION_GRANTED

private fun canScheduleExactAlarms(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
    val manager = context.getSystemService(android.app.AlarmManager::class.java) ?: return false
    return manager.canScheduleExactAlarms()
}

private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
    val power = context.getSystemService(PowerManager::class.java) ?: return false
    return power.isIgnoringBatteryOptimizations(context.packageName)
}

private fun openNotificationSettings(context: Context) {
    val intent =
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    runCatching { context.startActivity(intent) }
}

private fun openExactAlarmSettings(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    val intent =
        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    runCatching { context.startActivity(intent) }
}

private fun openDndSettings(context: Context) {
    val intent =
        Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    runCatching { context.startActivity(intent) }
}

private fun openBatterySettings(context: Context) {
    val intent =
        Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    runCatching { context.startActivity(intent) }
}
