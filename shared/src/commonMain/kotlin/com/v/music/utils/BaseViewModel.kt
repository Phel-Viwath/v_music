package com.v.music.utils

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class BaseViewModel <M : Any> : ViewModel() {

    abstract fun initialState(): M

    val state: StateFlow<M>
        field: MutableStateFlow<M> = MutableStateFlow(initialState())

    protected fun setState(reducer: M.() -> M){
        state.update { it.reducer() }
    }

}