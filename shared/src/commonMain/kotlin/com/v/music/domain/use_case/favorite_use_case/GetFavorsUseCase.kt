package com.v.music.domain.use_case.favorite_use_case

import com.v.music.domain.model.Music
import com.v.music.domain.model.SortOrder
import com.v.music.domain.repository.MusicRepository
import com.v.music.utils.Resources
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class GetFavorsUseCase (
    private val repository: MusicRepository
) {
    operator fun invoke(sort: SortOrder): Flow<Resources<List<Music>>> = flow {
        emit(Resources.Loading())
        try {
            val musics = when (sort) {
                SortOrder.DATE -> repository.getFavoriteMusicByDate()
                SortOrder.DURATION -> repository.getFavoriteMusicByDuration()
                SortOrder.TITLE -> repository.getFavoriteMusicByTitle()
            }
            emit(Resources.Success(musics.first()))
        } catch (e: Exception) {
            emit(Resources.Error(e.message ?: "Unknown error"))
        }
    }

}