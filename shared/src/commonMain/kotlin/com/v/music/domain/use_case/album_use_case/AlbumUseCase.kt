package com.v.music.domain.use_case.album_use_case

data class AlbumUseCase(
    val getAlbumsUseCase: GetAlbumsUseCase,
    val getAlbumMusicUseCase: GetAlbumMusicUseCase
)
