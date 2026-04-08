package com.v.music.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v.music.domain.model.FavoriteToggle
import com.v.music.domain.model.Music
import com.v.music.domain.model.PlaybackStates
import com.v.music.domain.model.PlayerState
import com.v.music.domain.model.PlayingMode
import kotlin.time.Clock

@Composable
fun MusicController(
    modifier: Modifier = Modifier,
    music: Music,
    favoriteToggleState: FavoriteToggle,
    playbackState: PlaybackStates,
    playingMode: PlayingMode,
    onFavoriteClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onMoreClick: () -> Unit
){
    val favoriteIcon = if (favoriteToggleState == FavoriteToggle.FAVORITE)
        Icons.Default.Favorite
    else Icons.Default.FavoriteBorder

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {

                Column(modifier = Modifier.weight(0.85f)) {
                    Text(
                        text = playbackState.currentMusic?.title ?: music.title,
                        color = Color.White,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(end = 8.dp)
                    )
                }

                // Favorite
                IconButton(
                    modifier = Modifier
                        .weight(0.15f)
                        .padding(start = 8.dp),
                    onClick = {
                        onFavoriteClick()
                    }
                ) {
                    Icon(
                        imageVector = favoriteIcon,
                        tint  = if (favoriteToggleState == FavoriteToggle.FAVORITE) Color.Green else Color.White,
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                    )
                }
            }

            // artist name
            Text(
                text = playbackState.currentMusic?.artist ?: music.artist,
                color = Color.LightGray,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
            )

            // Custom progress bar
            CustomProgressBar(
                modifier = Modifier.padding(horizontal = 8.dp),
                currentPosition = playbackState.currentPosition,
                duration = playbackState.duration,
                onSeekTo = { position -> onSeekTo(position) },
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                height = 36.dp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // repeat
                IconButton(
                    onClick = onRepeatClick,
                    modifier = Modifier.size(56.dp)
                ) {
                    val icon = when (playingMode) {
                        PlayingMode.REPEAT_ALL -> Icons.Default.Repeat
                        PlayingMode.REPEAT_ONE -> Icons.Default.RepeatOne
                        PlayingMode.SHUFFLE -> Icons.Default.Shuffle
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(32.dp),
                    )
                }

                // previous
                IconButton(
                    onClick = onPreviousClick,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "previous",
                        tint = Color.White,
                        modifier = Modifier
                            .size(32.dp),
                    )
                }

                // play/pause
                IconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = if (!playbackState.isPlaying)
                            Icons.Default.Pause
                        else Icons.Default.PlayCircle,
                        contentDescription = if (playbackState.isPlaying) "pause" else "play",
                        tint = Color.White,
                        modifier = Modifier
                            .size(48.dp),
                    )
                }

                // next
                IconButton(
                    onClick = onNextClick,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "next",
                        tint = Color.White,
                        modifier = Modifier
                            .size(32.dp),
                    )
                }

                // more
                IconButton(
                    onClick = onMoreClick,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(32.dp)
                    )
                }

            }
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun MusicControllerPreview() {
    // Sample music data
    val sampleMusic = Music(
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
    )

    // Sample playback state
    val playbackState = PlaybackStates(
        isPlaying = true,
        isPaused = false,
        isLoading = false,
        currentPosition = 125000L, // 2:05
        duration = 354000L, // 5:54
        currentMusic = sampleMusic,
        currentIndex = 0,
        playlist = listOf(sampleMusic),
        playingMode = PlayingMode.REPEAT_ALL,
        playerState = PlayerState.BUFFERING,
        error = null
    )

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF360033),
                            Color(0xFF2a0845)
                        )
                    )
                )
        ) {
            MusicController(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                music = sampleMusic,
                favoriteToggleState = FavoriteToggle.FAVORITE,
                playbackState = playbackState,
                playingMode = PlayingMode.REPEAT_ALL,
                onFavoriteClick = { println("Favorite clicked") },
                onSeekTo = { position -> println("Seek to: $position") },
                onPreviousClick = { println("Previous clicked") },
                onNextClick = { println("Next clicked") },
                onPlayPauseClick = { println("Play/Pause clicked") },
                onRepeatClick = { println("Repeat clicked") },
                onMoreClick = { println("More clicked") }
            )
        }
    }
}