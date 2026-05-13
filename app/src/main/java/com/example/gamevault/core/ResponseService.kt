package com.example.gamevault.core

sealed class ResponseService<out T> {
    data class Success<T>(val data: T) : ResponseService<T>()
    data class Error(val error: String) : ResponseService<Nothing>()
    object Loading : ResponseService<Nothing>()
}
