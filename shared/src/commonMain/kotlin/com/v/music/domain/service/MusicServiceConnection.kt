package com.v.music.domain.service

import kotlinx.coroutines.flow.StateFlow

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class MusicServiceConnection {
    val player: StateFlow<MusicPlayer?>

    fun bind()
    fun unbind()
}