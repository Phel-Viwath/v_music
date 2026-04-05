package com.v.music.presentation.music_list_page

import androidx.lifecycle.viewModelScope
import com.v.music.domain.model.Music
import com.v.music.domain.model.PlaybackStates
import com.v.music.domain.model.SortOrder
import com.v.music.domain.service.MusicPlayer
import com.v.music.domain.service.MusicServiceConnection
import com.v.music.domain.use_case.music_use_case.MusicUseCase
import com.v.music.utils.BaseViewModel
import com.v.music.utils.Resources
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MusicListState(
    val music: Resources<List<Music>>? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class MusicViewModel(
    private val musicUseCase: MusicUseCase,
    private val connection: MusicServiceConnection
) : BaseViewModel<MusicListState>(), MusicListPageIntent {

    val sortOrder: StateFlow<SortOrder>
        field = MutableStateFlow(SortOrder.TITLE)

    val playBackState: StateFlow<PlaybackStates> = connection.player
        .flatMapLatest { player ->
            player?.playbackStates ?: flowOf(PlaybackStates())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PlaybackStates()
        )

    private val player: MusicPlayer?
        get() = connection.player.value

    init {
        connection.bind()
        loadMusic()
    }

    override fun initialState(): MusicListState {
        return MusicListState()
    }

    override fun onHandleInten(intent: MusicListPageIntent.Intent) {
        TODO("Not yet implemented")
    }

    fun setOrder(order: SortOrder) {
        sortOrder.value = order
        loadMusic(order)
    }

    private fun loadMusic(order: SortOrder = sortOrder.value) {
        viewModelScope.launch {
            musicUseCase.getMusicsUseCase(order).collect { resource ->
                setState { copy(music = resource) }
            }
        }
    }




}