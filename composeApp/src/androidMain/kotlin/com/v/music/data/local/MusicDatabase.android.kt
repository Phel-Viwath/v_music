package com.v.music.data.local

import androidx.room3.RoomDatabaseConstructor

@Suppress("NO_ACTUAL_CLASS_MEMBER_FOR_EXPECTED_CLASS")
actual object MusicDatabaseConstructor : RoomDatabaseConstructor<MusicDatabase> {
    actual override fun initialize(): MusicDatabase = TODO("Provided by Room")
}