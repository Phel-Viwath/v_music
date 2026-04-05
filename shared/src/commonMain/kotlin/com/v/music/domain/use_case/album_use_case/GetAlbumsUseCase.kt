package com.v.music.domain.use_case.album_use_case

import com.v.music.domain.model.Album
import com.v.music.domain.repository.MusicRepository
import com.v.music.utils.Resources
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetAlbumsUseCase (
    private val repository: MusicRepository
){
    operator fun invoke(): Flow<Resources<List<Album>>> = flow{
        emit(Resources.Loading())
        try {
            val albums = repository.getAlbums()
            emit(Resources.Success(albums))
        }catch (e: Exception){
            emit(Resources.Error(e.message ?: "Unknown error"))
        }
    }

}