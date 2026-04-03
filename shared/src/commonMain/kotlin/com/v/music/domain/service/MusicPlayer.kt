package com.v.music.domain.service

import com.v.music.domain.model.Music
import com.v.music.domain.model.PlaybackStates
import kotlinx.coroutines.flow.StateFlow

interface MusicPlayer {
    val playbackStates: StateFlow<PlaybackStates>
    fun setPlaylist(musics: List<Music>)
    fun play(music: Music? = null)
    fun pause()
    fun resume()
    fun next()
    fun previous()
    fun seekTo(position: Long)
    fun seekToIndex(index: Int)
    fun setRepeatAll()
    fun setRepeatOne()
    fun setShuffle(enabled: Boolean)
    fun stop()
    fun addToPlayNext(music: Music)
    fun playLast(music: Music)

    ///
    fun calculateProgress(): Int
}