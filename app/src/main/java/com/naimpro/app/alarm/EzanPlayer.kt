package com.naimpro.app.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.naimpro.app.R
import com.naimpro.app.data.prefs.NotificationMode
import java.io.IOException

/**
 * Plays the adhan (or the built-in chime, or a short vibration) when a prayer
 * time alarm fires.
 *
 * A wake lock is held only while audio is playing and released immediately
 * afterwards, which keeps the battery impact to a few seconds per prayer.
 */
object EzanPlayer {
    private var player: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null

    fun play(
        context: Context,
        mode: NotificationMode,
        volumePercent: Int,
        customUri: String?,
        vibrationEnabled: Boolean,
    ) {
        release()
        when (mode) {
            NotificationMode.SILENT -> Unit
            NotificationMode.VIBRATE -> vibrate(context)
            NotificationMode.SHORT_ALERT -> {
                startPlayer(context, null, volumePercent, short = true)
                if (vibrationEnabled) vibrate(context)
            }
            NotificationMode.EZAN -> {
                startPlayer(context, customUri, volumePercent, short = false)
                if (vibrationEnabled) vibrate(context)
            }
        }
    }

    fun stop() = release()

    fun isPlaying(): Boolean = player?.isPlaying == true

    private fun startPlayer(
        context: Context,
        customUri: String?,
        volumePercent: Int,
        short: Boolean,
    ) {
        acquireWakeLock(context)
        val resolved = resolveSource(context, customUri, short)
        val mediaPlayer = MediaPlayer()
        try {
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )
            if (resolved.uri != null) {
                mediaPlayer.setDataSource(context, resolved.uri)
            } else {
                context.resources.openRawResourceFd(resolved.rawResId).use { fd ->
                    mediaPlayer.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
                }
            }
            mediaPlayer.setVolume(volumePercent.coerceIn(0, 100) / 100f, volumePercent.coerceIn(0, 100) / 100f)
            mediaPlayer.isLooping = false
            mediaPlayer.setOnCompletionListener { release() }
            mediaPlayer.prepare()
            mediaPlayer.start()
            player = mediaPlayer
        } catch (error: IOException) {
            release()
        } catch (error: IllegalStateException) {
            release()
        }
    }

    private data class Source(val uri: Uri?, val rawResId: Int)

    /** Either the user's own audio file, the full chime, or the short alert tone. */
    private fun resolveSource(
        context: Context,
        customUri: String?,
        short: Boolean,
    ): Source {
        if (!customUri.isNullOrBlank()) {
            val parsed = runCatching { Uri.parse(customUri) }.getOrNull()
            if (parsed != null) return Source(parsed, 0)
        }
        return Source(null, if (short) R.raw.naim_alert else R.raw.naim_chime)
    }

    private fun vibrate(context: Context) {
        val vibrator =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(VibratorManager::class.java)
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Vibrator::class.java)
            } ?: return

        if (!vibrator.hasVibrator()) return
        val pattern = longArrayOf(0, 400, 200, 400, 200, 600)
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
    }

    private fun acquireWakeLock(context: Context) {
        val powerManager = context.getSystemService(PowerManager::class.java) ?: return
        val lock =
            powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "NaimPro:Adhan",
            )
        lock.acquire(MAX_WAKE_LOCK_MS)
        wakeLock = lock
    }

    private fun release() {
        player?.let { mediaPlayer ->
            runCatching { if (mediaPlayer.isPlaying) mediaPlayer.stop() }
            runCatching { mediaPlayer.release() }
        }
        player = null
        wakeLock?.let { lock ->
            runCatching { if (lock.isHeld) lock.release() }
        }
        wakeLock = null
    }

    /** Longest the adhan wake lock may ever be held. */
    private const val MAX_WAKE_LOCK_MS = 6 * 60 * 1000L
}
