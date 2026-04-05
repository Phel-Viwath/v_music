package com.v.music.domain.service

import com.v.music.domain.model.Music
import com.v.music.domain.model.PlaybackStates
import com.v.music.domain.model.PlayerState
import com.v.music.domain.model.PlayingMode
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerTimeControlStatusPaused
import platform.AVFoundation.AVPlayerTimeControlStatusPlaying
import platform.AVFoundation.AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.duration
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.AVFoundation.seekToTime
import platform.AVFoundation.timeControlStatus
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMake
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL
import platform.MediaPlayer.MPMediaItemPropertyAlbumTitle
import platform.MediaPlayer.MPMediaItemPropertyArtist
import platform.MediaPlayer.MPMediaItemPropertyPlaybackDuration
import platform.MediaPlayer.MPMediaItemPropertyTitle
import platform.MediaPlayer.MPNowPlayingInfoCenter
import platform.MediaPlayer.MPNowPlayingInfoPropertyElapsedPlaybackTime
import platform.MediaPlayer.MPNowPlayingInfoPropertyPlaybackRate
import platform.MediaPlayer.MPRemoteCommandCenter
import kotlin.math.roundToLong

@OptIn(ExperimentalForeignApi::class)
class IosMusicPlayer : MusicPlayer {

    private val avPlayer = AVPlayer()
    private var playlist: List<Music> = emptyList()
    private var currentIndex: Int = 0
    private var playingMode: PlayingMode = PlayingMode.REPEAT_ALL

    // Shuffle keeps a shuffled index list so we can walk through it linearly
    private var shuffleOrder: List<Int> = emptyList()
    private var shufflePosition: Int = 0   // position inside shuffleOrder

    // ── State ─────────────────────────────────────────────────────────────────

    private val _playbackStates = MutableStateFlow(PlaybackStates())
    override val playbackStates: StateFlow<PlaybackStates> = _playbackStates

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var positionPollingJob: Job? = null
    private var timeObserverToken: Any? = null

    init {
        setupRemoteCommandCenter()
        observeItemEnd()
    }

    override fun setPlaylist(musics: List<Music>) {
        playlist = musics
        currentIndex = 0
        shuffleOrder = musics.indices.shuffled()
        shufflePosition = 0
        loadCurrentItem()
    }

    override fun play(music: Music?) {
        if (music != null) {
            val idx = playlist.indexOf(music)
            if (idx >= 0) {
                currentIndex = idx
                shufflePosition = shuffleOrder.indexOf(idx).takeIf { it >= 0 } ?: 0
                loadCurrentItem()
            }
        }
        avPlayer.play()
        startPositionPolling()
        updateState()
        updateNowPlayingInfo()
    }

    override fun pause() {
        avPlayer.pause()
        stopPositionPolling()
        updateState()
        updateNowPlayingInfo()
    }

    override fun resume() {
        avPlayer.play()
        startPositionPolling()
        updateState()
        updateNowPlayingInfo()
    }

    override fun next() {
        currentIndex = nextIndex()
        shufflePosition = shuffleOrder.indexOf(currentIndex).takeIf { it >= 0 } ?: 0
        loadCurrentItem()
        avPlayer.play()
        startPositionPolling()
        updateState()
        updateNowPlayingInfo()
    }

    override fun previous() {
        val positionSec = CMTimeGetSeconds(avPlayer.currentTime())
        if (positionSec > 3.0) {
            // restart current track if more than 3 s in
            seekTo(0L)
            return
        }
        currentIndex = previousIndex()
        shufflePosition = shuffleOrder.indexOf(currentIndex).takeIf { it >= 0 } ?: 0
        loadCurrentItem()
        avPlayer.play()
        startPositionPolling()
        updateState()
        updateNowPlayingInfo()
    }

    override fun seekTo(position: Long) {
        val time = CMTimeMake(position / 1000, 1)
        avPlayer.seekToTime(time)
        updateState()
    }

