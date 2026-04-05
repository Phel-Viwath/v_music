package com.v.music.domain.use_case.favorite_use_case

data class FavoriteUseCase (
    val addFavorUseCase: AddFavorUseCase,
    val getFavorByIdUseCase: GetFavorByIdUseCase,
    val getFavorsUseCase: GetFavorsUseCase,
    val removeFavorUseCase: RemoveFavorUseCase
)