package com.v.music.presentation.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.v.music.domain.model.Music
import kotlin.time.Clock

@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    currentMusic: Music,
    duration: Long,
    currentPosition: Long,
    onTap: () -> Unit = {},
    onResumeClick: () -> Unit,
    onPauseClick: () -> Unit,
    onPlayNextClick: () -> Unit,
    onSeekTo: (Long) -> Unit
){
    Card(
        modifier = modifier.fillMaxWidth().clickable { onTap() },
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent, // Semi-transparent for floating effect
            contentColor = MaterialTheme.colorScheme.surface
        ),
        elevation = cardElevation(
            defaultElevation = 16.dp
        ),
        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
    ) {

        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Color.White)
//                .background(
//                    Brush.linearGradient(
//                    listOf(
//                        Color(0xFF360033),
//                        Color(0xFF2a0845)
//                    )
//                ))
        ){
            Column(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, top = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // row detail
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(52.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ){
                        currentMusic.imagePath?.let { path ->
                            Image(
                                painter = rememberAsyncImagePainter(path),
                                contentDescription = "Album Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = currentMusic.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = currentMusic.artist,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (isPlaying) {
                                onPauseClick()
                            } else {
                                onResumeClick()
                            }
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(onClick = onPlayNextClick){
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Play next",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                }

                // progress
                CustomProgressBar(
                    modifier = Modifier.height(4.dp),
                    currentPosition = currentPosition,
                    duration = duration,
                    onSeekTo = { position -> onSeekTo(position) },
                    thumbColor = Color.Transparent,
                    activeTrackColor = Color(0xFF8B5CF6),
                    height = 4.dp
                )
            }

        }
    }
}


@Preview(showBackground = true)
@Composable
fun MiniPlayerPreview() {
    // Sample music data for preview
    val sampleMusic = Music(
        id = 1001,
        title = "Bohemian Rhapsody",
        artist = "Queen",
        album = "A Night at the Opera",
        albumId = 5001,
        duration = 354000, // 5:54
        imagePath = null, // Use null for preview, or provide a test URL
        uri = "content://sample",
        addDate = Clock.System.now().toEpochMilliseconds(),
        isFavorite = true
    )

    // Wrapper to provide proper theme context
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(16.dp)
        ) {
            MiniPlayer(
                modifier = Modifier,
                isPlaying = true,
                currentMusic = sampleMusic,
                duration = 354000,
                currentPosition = 125000, // 2:05
                onTap = { /* Handle tap */ },
                onResumeClick = { /* Handle resume */ },
                onPauseClick = { /* Handle pause */ },
                onPlayNextClick = { /* Handle next */ },
                onSeekTo = { position -> /* Handle seek */ }
            )
        }
    }
}
