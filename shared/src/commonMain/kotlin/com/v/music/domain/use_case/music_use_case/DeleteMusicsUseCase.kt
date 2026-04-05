package com.v.music.domain.use_case.music_use_case

import com.v.music.domain.model.Music
import com.v.music.domain.repository.MusicRepository
import com.v.music.utils.DeleteResult
import com.v.music.utils.MusicDeletionResult
import com.v.music.utils.Resources
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteMusicsUseCase (
    private val repository: MusicRepository
) {
    operator fun invoke(music: Music): Flow<Resources<DeleteResult>> = flow {
        emit(Resources.Loading())
        try {
            when (val result = repository.getDeletePermissionIntent(music)) {
                is MusicDeletionResult.RequiresPermission -> {
                    emit(Resources.Success(DeleteResult.NeedPermission(result.platformIntent)))
                    return@flow
                }

                is MusicDeletionResult.Failure -> {
                    emit(Resources.Error(result.error))
                    return@flow
                }

                is MusicDeletionResult.Success -> {
                    // no permission needed, proceed to delete directly
                }
            }

            val deletedRows = repository.deleteMusic(music)
            if (deletedRows > 0) {
                emit(Resources.Success(DeleteResult.Success))
            } else {
                emit(Resources.Error("Failed to delete music"))
            }
        } catch (e: Exception) {
            emit(Resources.Error(e.message ?: "Unknown error"))
        }
    }

    // Direct delete after permission granted
    fun executeDelete(music: Music): Flow<Resources<DeleteResult>> = flow {
        emit(Resources.Loading())
        try {
            val deletedRows = repository.deleteMusic(music)
            if (deletedRows > 0) {
                emit(Resources.Success(DeleteResult.Success))
            } else {
                emit(Resources.Error("Failed to delete music"))
            }
        } catch (e: Exception) {
            emit(Resources.Error(e.message ?: "Unknown error"))
        }
    }


}