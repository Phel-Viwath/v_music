package com.v.music.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v.music.utils.TimeFormatUtil.formatTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomProgressBar(
    modifier: Modifier = Modifier,
    currentPosition: Long,
    duration: Long,
    onSeekTo: (Long) -> Unit,
    thumbColor: Color,
    activeTrackColor: Color,
    height: Dp
){
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    var isUserSeeking by remember { mutableStateOf(false) }

    val progress = if (duration > 0) {
        currentPosition.toFloat() / duration
    } else 0f

    LaunchedEffect(currentPosition, duration, isUserSeeking) {
        if (!isUserSeeking && duration > 0) {
            sliderPosition = progress
        }
    }

    Column(modifier = modifier) {
        // progress bar
        Slider(
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            value = sliderPosition,
            onValueChange = {
                sliderPosition = it
                isUserSeeking = true
            },
            onValueChangeFinished = {
                onSeekTo((sliderPosition * duration).toLong())
                isUserSeeking = false
            },
            colors = SliderDefaults.colors(
                thumbColor = thumbColor,
                activeTrackColor = activeTrackColor,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f),
            ),
            valueRange = 0f..1f,
            thumb = {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .shadow(4.dp, CircleShape)
                        .background(thumbColor, CircleShape)
                )
            },
            track = {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f))
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(sliderPosition)
                            .fillMaxHeight()
                            .background(activeTrackColor)
                    )
                }
            }
        )

        // time label
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val shownPosition = if (isUserSeeking) (sliderPosition * duration).toLong() else currentPosition

            Text(
                text = shownPosition.formatTime(),
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = duration.formatTime(),
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }

}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun CustomProgressBarPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .padding(16.dp)
        ) {
            CustomProgressBar(
                modifier = Modifier.fillMaxWidth(),
                currentPosition = 125000L, // 2 minutes 5 seconds
                duration = 354000L, // 5 minutes 54 seconds
                onSeekTo = { position ->
                    // In preview, just log or do nothing
                    println("Seek to: $position")
                },
                thumbColor = Color(0xFF8B5CF6),
                activeTrackColor = Color(0xFF8B5CF6),
                height = 4.dp
            )
        }
    }
}