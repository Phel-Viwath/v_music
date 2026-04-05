package com.v.music.di

import androidx.room3.RoomDatabase
import com.v.music.data.local.AndroidMediaManager
import com.v.music.data.local.MusicDatabase
import com.v.music.data.local.PlatformMediaManager
import com.v.music.data.local.getDatabaseBuilder
import com.v.music.domain.service.AndroidMusicPlayer
import com.v.music.domain.service.MusicPlayer
import com.v.music.domain.service.MusicServiceConnection
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual fun platformModule() = module {

    /// inject database builder
    single<RoomDatabase.Builder<MusicDatabase>> {
        getDatabaseBuilder(androidContext())
    }

    // inject media manager
    single<PlatformMediaManager> {
        AndroidMediaManager(androidContext())
    }

    single { AndroidMusicPlayer(androidContext()) }
    single<MusicPlayer> { get<AndroidMusicPlayer>() }

    single { MusicServiceConnection(androidContext()) }
}
