package com.v.music.utils

import kotlinx.coroutines.flow.flow

sealed class Resources <T> {
    data class Success<T>(val data: T) : Resources<T>()
    data class Error<T>(val message: String) : Resources<T>()
    class Loading<T> : Resources<T>()
}

fun <T> resourceInvokeCallBack(
    executable: suspend () -> Resources<T>
) = flow {
    val result = executable()
    emit(result)
}