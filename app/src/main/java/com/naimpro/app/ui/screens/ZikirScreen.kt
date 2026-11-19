package com.naimpro.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naimpro.app.R
import com.naimpro.app.data.repo.ZikirRepository
import com.naimpro.app.ui.AppViewModel
import com.naimpro.app.ui.components.SectionTitle
import com.naimpro.app.ui.components.SwitchRow
import com.naimpro.app.ui.theme.BrandDeep
import com.naimpro.app.ui.theme.BrandGold
import com.naimpro.app.ui.theme.BrandGoldLight
import com.naimpro.app.ui.theme.BrandTeal
import com.naimpro.app.ui.theme.BrandTealLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZikirScreen(
    vm: AppViewModel,
    onBack: () -> Unit,
) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val total by vm.zikirTotal.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current

    var phrase by remember { mutableStateOf(settings.lastZikir) }
    var target by remember { mutableIntStateOf(settings.lastZikirTarget) }
    var count by remember { mutableIntStateOf(0) }
    var vibrate by remember { mutableStateOf(true) }

    val completed = count >= target && target > 0

    fun increment() {
        count++
        if (vibrate) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        if (count == target) {
            vm.recordZikir(phrase, count, target, completed = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.zikir_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (count > 0) vm.recordZikir(phrase, count, target, completed)
                        count = 0
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.zikir_reset),
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        ),
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = phrase,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Box(
                            modifier =
                                Modifier
                                    .size(220.dp)
                                    .clip(CircleShape)
                                    .background(
                                        androidx.compose.ui.graphics.Brush.verticalGradient(
                                            listOf(BrandDeep, BrandTeal),
                                        ),
                                    )
                                    .clickable { increment() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = count.toString(),
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandGoldLight,
                                )
                                Text(
                                    text = "/ $target",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = BrandTealLight,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.zikir_target),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ColorWhite.copy(alpha = 0.7f),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        if (completed) {
                            Text(
                                text = stringResource(R.string.zikir_completed),
                                color = BrandGold,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        } else {
                            Text(
                                text = "${total + count} · ${stringResource(R.string.zikir_total)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { increment() }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (settings.language.tag == "tr") "Zikret" else "Count")
                        }
                    }
                }
            }

            item {
                SectionTitle(stringResource(R.string.zikir_custom))
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ZikirRepository.presets.take(3).forEach { preset ->
                        FilterChip(
                            selected = phrase == preset.first,
                            onClick = {
                                phrase = preset.first
                                target = preset.second
                            },
                            label = { Text(preset.first, style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ZikirRepository.presets.drop(3).forEach { preset ->
                        FilterChip(
                            selected = phrase == preset.first,
                            onClick = {
                                phrase = preset.first
                                target = preset.second
                            },
                            label = { Text(preset.first, style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = phrase,
                    onValueChange = { phrase = it.take(60) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.zikir_custom)) },
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = target.toString(),
                    onValueChange = { raw ->
                        target = raw.filter { it.isDigit() }.take(4).toIntOrNull() ?: 0
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.zikir_target)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                Spacer(modifier = Modifier.height(4.dp))
                SwitchRow(
                    title = stringResource(R.string.zikir_vibrate),
                    checked = vibrate,
                    onCheckedChange = { vibrate = it },
                )
            }
        }
    }
}

private val ColorWhite = androidx.compose.ui.graphics.Color(0xFFFFFFFF)
