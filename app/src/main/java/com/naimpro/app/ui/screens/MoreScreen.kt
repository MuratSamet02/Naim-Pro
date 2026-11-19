package com.naimpro.app.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naimpro.app.BuildConfig
import com.naimpro.app.R
import com.naimpro.app.ui.AppViewModel
import com.naimpro.app.ui.components.NaimMark
import com.naimpro.app.ui.components.SectionTitle
import com.naimpro.app.ui.components.VerticalGap

@Composable
fun MoreScreen(
    vm: AppViewModel,
    onOpenTracking: () -> Unit,
    onOpenZikir: () -> Unit,
    onOpenContent: () -> Unit,
    onOpenEsma: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    val state by vm.home.collectAsStateWithLifecycle()
    val settings = state.settings

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                NaimMark(size = 56.dp)
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.app_tagline),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            VerticalGap(4.dp)
            SectionTitle(stringResource(R.string.settings_general))
        }

        item {
            MenuEntry(
                icon = Icons.Default.Settings,
                title = stringResource(R.string.more_settings),
                subtitle =
                    stringResource(R.string.settings_calculation) + " · " +
                        stringResource(R.string.settings_notifications),
                onClick = onOpenSettings,
            )
        }
        item {
            MenuEntry(
                icon = Icons.Outlined.TrackChanges,
                title = stringResource(R.string.more_tracking),
                subtitle = stringResource(R.string.tracking_only_device),
                onClick = onOpenTracking,
            )
        }
        item {
            MenuEntry(
                icon = Icons.Outlined.Spa,
                title = stringResource(R.string.more_zikir),
                subtitle = stringResource(R.string.zikir_total) + ": ${vm.zikirTotal.collectAsStateWithLifecycle().value}",
                onClick = onOpenZikir,
            )
        }

        item { SectionTitle(stringResource(R.string.content_title)) }

        item {
            MenuEntry(
                icon = Icons.Outlined.AutoStories,
                title = stringResource(R.string.more_content),
                subtitle =
                    stringResource(R.string.content_dua) + " · " +
                        stringResource(R.string.content_ayet) + " · " +
                        stringResource(R.string.content_hadis),
                onClick = onOpenContent,
            )
        }
        item {
            MenuEntry(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                title = stringResource(R.string.more_esma),
                subtitle = "Esmâ-i Hüsnâ · 99",
                onClick = onOpenEsma,
            )
        }
        item {
            MenuEntry(
                icon = Icons.Outlined.AutoStories,
                title = stringResource(R.string.nav_calendar),
                subtitle = stringResource(R.string.calendar_upcoming),
                onClick = onOpenCalendar,
            )
        }

        item { SectionTitle(stringResource(R.string.more_widgets)) }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    ),
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Widgets,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.more_widgets_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }

        item {
            MenuEntry(
                icon = Icons.Default.Info,
                title = stringResource(R.string.more_about),
                subtitle =
                    stringResource(R.string.settings_about_version, BuildConfig.VERSION_NAME) +
                        " · " + settings.language.tag.uppercase(),
                onClick = onOpenAbout,
            )
        }

        item {
            VerticalGap(8.dp)
            Text(
                text = "GPL-3.0 · no ads · no trackers · offline first",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun MenuEntry(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
