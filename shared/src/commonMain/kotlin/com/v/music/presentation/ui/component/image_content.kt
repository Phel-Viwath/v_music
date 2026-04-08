package com.v.music.presentation.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.v.music.domain.model.Music
import v_music.shared.generated.resources.Res
import v_music.shared.generated.resources.compose_multiplatform
import kotlin.time.Clock

@Composable
fun ImageContent(
    modifier: Modifier = Modifier,
    music: Music
){
    Box(
        modifier = modifier
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ){
        Image(
            painter = rememberAsyncImagePainter(music.imagePath ?: Res.drawable.compose_multiplatform),
            contentDescription = "${music.id}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(
                    BorderStroke(1.dp, Color.Transparent),
                    shape = RoundedCornerShape(8.dp)
                )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ImageContentPreview(){
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
    ImageContent(
        music = sampleMusic,
    )
}