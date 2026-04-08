package com.v.music.presentation.music_list_page

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v.music.domain.model.Music
import com.v.music.domain.model.PlaybackStates
import com.v.music.domain.model.PlayerState
import com.v.music.domain.model.PlayingMode
import com.v.music.presentation.ui.component.MusicList
import com.v.music.presentation.ui.dialog.AppDialog
import com.v.music.utils.ResourceHandler
import com.v.music.utils.Resources
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Clock

@Composable
fun MusicListPage(
    viewModel: MusicViewModel,
){
    LocalProvider(controller = viewModel) {
        MusicListScreen(
            onMusicSelected = {},
            onMenuClick = {}
        )
    }
}

@Composable
private fun MusicListScreen(
    modifier: Modifier = Modifier,
    onMusicSelected: (Music) -> Unit = {},
    onMenuClick: (Music) -> Unit
){
    val controller = LocalMusicListIntentional.current
    val state by controller.state.collectAsStateWithLifecycle()
    val playbackState by controller.playbackState.collectAsStateWithLifecycle()

    val currentMusic = playbackState.currentMusic
    val isPaused = playbackState.isPaused
    val musicList = state.music

    val showDialog = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf("") }

    Box(
        modifier = modifier
    ) {

        ResourceHandler(
            state = musicList,

            onSuccess = { musicList ->

                MusicList(
                    modifier = Modifier,
                    musicList = musicList,
                    currentMusic = currentMusic,
                    isPaused = isPaused,
                    onMusicSelected = { selectedMusic ->

                        val isPlaying = currentMusic?.id == selectedMusic.id

                        if (!isPlaying) {
                            controller.onHandleIntent(
                                MusicListPageIntent.Intent.OnPlay(
                                    selectedMusic,
                                    musicList
                                )
                            )
                        }

                        onMusicSelected(selectedMusic)
                    },
                    onMenuClick = onMenuClick
                )
            },

            onError = { error ->

                errorMessage.value = error
                showDialog.value = true
            },

            onLoading = {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        )

        AppDialog(
            showDialog = showDialog.value,
            message = errorMessage.value,
            confirmText = "OK",
            onDismiss = { showDialog.value = false },
        )
    }

}

@Composable
private fun LocalProvider(
    controller: MusicListPageIntent,
    content: @Composable () -> Unit
){
    return CompositionLocalProvider(
        value = LocalMusicListIntentional provides controller,
        content = content
    )
}

@Preview
@Composable
fun MusicListScreenPreview() {

    val controller = remember {
        object : MusicListPageIntent {
            private val cuurentMusic =  Music(
                id = 1001,
                title = "Bohemian Rhapsody",
                artist = "Queen",
                album = "A Night at the Opera",
                albumId = 5001,
                duration = 354000,
                imagePath = null,
                uri = "content://sample/1",
                addDate = Clock.System.now().toEpochMilliseconds(),
            )

            private val _state = MutableStateFlow(
            MusicListState(
                music = Resources.Success(listOf(
                    Music(
                        id = 1001,
                        title = "Bohemian Rhapsody",
                        artist = "Queen",
                        album = "A Night at the Opera",
                        albumId = 5001,
                        duration = 354000,
                        imagePath = null,
                        uri = "content://sample/1",
                        addDate = Clock.System.now().toEpochMilliseconds(),
                        isFavorite = true
                    ),
                    Music(
                        id = 1002,
                        title = "Shape of You",
                        artist = "Ed Sheeran",
                        album = "÷ (Divide)",
                        albumId = 5002,
                        duration = 233000,
                        imagePath = null,
                        uri = "content://sample/2",
                        addDate = Clock.System.now().toEpochMilliseconds(),
                        isFavorite = false
                    ),
                    Music(
                        id = 1003,
                        title = "Blinding Lights",
                        artist = "The Weeknd",
                        album = "After Hours",
                        albumId = 5003,
                        duration = 200000,
                        imagePath = null,
                        uri = "content://sample/3",
                        addDate = Clock.System.now().toEpochMilliseconds(),
                        isFavorite = true
                    ),
                    Music(
                        id = 1004,
                        title = "Someone Like You",
                        artist = "Adele",
                        album = "21",
                        albumId = 5004,
                        duration = 285000,
                        imagePath = null,
                        uri = "content://sample/4",
                        addDate = Clock.System.now().toEpochMilliseconds(),
                        isFavorite = false
                    ),
                    Music(
                        id = 1005,
                        title = "Rolling in the Deep",
                        artist = "Adele",
                        album = "21",
                        albumId = 5004,
                        duration = 228000,
                        imagePath = null,
                        uri = "content://sample/5",
                        addDate = Clock.System.now().toEpochMilliseconds(),
                        isFavorite = false
                    )
                ))
            ))

            private val _playbackState = MutableStateFlow(
                PlaybackStates(
                        isPlaying = true,
                isPaused = false,
                isLoading = false,
                currentPosition = 125000L, // 2:05
                duration = 354000L, // 5:54
                currentMusic = cuurentMusic,
                currentIndex = 0,
                playlist = emptyList(),
                playingMode = PlayingMode.REPEAT_ALL,
                playerState = PlayerState.READY,
                error = null
                ),
            )

            override val state: StateFlow<MusicListState>
                get() = _state
            override val playbackState: StateFlow<PlaybackStates>
                get() = _playbackState

            override fun onHandleIntent(intent: MusicListPageIntent.Intent) {}

        }
    }

    LocalProvider(
        controller = controller
    ){
        MusicListScreen(
            onMusicSelected = {},
            onMenuClick = {}
        )
    }
}