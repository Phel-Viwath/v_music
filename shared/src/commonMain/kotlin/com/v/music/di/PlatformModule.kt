package com.v.music.di

import com.v.music.data.local.getFavoriteDao
import com.v.music.data.local.getMusicDatabase
import com.v.music.data.local.getPlaylistDao
import com.v.music.data.repository.MusicRepositoryImp
import com.v.music.di.UseCaseModule.provideAlbumUseCase
import com.v.music.di.UseCaseModule.provideFavorsUseCase
import com.v.music.di.UseCaseModule.provideMusicUseCaseModule
import com.v.music.domain.repository.MusicRepository
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module

expect fun platformModule() : Module

fun initKoin(config: KoinAppDeclaration? = null): KoinApplication {
    return startKoin {
        config?.invoke(this)
        modules(
            listOf(
                platformModule(),
                provideDatabaseModule,
                provideMusicRepositoryModule,
                provideMusicUseCaseModule,
                provideAlbumUseCase,
                provideFavorsUseCase
            )
        )
    }
}

val provideMusicRepositoryModule = module {
    singleOf(::MusicRepositoryImp).bind(MusicRepository::class)
}

val provideDatabaseModule = module {
    single { getMusicDatabase(get()) }
    single { getFavoriteDao(get()) }
    single { getPlaylistDao(get()) }
}
