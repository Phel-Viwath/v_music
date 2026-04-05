package com.v.music.domain.use_case.favorite_use_case

import com.v.music.domain.model.Music
import com.v.music.domain.repository.MusicRepository

class GetFavorByIdUseCase(
    private val repository: MusicRepository
) {
    suspend operator fun invoke(id: Long): Music? {
        return try{
            repository.getFavoriteMusicById(id)
        }catch (e: Exception){
            null
        }
    }
}