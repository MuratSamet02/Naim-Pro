package com.naimpro.app.ui.screens

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Explore
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naimpro.app.R
import com.naimpro.app.domain.prayer.QiblaCalculator
import com.naimpro.app.ui.AppViewModel
import com.naimpro.app.ui.components.NaimMark
import com.naimpro.app.ui.sensors.rememberCompassReading
import com.naimpro.app.ui.theme.BrandDeep
import com.naimpro.app.ui.theme.BrandGold
import com.naimpro.app.ui.theme.BrandGoldLight
import com.naimpro.app.ui.theme.BrandTeal
import com.naimpro.app.ui.util.formatDecimal
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen(
    vm: AppViewModel,
    onBack: () -> Unit,
) {
    val state by vm.home.collectAsStateWithLifecycle()
    val location = state.location
    val reading = rememberCompassReading()

    val qibla = location?.let { QiblaCalculator.bearingFrom(it.latitude, it.longitude) }
    val distance = location?.let { QiblaCalculator.distanceKm(it.latitude, it.longitude) }
    val azimuth = reading.azimuth
    val aligned = qibla != null && azimuth != null && QiblaCalculator.isAligned(azimuth, qibla)
    val offset = if (qibla != null && azimuth != null) QiblaCalculator.offsetFrom(azimuth, qibla) else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.qibla_title)) },
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
                    .padding(padding)
                    .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            if (!reading.hasSensor) {
                Text(
                    text = stringResource(R.string.qibla_no_sensor),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (location == null) {
                Text(
                    text = stringResource(R.string.qibla_permission_needed),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            CompassDial(
                azimuth = azimuth,
                qibla = qibla,
                aligned = aligned,
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text =
                    when {
                        aligned -> stringResource(R.string.qibla_aligned)
                        offset == null -> stringResource(R.string.qibla_permission_needed)
                        offset > 0 -> stringResource(R.string.qibla_turn_right)
                        else -> stringResource(R.string.qibla_turn_left)
                    },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (aligned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = qibla?.let { stringResource(R.string.qibla_degrees, formatDecimal(it, 1)) } ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(6.dp))
            if (distance != null) {
                Text(
                    text = stringResource(R.string.qibla_distance, formatDecimal(distance, 0)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            if (reading.accuracy == android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                        ),
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        Text(
                            text = stringResource(R.string.qibla_accuracy_low),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = location?.displayName.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 14.dp),
            )
        }
    }
}

@Composable
private fun CompassDial(
    azimuth: Float?,
    qibla: Double?,
    aligned: Boolean,
) {
    Box(
        modifier =
            Modifier
                .size(300.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(listOf(BrandTeal.copy(alpha = 0.18f), BrandDeep.copy(alpha = 0.06f))),
                ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f - 12f
            val az = azimuth ?: 0f

            drawCircle(
                color = BrandGold.copy(alpha = 0.35f),
                radius = radius,
                center = center,
                style = Stroke(width = 3f),
            )
            drawCircle(
                color = BrandDeep.copy(alpha = 0.10f),
                radius = radius * 0.72f,
                center = center,
                style = Stroke(width = 2f),
            )

            rotate(degrees = -az, pivot = center) {
                for (tick in 0 until 72) {
                    val angle = tick * 5f
                    val major = tick % 9 == 0
                    val length = if (major) radius * 0.10f else radius * 0.05f
                    val startRadius = radius
                    val rad = Math.toRadians(angle.toDouble() - 90.0)
                    val start =
                        Offset(
                            center.x + (cos(rad) * startRadius).toFloat(),
                            center.y + (sin(rad) * startRadius).toFloat(),
                        )
                    val end =
                        Offset(
                            center.x + (cos(rad) * (startRadius - length)).toFloat(),
                            center.y + (sin(rad) * (startRadius - length)).toFloat(),
                        )
                    drawLine(
                        color = if (major) BrandGold else BrandDeep.copy(alpha = 0.35f),
                        start = start,
                        end = end,
                        strokeWidth = if (major) 4f else 2f,
                    )
                }

                val labelPaint =
                    Paint().apply {
                        isAntiAlias = true
                        textSize = 34f
                        color = android.graphics.Color.parseColor("#0B2A2E")
                        textAlign = Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                val labels = listOf("N" to 0f, "E" to 90f, "S" to 180f, "W" to 270f)
                labels.forEach { (label, angle) ->
                    val rad = Math.toRadians(angle.toDouble() - 90.0)
                    val pointRadius = radius - 46f
                    val x = center.x + (cos(rad) * pointRadius).toFloat()
                    val y = center.y + (sin(rad) * pointRadius).toFloat() + 12f
                    drawContext.canvas.nativeCanvas.drawText(label, x, y, labelPaint)
                }

                if (qibla != null) {
                    rotate(degrees = qibla.toFloat(), pivot = center) {
                        val needle =
                            Path().apply {
                                moveTo(center.x, center.y - radius * 0.86f)
                                lineTo(center.x - 16f, center.y - radius * 0.58f)
                                lineTo(center.x + 16f, center.y - radius * 0.58f)
                                close()
                            }
                        drawPath(
                            path = needle,
                            color = if (aligned) Color(0xFF2E7D32) else BrandGold,
                        )
                        drawLine(
                            color = if (aligned) Color(0xFF2E7D32) else BrandGold,
                            start = center,
                            end = Offset(center.x, center.y - radius * 0.62f),
                            strokeWidth = 7f,
                        )
                    }
                }
            }

            // Fixed "you are looking here" marker at the top of the screen.
            drawCircle(color = BrandGoldLight, radius = 7f, center = Offset(center.x, 18f))

            drawCircle(color = BrandDeep, radius = 12f, center = center)
            drawCircle(
                color = BrandGold,
                radius = 7f,
                center = center,
            )
        }
        Box(modifier = Modifier.size(180.dp), contentAlignment = Alignment.Center) {
            NaimMark(size = 54.dp)
        }
        Text(
            text = azimuth?.let { "${formatDecimal(it.toDouble(), 0)}°" } ?: "—",
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 66.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
