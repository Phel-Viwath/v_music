package com.v.music.domain.model

import androidx.compose.runtime.Stable

@Stable
data class PlaybackStates(
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val isLoading: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val currentMusic: Music? = null,
    val currentIndex: Int = 0,
    val playlist: List<Music> = emptyList(),
    val playingMode: PlayingMode = PlayingMode.REPEAT_ALL,
    val playerState: PlayerState = PlayerState.IDLE,
    val error: String? = null,
){
    val progress: Float
        get() = if (duration > 0L) currentPosition.toFloat() / duration.toFloat() else 0f

    val hasNext: Boolean
        get() = currentIndex < playlist.size - 1

    val hasPrevious: Boolean
        get() = currentIndex > 0

    val isIdle: Boolean
        get() = playerState == PlayerState.IDLE

    val isBuffering: Boolean
        get() = playerState == PlayerState.BUFFERING
}



enum class PlayingMode {
    SHUFFLE,
    REPEAT_ALL,
    REPEAT_ONE;

    fun next(): PlayingMode = when (this) {
        REPEAT_ALL -> REPEAT_ONE
        REPEAT_ONE -> SHUFFLE
        SHUFFLE    -> REPEAT_ALL
    }

    fun displayName(): String = when (this) {
        SHUFFLE    -> "Shuffle"
        REPEAT_ALL -> "Repeat all"
        REPEAT_ONE -> "Repeat one"
    }
}

enum class PlayerState {
    IDLE,
    BUFFERING,
    READY,
    ENDED
}
