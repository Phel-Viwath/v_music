package com.v.music.domain.use_case.favorite_use_case

import com.v.music.domain.model.Music
import com.v.music.domain.repository.MusicRepository
import com.v.music.utils.Resources
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AddFavorUseCase (
    private val repository: MusicRepository
){
    operator fun invoke(music: Music): Flow<Resources<Long>> = flow {
        emit(Resources.Loading())
        try {
            val rowId = repository.addFavorite(music)
            emit(Resources.Success(rowId))
        }catch (e: Exception){
            emit(Resources.Error(e.message ?: "Unknown error"))
        }

    }
}