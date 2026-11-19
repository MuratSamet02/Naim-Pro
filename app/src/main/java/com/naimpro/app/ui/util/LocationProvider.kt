package com.naimpro.app.ui.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import com.naimpro.app.data.city.CityRepository
import com.naimpro.app.domain.model.Location
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Single-shot, approximate location lookup.
 *
 * Only the coarse permission is declared in the manifest, so the returned point
 * is deliberately rounded to two decimals (roughly one kilometre) before it is
 * ever stored. Nothing leaves the device.
 */
object LocationProvider {
    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    suspend fun current(
        context: Context,
        cities: CityRepository,
    ): Location? {
        if (!hasPermission(context)) return null
        val manager = context.getSystemService(LocationManager::class.java) ?: return null
        if (!LocationManagerCompat.isLocationEnabled(manager)) return null

        val provider =
            when {
                manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                else -> return null
            }

        val androidLocation =
            suspendCancellableCoroutine<android.location.Location?> { continuation ->
                val signal = CancellationSignal()
                continuation.invokeOnCancellation { signal.cancel() }
                try {
                    LocationManagerCompat.getCurrentLocation(
                        manager,
                        provider,
                        signal,
                        ContextCompat.getMainExecutor(context),
                    ) { location ->
                        if (continuation.isActive) continuation.resume(location)
                    }
                } catch (error: SecurityException) {
                    if (continuation.isActive) continuation.resume(null)
                }
            } ?: return null

        val latitude = round(androidLocation.latitude)
        val longitude = round(androidLocation.longitude)
        val nearest = cities.nearest(latitude, longitude)
        return Location(
            id = "gps/$latitude/$longitude",
            name = nearest?.name ?: "Konum",
            region = nearest?.region ?: "",
            country = nearest?.country ?: "",
            latitude = latitude,
            longitude = longitude,
            zoneId = nearest?.zoneId ?: java.time.ZoneId.systemDefault(),
            isGps = true,
        )
    }

    private fun round(value: Double): Double = Math.round(value * 100.0) / 100.0
}
