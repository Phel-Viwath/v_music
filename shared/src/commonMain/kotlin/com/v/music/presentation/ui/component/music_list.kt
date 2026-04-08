package com.v.music.presentation.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.v.music.domain.model.Music
import v_music.shared.generated.resources.Res
import v_music.shared.generated.resources.compose_multiplatform
import v_music.shared.generated.resources.v_music_icon
import kotlin.time.Clock

@Composable
fun MusicList(
    modifier: Modifier = Modifier,
    musicList: List<Music>,
    currentMusic: Music?,
    isPaused: Boolean,
    onMusicSelected: (Music) -> Unit,
    onMenuClick: (Music) -> Unit
) {

    var selectedMusicForMenu by remember { mutableStateOf<Music?>(null) }

    Box(
        modifier = modifier
    ){
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent),
        ) {
            items(
                items = musicList,
                key = {music -> music.id}
            ) { music ->
                val isPlaying = currentMusic?.id == music.id
                val isPause = if (currentMusic?.id == music.id) isPaused  else false
                MusicListItem(
                    music = music,
                    onItemClick = { selectedMusic ->
                        onMusicSelected(selectedMusic)
                    },
                    onItemMenuClick = onMenuClick,
                    isPlaying = isPlaying,
                    isPaused = isPause
                )
            }
        }
    }

    selectedMusicForMenu?.let { musicDto ->
//        ShowBottomSheetMenu(
//            isVisible = true,
//            musicDto = musicDto,
//        ) {
//            selectedMusicForMenu = null
//        }
    }


}

@Composable
fun MusicListItem(
    music: Music,
    onItemClick: (Music) -> Unit,
    onItemMenuClick: (Music) -> Unit,
    isPlaying: Boolean = false,
    isPaused: Boolean = false
){
    val textColor = if (isPlaying) Color.Green else Color.White
    Box(modifier = Modifier.background(Color.Transparent)){
        Row(
            modifier = Modifier
                .background(Color.Transparent)
                .fillMaxWidth()
                .clickable{ onItemClick(music) }
                .padding(start = 14.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Box {
                Image(
                    painter = rememberAsyncImagePainter(music.imagePath ?: Res.drawable.compose_multiplatform),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            BorderStroke(1.dp, Color.Black),
                            shape = RoundedCornerShape(8.dp)
                        )
                )
                if (isPlaying)
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Play",
                        tint = Color.Black,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .background(Color.Green, shape = CircleShape)
                            .size(12.dp)
                            .padding(2.dp)
                    )
                if (isPaused)
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.Black,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .background(Color.Green, shape = CircleShape)
                            .size(12.dp)
                            .padding(2.dp)
                    )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = music.title,
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = music.artist,
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1
                )
            }

            IconButton(
                modifier = Modifier
                    .wrapContentSize(),
                onClick = { onItemMenuClick(music) },
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MusicListPreview() {
    // Sample music list for preview
    val sampleMusicList = listOf(
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
    )

    // Current playing music (Bohemian Rhapsody is playing)
    val currentPlayingMusic = sampleMusicList[0]

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(16.dp)
        ) {
            MusicList(
                modifier = Modifier.fillMaxSize(),
                musicList = sampleMusicList,
                currentMusic = currentPlayingMusic,
                isPaused = false,  // Currently playing, not paused
                onMusicSelected = { music ->
                    println("Selected music: ${music.title}")
                },
                onMenuClick = { music ->
                    println("Menu clicked for: ${music.title}")
                }
            )
        }
    }
}

