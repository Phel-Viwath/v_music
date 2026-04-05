package com.v.music.domain.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class MusicServiceConnection(
    private val context: Context
) {
    // Emits non-null once the service is bound
    private val _player = MutableStateFlow<MusicPlayer?>(null)
    actual val player: StateFlow<MusicPlayer?> = _player.asStateFlow()

    private var isBound = false

    // ── ServiceConnection ────────────────────────────────────────────────────

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val musicBinder = binder as? MusicService.MusicBinder ?: return
            _player.value = musicBinder.getPlayer()   // AndroidMusicPlayer
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            _player.value = null
            isBound = false
        }
    }

    actual fun bind() {
        if (isBound) return
        val intent = Intent(context, MusicService::class.java)
        // Start so it survives unbind, then also bind for the IBinder handle
        context.startService(intent)
        isBound = context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    actual fun unbind() {
        if (!isBound) return
        context.unbindService(connection)
        isBound = false
        _player.value = null
    }
}