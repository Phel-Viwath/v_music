package com.v.music.presentation.music_list_page

import com.v.music.domain.model.Music
import com.v.music.domain.model.SortOrder
import kotlinx.coroutines.flow.StateFlow

interface MusicListPageIntent{
    val state: StateFlow<MusicListState>

    fun onHandleInten(intent: Intent)

    sealed class Intent {
        data class Order(val sortOrder: SortOrder): Intent()
        data class SearchTextChange(val searchText: String): Intent()

        data object SearchClick: Intent()

        data object OnLoadMusic: Intent()

        data object GetOrder: Intent()

        data object OnPlayNext: Intent()
        data object OnPlayPrevious: Intent()
        data object OnPause: Intent()
        data object OnResume: Intent()
        data object OnRepeatOne: Intent()
        data object OnRepeatAll: Intent()

        data class AddToPlayNext(val music: Music, val musics: List<Music> = emptyList()): Intent()
        data class AddToPlayLast(val music: Music): Intent()

        data class DeleteMusic(val music: Music): Intent()
        data object OnDeletePermissionGranted: Intent()

        data class ShuffleMode(val isShuffle: Boolean): Intent()
        data class OnSeekTo(val position: Long): Intent()
        data class OnPlay(val music: Music, val musics: List<Music> = emptyList()): Intent()

    }
}