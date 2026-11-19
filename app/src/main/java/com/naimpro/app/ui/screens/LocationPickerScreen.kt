package com.naimpro.app.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.naimpro.app.R
import com.naimpro.app.domain.model.Location
import com.naimpro.app.ui.AppViewModel
import com.naimpro.app.ui.components.SectionTitle
import com.naimpro.app.ui.util.LocationProvider
import kotlinx.coroutines.launch

/**
 * Search over the bundled GeoNames extract plus a one-shot coarse GPS lookup.
 * The copy next to the GPS button states exactly what is used and what is not.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerScreen(
    vm: AppViewModel,
    onBack: () -> Unit,
    onSelected: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf(vm.citySuggestions()) }
    var busy by remember { mutableStateOf(false) }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted ->
            if (!granted) {
                Toast.makeText(context, R.string.settings_permission_missing, Toast.LENGTH_SHORT).show()
                return@rememberLauncherForActivityResult
            }
            busy = true
            scope.launch {
                val resolved = vm.resolveCoarseLocation()
                busy = false
                if (resolved == null) {
                    Toast.makeText(context, R.string.location_empty, Toast.LENGTH_SHORT).show()
                } else {
                    vm.selectLocation(resolved)
                    onSelected()
                }
            }
        }

    fun pick(location: Location) {
        vm.selectLocation(location)
        onSelected()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.location_title)) },
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        ),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.location_use_gps),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.location_gps_explain),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            enabled = !busy,
                            onClick = {
                                if (LocationProvider.hasPermission(context)) {
                                    busy = true
                                    scope.launch {
                                        val resolved = vm.resolveCoarseLocation()
                                        busy = false
                                        if (resolved == null) {
                                            Toast.makeText(
                                                context,
                                                R.string.location_empty,
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                        } else {
                                            vm.selectLocation(resolved)
                                            onSelected()
                                        }
                                    }
                                } else {
                                    permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.location_use_gps))
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { text ->
                        query = text
                        results = vm.searchCities(text)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            TextButton(onClick = {
                                query = ""
                                results = vm.citySuggestions()
                            }) {
                                Text(stringResource(R.string.common_cancel))
                            }
                        }
                    },
                    label = { Text(stringResource(R.string.location_search_hint)) },
                )
            }

            item {
                SectionTitle(
                    text =
                        if (query.isBlank()) {
                            stringResource(R.string.location_turkey)
                        } else {
                            stringResource(R.string.location_world)
                        },
                )
            }

            if (results.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.location_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            items(results, key = { it.id }) { location ->
                LocationRow(location = location, onClick = { pick(location) })
            }
        }
    }
}

@Composable
private fun LocationRow(
    location: Location,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f))
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier =
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = location.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            val secondary =
                listOf(location.region, location.country)
                    .filter { it.isNotBlank() && !it.equals(location.name, ignoreCase = true) }
                    .distinct()
                    .joinToString(", ")
            if (secondary.isNotBlank()) {
                Text(
                    text = secondary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text = "${location.latitude.toInt()}° / ${location.longitude.toInt()}°",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
