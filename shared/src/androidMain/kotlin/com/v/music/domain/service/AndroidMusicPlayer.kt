package com.v.music.domain.service

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.v.music.domain.model.Music
import com.v.music.domain.model.PlaybackStates
import com.v.music.domain.model.PlayerState
import com.v.music.domain.model.PlayingMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AndroidMusicPlayer(
    private val context: Context
) : MusicPlayer {

    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()

    private val _playbackStates = MutableStateFlow(PlaybackStates())
    override val playbackStates: StateFlow<PlaybackStates>
        get() = _playbackStates

    val player: ExoPlayer
        get() = exoPlayer

    private var playlist: List<Music> = emptyList()

    init {
        setupListener()
    }

    private fun setupListener() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateState()
            }

            override fun onPlaybackStateChanged(state: Int) {
                updateState()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateState()
            }
        })
    }

    private fun updateState() {
        val duration = if (exoPlayer.duration == C.TIME_UNSET) 0L else exoPlayer.duration

        _playbackStates.value = PlaybackStates(
            isPlaying = exoPlayer.isPlaying,
            isPaused = !exoPlayer.isPlaying && exoPlayer.playbackState == Player.STATE_READY,
            isLoading = exoPlayer.playbackState == Player.STATE_BUFFERING,
            currentPosition = exoPlayer.currentPosition,
            duration = duration,
            currentMusic = if (exoPlayer.currentMediaItemIndex in playlist.indices) playlist[exoPlayer.currentMediaItemIndex] else null,
            currentIndex = exoPlayer.currentMediaItemIndex,
            playlist = playlist,
            playingMode = getPlayingMode(),
            playerState = mapPlayerState()
        )
    }

    private fun getPlayingMode(): PlayingMode {
        return when {
            exoPlayer.shuffleModeEnabled -> PlayingMode.SHUFFLE
            exoPlayer.repeatMode == Player.REPEAT_MODE_ONE -> PlayingMode.REPEAT_ONE
            else -> PlayingMode.REPEAT_ALL
        }
    }

    private fun mapPlayerState(): PlayerState {
        return when (exoPlayer.playbackState) {
            Player.STATE_IDLE -> PlayerState.IDLE
            Player.STATE_BUFFERING -> PlayerState.BUFFERING
            Player.STATE_READY -> PlayerState.READY
            Player.STATE_ENDED -> PlayerState.ENDED
            else -> PlayerState.IDLE
        }
    }

    override fun setPlaylist(musics: List<Music>) {
        playlist = musics
        val mediaItems = musics.map { MediaItem.fromUri(it.uri) }

        exoPlayer.setMediaItems(mediaItems)
        exoPlayer.prepare()
    }

    override fun play(music: Music?) {
        music?.let {
            val index = playlist.indexOf(it)
            if (index >= 0) exoPlayer.seekToDefaultPosition(index)
        }
        exoPlayer.play()
    }

    override fun pause() =exoPlayer.pause()
    override fun resume() = exoPlayer.play()
    override fun next() = exoPlayer.seekToNext()
    override fun previous() = exoPlayer.seekToPrevious()
    override fun seekTo(position: Long) = exoPlayer.seekTo(position)
    override fun seekToIndex(index: Int) = exoPlayer.seekToDefaultPosition(index)

    override fun setRepeatAll() {
        exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
    }

    override fun setRepeatOne() {
        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
    }

    override fun setShuffle(enabled: Boolean) {
        exoPlayer.shuffleModeEnabled = enabled
    }
    override fun stop() = exoPlayer.stop()


    override fun addToPlayNext(music: Music) {
        val index = exoPlayer.currentMediaItemIndex
        exoPlayer.addMediaItem(index + 1, MediaItem.fromUri(music.uri))
    }

    override fun playLast(music: Music) {
        exoPlayer.addMediaItem(MediaItem.fromUri(music.uri))
    }

    override fun calculateProgress(): Int {
        val currentPosition = exoPlayer.currentPosition
        val duration = if (exoPlayer.duration == C.TIME_UNSET) 0L else exoPlayer.duration
        return if (duration > 0) {
            ((currentPosition.toFloat() / duration.toFloat()) * 100).toInt()
        } else 0
    }
}