package com.naimpro.app.ui.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.naimpro.app.domain.geo.MagneticDeclination

data class CompassReading(
    /** Heading relative to true north, already declination corrected. */
    val azimuth: Float? = null,
    /** Raw heading relative to magnetic north, for diagnostics. */
    val magneticAzimuth: Float? = null,
    val accuracy: Int = SensorManager.SENSOR_STATUS_UNRELIABLE,
    val hasSensor: Boolean = true,
)

/**
 * Rotation-vector based compass. Uses the fused rotation vector when available
 * and silently falls back to accelerometer + magnetometer.
 *
 * A phone reports the heading against magnetic north, while the qibla bearing is
 * computed against true north. [declinationDegrees] (east positive) and the
 * user's manual [manualOffsetDegrees] are therefore added before the value is
 * exposed, which removes the 4–6° error seen in Türkiye.
 */
@Composable
fun rememberCompassReading(
    declinationDegrees: Float = 0f,
    manualOffsetDegrees: Float = 0f,
): CompassReading {
    val context = LocalContext.current
    var reading by remember { mutableStateOf(CompassReading()) }
    val correction = declinationDegrees + manualOffsetDegrees

    DisposableEffect(context, correction) {
        val manager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        if (manager == null) {
            reading = CompassReading(hasSensor = false)
            return@DisposableEffect onDispose { }
        }

        val rotation = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (rotation == null && (accelerometer == null || magnetometer == null)) {
            reading = CompassReading(hasSensor = false)
            return@DisposableEffect onDispose { }
        }

        val matrix = FloatArray(9)
        val orientation = FloatArray(3)
        var gravity: FloatArray? = null
        var geomagnetic: FloatArray? = null

        fun publish(
            magnetic: Float,
            accuracy: Int,
        ) {
            reading =
                CompassReading(
                    azimuth = MagneticDeclination.correctedHeading(magnetic, correction),
                    magneticAzimuth = magnetic,
                    accuracy = accuracy,
                )
        }

        val listener =
            object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    when (event.sensor.type) {
                        Sensor.TYPE_ROTATION_VECTOR -> {
                            SensorManager.getRotationMatrixFromVector(matrix, event.values)
                            SensorManager.getOrientation(matrix, orientation)
                            publish(
                                normalize(Math.toDegrees(orientation[0].toDouble()).toFloat()),
                                SensorManager.SENSOR_STATUS_ACCURACY_HIGH,
                            )
                        }
                        Sensor.TYPE_ACCELEROMETER -> {
                            gravity = event.values.clone()
                        }
                        Sensor.TYPE_MAGNETIC_FIELD -> {
                            geomagnetic = event.values.clone()
                        }
                    }
                    val g = gravity
                    val m = geomagnetic
                    if (rotation == null && g != null && m != null) {
                        if (SensorManager.getRotationMatrix(matrix, null, g, m)) {
                            SensorManager.getOrientation(matrix, orientation)
                            publish(
                                normalize(Math.toDegrees(orientation[0].toDouble()).toFloat()),
                                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM,
                            )
                        }
                    }
                }

                override fun onAccuracyChanged(
                    sensor: Sensor?,
                    accuracy: Int,
                ) {
                    reading = reading.copy(accuracy = accuracy)
                }
            }

        val active = rotation ?: accelerometer
        if (active != null) {
            manager.registerListener(listener, active, SensorManager.SENSOR_DELAY_UI)
        }
        if (rotation == null) {
            magnetometer?.let { manager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        }

        onDispose { manager.unregisterListener(listener) }
    }

    return reading
}

private fun normalize(degrees: Float): Float {
    val value = degrees % 360f
    return if (value < 0) value + 360f else value
}