    override fun seekToIndex(index: Int) {
        if (index !in playlist.indices) return
        currentIndex = index
        shufflePosition = shuffleOrder.indexOf(index).takeIf { it >= 0 } ?: 0
        loadCurrentItem()
        avPlayer.play()
        startPositionPolling()
        updateState()
    }

    override fun setRepeatAll() {
        playingMode = PlayingMode.REPEAT_ALL
        updateState()
    }

    override fun setRepeatOne() {
        playingMode = PlayingMode.REPEAT_ONE
        updateState()
    }

    override fun setShuffle(enabled: Boolean) {
        playingMode = if (enabled) PlayingMode.SHUFFLE else PlayingMode.REPEAT_ALL
        if (enabled) {
            shuffleOrder = playlist.indices.shuffled()
            shufflePosition = shuffleOrder.indexOf(currentIndex).takeIf { it >= 0 } ?: 0
        }
        updateState()
    }

    override fun stop() {
        avPlayer.pause()
        stopPositionPolling()
        playlist = emptyList()
        currentIndex = 0
        _playbackStates.value = PlaybackStates()
        MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = null
    }

    override fun addToPlayNext(music: Music) {
        val mutable = playlist.toMutableList()
        mutable.add(currentIndex + 1, music)
        playlist = mutable
        regenerateShuffleOrder()
    }

    override fun playLast(music: Music) {
        val mutable = playlist.toMutableList()
        mutable.add(music)
        playlist = mutable
        regenerateShuffleOrder()
    }

    override fun calculateProgress(): Int {
        val pos = currentPositionMs()
        val dur = durationMs()
        return if (dur > 0) ((pos.toFloat() / dur.toFloat()) * 100).toInt() else 0
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private fun loadCurrentItem() {
        val music = playlist.getOrNull(currentIndex) ?: return
        val url = NSURL.URLWithString(music.uri) ?: return
        val item = AVPlayerItem(url)
        avPlayer.replaceCurrentItemWithPlayerItem(item)
        observeItemEnd() // re-register because replaceCurrentItem resets observer context
    }

    private fun updateState() {
        val isPlaying =
            avPlayer.timeControlStatus == AVPlayerTimeControlStatusPlaying
        val isPaused =
            avPlayer.timeControlStatus == AVPlayerTimeControlStatusPaused
        val isLoading =
            avPlayer.timeControlStatus == AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate

        _playbackStates.value = PlaybackStates(
            isPlaying = isPlaying,
            isPaused = isPaused,
            isLoading = isLoading,
            currentPosition = currentPositionMs(),
            duration = durationMs(),
            currentMusic = playlist.getOrNull(currentIndex),
            currentIndex = currentIndex,
            playlist = playlist,
            playingMode = playingMode,
            playerState = when {
                isLoading -> PlayerState.BUFFERING
                isPlaying || isPaused -> PlayerState.READY
                else -> PlayerState.IDLE
            }
        )
    }

    // Poll position every 500 ms so the UI progress bar stays smooth
    private fun startPositionPolling() {
        positionPollingJob?.cancel()
        positionPollingJob = scope.launch {
            while (true) {
                updateState()
                delay(500)
            }
        }
    }

    private fun stopPositionPolling() {
        positionPollingJob?.cancel()
        positionPollingJob = null
    }

    private fun currentPositionMs(): Long {
        val secs = CMTimeGetSeconds(avPlayer.currentTime())
        return if (secs.isNaN() || secs.isInfinite()) 0L
        else (secs * 1000).roundToLong()
    }

    private fun durationMs(): Long {
        val item = avPlayer.currentItem ?: return 0L
        val secs = CMTimeGetSeconds(item.duration)
        return if (secs.isNaN() || secs.isInfinite()) 0L
        else (secs * 1000).roundToLong()
    }

    // ── Track end handling ───────────────────────────────────────────────────

    private var endObserver: Any? = null

    private fun observeItemEnd() {
        endObserver?.let {
            NSNotificationCenter.defaultCenter.removeObserver(it)
        }
        endObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = avPlayer.currentItem,
            queue = null
        ) { _ ->
            onItemFinished()
        }
    }

