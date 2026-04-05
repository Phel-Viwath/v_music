package com.v.music.domain.use_case.favorite_use_case

import com.v.music.domain.model.Music
import com.v.music.domain.repository.MusicRepository
import com.v.music.utils.Resources
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RemoveFavorUseCase(
    private val repository: MusicRepository
){
    operator fun invoke(music: Music): Flow<Resources<Int>> = flow {
        emit(Resources.Loading())
        try {
            val effectedRow = repository.removeFavorite(music)
            if (effectedRow > 0)
                emit(Resources.Success(effectedRow))
            else
                emit(Resources.Error("No rows affected"))
        }catch (e: Exception){
            emit(Resources.Error(e.message ?: "Unknown error"))
        }
    }
}