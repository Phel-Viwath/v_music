package com.v.music.utils

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
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

@Composable
fun <T> ResourceHandler(
    state: Resources<T>,
    onSuccess: @Composable (T) -> Unit,
    onError: @Composable (String) -> Unit,
    onLoading: @Composable () -> Unit = {
        CircularProgressIndicator()
    }
) {
    when (state) {
        is Resources.Loading -> onLoading()
        is Resources.Success -> onSuccess(state.data)
        is Resources.Error -> onError(state.message)
    }
}