    private fun onItemFinished() {
        when (playingMode) {
            PlayingMode.REPEAT_ONE -> {
                seekTo(0L)
                avPlayer.play()
            }
            PlayingMode.SHUFFLE, PlayingMode.REPEAT_ALL -> {
                next()
            }
        }
    }

    // ── Navigation helpers ───────────────────────────────────────────────────

    private fun nextIndex(): Int {
        if (playlist.isEmpty()) return 0
        return when (playingMode) {
            PlayingMode.SHUFFLE -> {
                shufflePosition = (shufflePosition + 1) % shuffleOrder.size
                shuffleOrder[shufflePosition]
            }
            PlayingMode.REPEAT_ONE -> currentIndex
            PlayingMode.REPEAT_ALL -> (currentIndex + 1) % playlist.size
        }
    }

    private fun previousIndex(): Int {
        if (playlist.isEmpty()) return 0
        return when (playingMode) {
            PlayingMode.SHUFFLE -> {
                shufflePosition = (shufflePosition - 1 + shuffleOrder.size) % shuffleOrder.size
                shuffleOrder[shufflePosition]
            }
            PlayingMode.REPEAT_ONE -> currentIndex
            PlayingMode.REPEAT_ALL ->
                (currentIndex - 1 + playlist.size) % playlist.size
        }
    }

    private fun regenerateShuffleOrder() {
        shuffleOrder = playlist.indices.shuffled()
        shufflePosition = shuffleOrder.indexOf(currentIndex).takeIf { it >= 0 } ?: 0
    }

    // ── Now Playing Info Center ──────────────────────────────────────────────

    private fun updateNowPlayingInfo() {
        val music = playlist.getOrNull(currentIndex) ?: return
        val positionSec = CMTimeGetSeconds(avPlayer.currentTime())
        val durationSec = avPlayer.currentItem?.let {
            CMTimeGetSeconds(it.duration).takeIf { d -> !d.isNaN() && !d.isInfinite() }
        } ?: 0.0
        val rate = if (avPlayer.timeControlStatus == AVPlayerTimeControlStatusPlaying) 1.0 else 0.0

        MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = mapOf(
            MPMediaItemPropertyTitle to music.title,
            MPMediaItemPropertyArtist to music.artist,
            MPMediaItemPropertyAlbumTitle to music.album,
            MPMediaItemPropertyPlaybackDuration to durationSec,
            MPNowPlayingInfoPropertyElapsedPlaybackTime to positionSec,
            MPNowPlayingInfoPropertyPlaybackRate to rate,
        )
    }

    // ── Remote Command Center ────────────────────────────────────────────────

    private fun setupRemoteCommandCenter() {
        val center = MPRemoteCommandCenter.sharedCommandCenter()

        center.playCommand.addTargetWithHandler { _ ->
            resume()
            1L // MPRemoteCommandHandlerStatusSuccess
        }
        center.pauseCommand.addTargetWithHandler { _ ->
            pause()
            1L
        }
        center.togglePlayPauseCommand.addTargetWithHandler { _ ->
            if (avPlayer.timeControlStatus == AVPlayerTimeControlStatusPlaying) pause()
            else resume()
            1L
        }
        center.nextTrackCommand.addTargetWithHandler { _ ->
            next()
            1L
        }
        center.previousTrackCommand.addTargetWithHandler { _ ->
            previous()
            1L
        }
        center.changePlaybackPositionCommand.addTargetWithHandler { _ ->
            // Cast via Objective-C runtime reflection isn't available in pure Kotlin/Native;
            // handle via a periodic AVPlayer observer instead (see startPositionPolling).
            1L
        }
    }
}