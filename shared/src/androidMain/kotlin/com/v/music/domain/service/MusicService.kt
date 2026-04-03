package com.v.music.domain.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaMetadata
import android.media.session.PlaybackState
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import com.v.music.domain.broadcast.PhoneCallReceiver
import com.v.music.domain.model.Music
import com.v.music.utils.Constant.ACTION_NEXT
import com.v.music.utils.Constant.ACTION_PAUSE
import com.v.music.utils.Constant.ACTION_PLAY
import com.v.music.utils.Constant.ACTION_PREVIOUS
import com.v.music.utils.Constant.ACTION_STOP
import com.v.music.utils.Constant.CHANNEL_ID
import com.v.music.utils.Constant.NOTIFICATION_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

@Suppress("DEPRECATION")
class MusicService : Service() {

    private val musicPlayer: AndroidMusicPlayer by inject()
    private val exoPlayer get() = musicPlayer.player

    private lateinit var mediaSession: MediaSessionCompat
    private var callStateListener: PhoneCallReceiver? = null

    private var currentMusic: Music? = null
    private var isServiceStarted = false

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        setupMediaSession()
        setupCallListener()
        initializeService()
    }

    override fun onBind(intent: Intent?): IBinder = MusicBinder()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let(::handleNotificationAction)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanupService()
    }

    private fun setupMediaSession() {
        mediaSession = MediaSessionCompat(this, "MusicService").apply {
            setCallback(createMediaSessionCallback())
            isActive = true
        }
    }

    private fun setupCallListener() {
        callStateListener = PhoneCallReceiver(
            onCallStart = { musicPlayer.pause() },
            onCallEnded = { musicPlayer.resume() }
        )
        val filter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        registerReceiver(callStateListener, filter)
    }

    private fun initializeService() {
        createNotificationChannel()
        startPlaybackObserver()
    }

    private fun cleanupService() {
        serviceScope.cancel()
        try {
            unregisterReceiver(callStateListener)
        } catch (_: Exception) {
            // ignore
        }
        mediaSession.release()
    }

    private fun startPlaybackObserver() {
        serviceScope.launch {
            musicPlayer.playbackStates.collect { state ->
                currentMusic = state.currentMusic
                updateMediaMetadata(state.currentMusic)
                updatePlaybackState()
                if (state.isPlaying) {
                    startForegroundService()
                }
                updateNotification(state.isPlaying)
            }
        }
    }

    private fun createMediaSessionCallback(): MediaSessionCompat.Callback {
        return object : MediaSessionCompat.Callback() {
            override fun onPlay() { musicPlayer.play() }
            override fun onPause() { musicPlayer.pause() }
            override fun onSkipToNext() { musicPlayer.next() }
            override fun onSkipToPrevious() { musicPlayer.previous() }
            override fun onStop() { stopService() }
            override fun onSeekTo(pos: Long) { musicPlayer.seekTo(pos) }
        }
    }

    private fun updatePlaybackState() {
        val stateBuilder = PlaybackStateCompat.Builder()
            .setActions(getPlaybackActions())
            .setState(getPlaybackState(), exoPlayer.currentPosition, 1f)
        mediaSession.setPlaybackState(stateBuilder.build())
    }

    private fun getPlaybackActions(): Long {
        return PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_STOP or
                PlaybackStateCompat.ACTION_SEEK_TO
    }

    private fun getPlaybackState(): Int {
        return when {
            exoPlayer.isPlaying -> PlaybackState.STATE_PLAYING
            exoPlayer.playbackState == androidx.media3.common.Player.STATE_BUFFERING -> PlaybackState.STATE_BUFFERING
            else -> PlaybackState.STATE_PAUSED
        }
    }

    private fun updateMediaMetadata(music: Music?) {
        music?.let {
            val metadata = MediaMetadataCompat.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, it.title)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, it.artist)
                .putString(MediaMetadata.METADATA_KEY_ALBUM, it.album)
                .putLong(MediaMetadata.METADATA_KEY_DURATION, it.duration)
                .build()
            mediaSession.setMetadata(metadata)
        }
    }

    private fun handleNotificationAction(action: String) {
        when (action) {
            ACTION_PLAY -> musicPlayer.play()
            ACTION_PAUSE -> musicPlayer.pause()
            ACTION_NEXT -> musicPlayer.next()
            ACTION_PREVIOUS -> musicPlayer.previous()
            ACTION_STOP -> stopService()
        }
    }

    private fun startForegroundService() {
        if (!isServiceStarted) {
            val notification = buildNotification(exoPlayer.isPlaying)
            startForeground(NOTIFICATION_ID, notification)
            isServiceStarted = true
        }
    }

    private fun updateNotification(isPlaying: Boolean) {
        if (isServiceStarted) {
            val notification = buildNotification(isPlaying)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(isPlaying: Boolean): Notification {
        val title = currentMusic?.title ?: "Unknown Music"
        val artist = currentMusic?.artist ?: "Unknown Artist"

        val mainActivityIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            this, 0, mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentIntent(contentIntent)
            .setContentTitle(title)
            .setContentText(artist)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setOngoing(isPlaying)
            .setShowWhen(false)
            .addAction(android.R.drawable.ic_media_previous, "Previous", createActionIntent(ACTION_PREVIOUS))
            .addAction(if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isPlaying) "Pause" else "Play", createActionIntent(if (isPlaying) ACTION_PAUSE else ACTION_PLAY))
            .addAction(android.R.drawable.ic_media_next, "Next", createActionIntent(ACTION_NEXT))
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", createActionIntent(ACTION_STOP))
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                .setShowActionsInCompactView(0, 1, 2)
            )

        return builder.build()
    }

    private fun createActionIntent(action: String): PendingIntent {
        val intent = Intent(this, MusicService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Music playback controls"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun stopService() {
        musicPlayer.stop()
        stopForeground(true)
        stopSelf()
        isServiceStarted = false
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }
}