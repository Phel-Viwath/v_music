package com.v.music.presentation.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v.music.domain.model.Music
import com.v.music.utils.TimeFormatUtil.dateFormatter

@Composable
fun InfoDialog(
    isVisible: Boolean,
    music: Music,
    onDismiss: () -> Unit
) {
    if (isVisible){
        AlertDialog(
            onDismissRequest = { onDismiss() },
            confirmButton = {},
            title = {
                Text(text = "Info")
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    InfoItem(label = "Name", value = music.title)
                    InfoItem(label = "Artist", value = music.artist)
                    InfoItem(label = "Album", value = music.album)
                    InfoItem(label = "Duration", value = "${music.duration / 1000 / 60}:${music.duration / 1000 % 60}")
                    InfoItem(label = "Add date", value = dateFormatter(music.addDate))
                    InfoItem(label = "Path", value = music.uri.substringBeforeLast("/"), isLast = true)
                }
            }
        )
    }
}

@Composable
fun InfoItem(label: String, value: String, isLast: Boolean = false){
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(bottom = if (isLast) 0.dp else 16.dp),
        verticalAlignment = Alignment.Top
    ){
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 11.sp,
            modifier = Modifier.widthIn(min = 50.dp)
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 11.sp,
            modifier = Modifier.weight(1f),
            maxLines = if (label == "Path" || label == "Name") 2 else 1,
            overflow = TextOverflow.Ellipsis
        )

    }
}

@Preview
@Composable
private fun InfoDialogPreview(){
    val music = Music(
        id = 1001,
        title = "Bohemian Rhapsody",
        artist = "Queen",
        album = "A Night at the Opera",
        albumId = 5001,
        duration = 354000, // 5 minutes 54 seconds in milliseconds
        imagePath = "/storage/music/album_art/queen_night_at_opera.jpg",
        uri = "content://media/external/audio/media/1001",
        addDate = 1704067200000, // January 1, 2024 00:00:00 UTC
        isFavorite = true
    )
    InfoDialog(
        isVisible = true,
        music = music,
        onDismiss = {}
    )
}
