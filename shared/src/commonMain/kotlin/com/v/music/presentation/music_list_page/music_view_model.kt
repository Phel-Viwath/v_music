package com.v.music.presentation.music_list_page

import androidx.lifecycle.viewModelScope
import com.v.music.domain.model.Music
import com.v.music.domain.model.PlaybackStates
import com.v.music.domain.model.SortOrder
import com.v.music.domain.service.MusicPlayer
import com.v.music.domain.service.MusicServiceConnection
import com.v.music.domain.use_case.music_use_case.MusicUseCase
import com.v.music.utils.BaseViewModel
import com.v.music.utils.DeleteResult
import com.v.music.utils.Resources
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MusicListState(
    val music: Resources<List<Music>> = Resources.Loading(),
    val searchText: String = "",
    val sortOrder: SortOrder = SortOrder.TITLE,

    /** Non-null while waiting for the user to grant OS delete permission. */
    val pendingDeleteMusic: Music? = null,

    /** Shown in a snackbar / toast after a delete attempt. */
    val deleteResult: DeleteResult? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class MusicViewModel(
    private val musicUseCase: MusicUseCase,
    private val connection: MusicServiceConnection
) : BaseViewModel<MusicListState>(), MusicListPageIntent {

    private val player: MusicPlayer?
        get() = connection.player.value


    val sortOrder: SortOrder
        get() = state.value.sortOrder


    init {
        connection.bind()
        loadMusic()
    }

    override fun initialState(): MusicListState = MusicListState()

    override val playbackState: StateFlow<PlaybackStates>
        get() = connection.player
            .flatMapLatest { player ->
                player?.playbackStates ?: flowOf(PlaybackStates())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = PlaybackStates()
            )

    override fun onHandleIntent(intent: MusicListPageIntent.Intent) {
        when (intent) {

            is MusicListPageIntent.Intent.OnLoadMusic ->
                loadMusic()

            is MusicListPageIntent.Intent.GetOrder -> {
                /* sortOrder lives in state; UI reads state.value.sortOrder */
            }

            is MusicListPageIntent.Intent.Order -> {
                setState { copy(sortOrder = intent.sortOrder) }
                loadMusic(order = intent.sortOrder)
            }

            is MusicListPageIntent.Intent.SearchTextChange ->
                setState { copy(searchText = intent.searchText) }

            is MusicListPageIntent.Intent.SearchClick ->
                loadMusic(filterText = state.value.searchText)

            is MusicListPageIntent.Intent.OnPlay -> {
                val queue = intent.musics.ifEmpty {
                    (state.value.music as? Resources.Success)?.data ?: listOf(intent.music)
                }
                player?.setPlaylist(queue)
                player?.play(intent.music)
            }

            is MusicListPageIntent.Intent.OnPause        -> player?.pause()
            is MusicListPageIntent.Intent.OnResume       -> player?.resume()
            is MusicListPageIntent.Intent.OnPlayNext     -> player?.next()
            is MusicListPageIntent.Intent.OnPlayPrevious -> player?.previous()
            is MusicListPageIntent.Intent.OnSeekTo       -> player?.seekTo(intent.position)
            is MusicListPageIntent.Intent.OnRepeatOne    -> player?.setRepeatOne()
            is MusicListPageIntent.Intent.OnRepeatAll    -> player?.setRepeatAll()

            is MusicListPageIntent.Intent.ShuffleMode ->
                player?.setShuffle(intent.isShuffle)

            is MusicListPageIntent.Intent.AddToPlayNext -> {
                if (intent.musics.isNotEmpty()) player?.setPlaylist(intent.musics)
                player?.addToPlayNext(intent.music)
            }

            is MusicListPageIntent.Intent.AddToPlayLast ->
                player?.playLast(intent.music)

            is MusicListPageIntent.Intent.DeleteMusic ->
                deleteMusic(intent.music)

            is MusicListPageIntent.Intent.OnDeletePermissionGranted -> {
                val pending = state.value.pendingDeleteMusic ?: return
                executeDeleteAfterPermission(pending)
            }
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private fun loadMusic(
        order: SortOrder = state.value.sortOrder,
        filterText: String = "",
    ) {
        viewModelScope.launch {
            musicUseCase.getMusicsUseCase(order).collect { resource ->
                val result = if (filterText.isNotBlank() && resource is Resources.Success) {
                    Resources.Success(resource.data.filter { music ->
                        music.title.contains(filterText, ignoreCase = true) ||
                                music.artist.contains(filterText, ignoreCase = true)
                    })
                } else {
                    resource
                }
                setState { copy(music = result) }
            }
        }
    }

    private fun deleteMusic(music: Music) {
        viewModelScope.launch {
            musicUseCase.deleteMusicsUseCase(music).collect { resource ->
                when (resource) {
                    is Resources.Loading -> Unit
                    is Resources.Success -> when (val result = resource.data) {
                        is DeleteResult.NeedPermission ->
                            setState { copy(pendingDeleteMusic = music, deleteResult = result) }
                        is DeleteResult.Success -> {
                            setState { copy(pendingDeleteMusic = null, deleteResult = result) }
                            loadMusic()
                        }
                    }
                    is Resources.Error ->
                        setState { copy(pendingDeleteMusic = null) }
                }
            }
        }
    }

    private fun executeDeleteAfterPermission(music: Music) {
        viewModelScope.launch {
            musicUseCase.deleteMusicsUseCase.executeDelete(music).collect { resource ->
                when (resource) {
                    is Resources.Success -> {
                        setState { copy(pendingDeleteMusic = null, deleteResult = resource.data) }
                        loadMusic()
                    }
                    is Resources.Error ->
                        setState { copy(pendingDeleteMusic = null) }
                    else -> Unit
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        connection.unbind()
    }

}