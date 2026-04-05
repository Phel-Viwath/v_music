package com.v.music.domain.use_case.album_use_case

import com.v.music.domain.model.Music
import com.v.music.domain.repository.MusicRepository
import com.v.music.utils.Resources
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetAlbumMusicUseCase (
    private val repository: MusicRepository
) {
    operator fun invoke(albumId: Long): Flow<Resources<List<Music>>> = flow {
        emit(Resources.Loading())
        if (albumId == -1L){
            emit(Resources.Error("Album not found"))
            return@flow
        }
        try {
            val musicFiles = repository.getSongByAlbumId(albumId)
            emit(Resources.Success(musicFiles))
        }catch (e: Exception){
            emit(Resources.Error(e.message ?: "Unknown error"))
        }
    }

}