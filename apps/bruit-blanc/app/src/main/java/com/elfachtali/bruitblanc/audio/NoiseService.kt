package com.elfachtali.bruitblanc.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.elfachtali.bruitblanc.MainActivity
import com.elfachtali.bruitblanc.R

/**
 * Service de premier plan de type `mediaPlayback` : il garde le son actif écran éteint,
 * affiche la notification avec le bouton Arrêt et applique la minuterie de sommeil.
 *
 * Il demande le focus audio et s'arrête si une autre app le prend durablement (appel,
 * musique). Un wake lock partiel, limité à 12 h, empêche le processeur de s'endormir.
 */
class NoiseService : Service() {
    private val player = NoisePlayer({ Playback.state.value.type }, { Playback.state.value.volume })
    private val handler = Handler(Looper.getMainLooper())
    private var wakeLock: PowerManager.WakeLock? = null
    private var focusRequest: AudioFocusRequest? = null

    // Vérifie chaque seconde si l'heure d'arrêt de la minuterie est atteinte.
    private val timerCheck = object : Runnable {
        override fun run() {
            val stopAt = Playback.state.value.stopAt
            if (stopAt != null && System.currentTimeMillis() >= stopAt) {
                Playback.stop(this@NoiseService)
            } else {
                handler.postDelayed(this, 1_000)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            Playback.stop(this)
            return START_NOT_STICKY
        }
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK else 0,
        )
        if (!requestFocus()) {
            Playback.stop(this)
            return START_NOT_STICKY
        }
        wakeLock = wakeLock ?: (getSystemService(POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BruitBlanc:playback")
            .apply { acquire(MAX_WAKE_MS) }
        player.start()
        handler.removeCallbacks(timerCheck)
        handler.post(timerCheck)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(timerCheck)
        player.stop()
        focusRequest?.let { (getSystemService(AUDIO_SERVICE) as AudioManager).abandonAudioFocusRequest(it) }
        wakeLock?.takeIf { it.isHeld }?.release()
        Playback.markStopped()
        super.onDestroy()
    }

    /** Demande le focus audio ; renvoie false si le système le refuse (appel en cours, par exemple). */
    private fun requestFocus(): Boolean {
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setOnAudioFocusChangeListener { change ->
                if (change == AudioManager.AUDIOFOCUS_LOSS) Playback.stop(this)
            }
            .build()
        focusRequest = request
        val manager = getSystemService(AUDIO_SERVICE) as AudioManager
        return manager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    /** Notification obligatoire du service : ouvre l'app au toucher, avec une action Arrêt. */
    private fun buildNotification(): Notification {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, getString(R.string.channel_name), NotificationManager.IMPORTANCE_LOW)
        )
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE,
        )
        val stop = PendingIntent.getService(
            this, 1, Intent(this, NoiseService::class.java).setAction(ACTION_STOP), PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_playing))
            .setContentIntent(open)
            .addAction(0, getString(R.string.stop), stop)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private companion object {
        const val CHANNEL_ID = "playback"
        const val NOTIFICATION_ID = 1
        const val ACTION_STOP = "com.elfachtali.bruitblanc.STOP"
        const val MAX_WAKE_MS = 12 * 60 * 60 * 1_000L
    }
}
