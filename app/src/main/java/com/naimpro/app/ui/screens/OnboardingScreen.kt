package com.naimpro.app.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naimpro.app.R
import com.naimpro.app.domain.model.Location
import com.naimpro.app.domain.prayer.CalculationMethod
import com.naimpro.app.ui.AppViewModel
import com.naimpro.app.ui.components.NaimMark
import com.naimpro.app.ui.components.SectionCard
import com.naimpro.app.ui.components.methodLabel
import com.naimpro.app.ui.design.NaimSpacing
import com.naimpro.app.ui.onboarding.OnboardingStep
import com.naimpro.app.ui.onboarding.canContinue
import com.naimpro.app.ui.theme.BrandGold

/**
 * First run flow: welcome, location, notifications, calculation method.
 *
 * The app used to silently fall back to Ankara on first start. Onboarding makes
 * the location an explicit choice, asks for the notification permission at the
 * moment it makes sense and lets the user pick a calculation method before the
 * first prayer time is ever shown.
 */
@Composable
fun OnboardingScreen(
    vm: AppViewModel,
    onDone: () -> Unit,
) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    var step by rememberSaveable { mutableStateOf(OnboardingStep.first()) }
    var selected by remember { mutableStateOf<Location?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var method by rememberSaveable { mutableStateOf(settings.method) }

    val results = remember(query) { if (query.length >= 2) vm.searchCities(query) else emptyList() }
    val popular = remember { vm.popularCities() }

    val notificationLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted ->
            vm.updateSettings { it.copy(notificationEnabled = granted) }
            step = OnboardingStep.METHOD
        }

    fun finish() {
        vm.completeOnboarding(
            location = selected,
            method = method,
        )
        onDone()
    }

    Scaffold(
        bottomBar = {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = NaimSpacing.gutter, vertical = NaimSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(NaimSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (step.previous() != null) {
                    TextButton(onClick = { step.previous()?.let { step = it } }) {
                        Text(stringResource(R.string.common_back))
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                if (step.isSkippable) {
                    TextButton(onClick = { step.next()?.let { step = it } }) {
                        Text(stringResource(R.string.onboarding_skip))
                    }
                }
                Button(
                    enabled = canContinue(step, selected != null),
                    onClick = {
                        when (step) {
                            OnboardingStep.NOTIFICATIONS -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    vm.updateSettings { it.copy(notificationEnabled = true) }
                                    step = OnboardingStep.METHOD
                                }
                            }
                            OnboardingStep.METHOD -> finish()
                            else -> step.next()?.let { step = it }
                        }
                    },
                ) {
                    Text(
                        stringResource(
                            if (step.isLast) R.string.onboarding_finish else R.string.onboarding_next,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = NaimSpacing.gutter),
        ) {
            StepIndicator(step = step)
            when (step) {
                OnboardingStep.WELCOME -> WelcomeStep()
                OnboardingStep.LOCATION ->
                    LocationStep(
                        query = query,
                        onQueryChange = { query = it },
                        results = results,
                        popular = popular,
                        selected = selected,
                        onSelect = { selected = it },
                        onUseCurrent = {
                            vm.useCurrentLocation { ok ->
                                if (ok) {
                                    selected = vm.location.value
                                    step = OnboardingStep.NOTIFICATIONS
                                }
                            }
                        },
                    )
                OnboardingStep.NOTIFICATIONS -> NotificationsStep()
                else ->
                    MethodStep(
                        method = method,
                        onSelect = { method = it },
                    )
            }
        }
    }
}

@Composable
private fun StepIndicator(step: OnboardingStep) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = NaimSpacing.lg, bottom = NaimSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(NaimSpacing.xs),
    ) {
        repeat(OnboardingStep.entries.size) { index ->
            Surface(
                modifier = Modifier.weight(1f).height(4.dp),
                shape = RoundedCornerShape(2.dp),
                color =
                    if (index <= step.ordinal) {
                        BrandGold
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
            ) {}
        }
    }
}

@Composable
private fun WelcomeStep() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = NaimSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NaimMark(size = 96.dp)
        Spacer(modifier = Modifier.height(NaimSpacing.xl))
        Text(
            text = stringResource(R.string.onboarding_welcome_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(NaimSpacing.sm))
        Text(
            text = stringResource(R.string.app_tagline),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(NaimSpacing.lg))
        Text(
            text = stringResource(R.string.onboarding_welcome_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun LocationStep(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<Location>,
    popular: List<Location>,
    selected: Location?,
    onSelect: (Location) -> Unit,
    onUseCurrent: () -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = NaimSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(NaimSpacing.sm),
    ) {
        item {
            Column {
                Text(
                    text = stringResource(R.string.onboarding_location_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(NaimSpacing.xs))
                Text(
                    text = stringResource(R.string.onboarding_location_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(NaimSpacing.md))
                OutlinedButton(
                    onClick = onUseCurrent,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(NaimSpacing.sm))
                    Text(stringResource(R.string.location_use_gps))
                }
                Spacer(modifier = Modifier.height(NaimSpacing.md))
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(stringResource(R.string.location_search_hint)) },
                )
                Spacer(modifier = Modifier.height(NaimSpacing.md))
                Text(
                    text = stringResource(R.string.onboarding_location_popular),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        val list = if (query.length >= 2) results else popular
        items(list, key = { "${it.latitude},${it.longitude},${it.displayName}" }) { location ->
            LocationRow(
                location = location,
                selected =
                    selected?.displayName == location.displayName &&
                        selected?.latitude == location.latitude,
                onClick = { onSelect(location) },
            )
        }
    }
}

@Composable
private fun LocationRow(
    location: Location,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(NaimSpacing.md),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(NaimSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = location.displayName, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = location.shortLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.onboarding_selected),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun NotificationsStep() {
    Column(modifier = Modifier.fillMaxWidth().padding(top = NaimSpacing.lg)) {
        Text(
            text = stringResource(R.string.onboarding_notifications_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(NaimSpacing.sm))
        Text(
            text = stringResource(R.string.onboarding_notifications_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(NaimSpacing.lg))
        SectionCard {
            Text(
                text = stringResource(R.string.onboarding_notifications_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MethodStep(
    method: CalculationMethod,
    onSelect: (CalculationMethod) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = NaimSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(NaimSpacing.xs),
    ) {
        item {
            Column {
                Text(
                    text = stringResource(R.string.onboarding_method_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(NaimSpacing.xs))
                Text(
                    text = stringResource(R.string.onboarding_method_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(NaimSpacing.md))
            }
        }
        items(CalculationMethod.entries.toList(), key = { it.name }) { entry ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = NaimSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = entry == method, onClick = { onSelect(entry) })
                Text(
                    text = stringResource(methodLabel(entry)),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = NaimSpacing.xs),
                )
            }
        }
    }
}
