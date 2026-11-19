package com.naimpro.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.naimpro.app.R
import com.naimpro.app.domain.model.Prayer
import com.naimpro.app.ui.theme.BrandDeep
import com.naimpro.app.ui.theme.BrandGold
import com.naimpro.app.ui.theme.BrandGoldLight
import com.naimpro.app.ui.theme.BrandTeal

/** Localised label for a prayer. */
@Composable
fun prayerLabel(prayer: Prayer): String =
    stringResource(
        when (prayer) {
            Prayer.IMSAK -> R.string.prayer_imsak
            Prayer.GUNES -> R.string.prayer_gunes
            Prayer.OGLE -> R.string.prayer_ogle
            Prayer.IKINDI -> R.string.prayer_ikindi
            Prayer.AKSAM -> R.string.prayer_aksam
            Prayer.YATSI -> R.string.prayer_yatsi
        },
    )

/** The Naim Pro mark, drawn with pure Compose so no image assets are needed. */
@Composable
fun NaimMark(
    size: Dp = 48.dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(listOf(BrandTeal, BrandDeep)),
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "☾",
            color = BrandGoldLight,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** Rounded surface used for every grouped block in the app. */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun SwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null,
    enabled: Boolean = true,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
fun Dot(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
    )
}

@Composable
fun VerticalGap(height: Dp = 12.dp) = Spacer(modifier = Modifier.height(height))

/** Accessible label helper for decorative elements. */
fun Modifier.decorative(): Modifier = clearAndSetSemantics { }

fun Modifier.describedAs(text: String): Modifier = this.then(Modifier.semanticsDescription(text))

private fun Modifier.semanticsDescription(text: String): Modifier =
    this.then(
        Modifier.clearAndSetSemantics { contentDescription = text },
    )

@Composable
fun KeyValueGrid(entries: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        entries.forEach { (label, value) ->
            InfoRow(label = label, value = value)
        }
    }
}

val GoldAccent = BrandGold
