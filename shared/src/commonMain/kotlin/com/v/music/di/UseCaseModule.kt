package com.v.music.di

import com.v.music.domain.use_case.album_use_case.AlbumUseCase
import com.v.music.domain.use_case.album_use_case.GetAlbumMusicUseCase
import com.v.music.domain.use_case.album_use_case.GetAlbumsUseCase
import com.v.music.domain.use_case.favorite_use_case.AddFavorUseCase
import com.v.music.domain.use_case.favorite_use_case.FavoriteUseCase
import com.v.music.domain.use_case.favorite_use_case.GetFavorByIdUseCase
import com.v.music.domain.use_case.favorite_use_case.GetFavorsUseCase
import com.v.music.domain.use_case.favorite_use_case.RemoveFavorUseCase
import com.v.music.domain.use_case.music_use_case.DeleteMusicsUseCase
import com.v.music.domain.use_case.music_use_case.GetMusicsUseCase
import com.v.music.domain.use_case.music_use_case.MusicUseCase
import org.koin.dsl.module

object UseCaseModule {

    val provideMusicUseCaseModule = module {
        factory { GetMusicsUseCase(get()) }
        factory { DeleteMusicsUseCase(get()) }
        factory {
            MusicUseCase(get(), get())
        }
    }

    val provideAlbumUseCase = module {
        factory { GetAlbumsUseCase(get()) }
        factory { GetAlbumMusicUseCase(get()) }
        factory {
            AlbumUseCase(get(), get())
        }
    }

    val provideFavorsUseCase = module {
        factory { AddFavorUseCase(get()) }
        factory { GetFavorByIdUseCase(get()) }
        factory { GetFavorsUseCase(get()) }
        factory { RemoveFavorUseCase(get()) }
        factory {
            FavoriteUseCase(
                addFavorUseCase = get(),
                getFavorByIdUseCase = get(),
                getFavorsUseCase = get(),
                removeFavorUseCase = get()
            )
        }
    }

}