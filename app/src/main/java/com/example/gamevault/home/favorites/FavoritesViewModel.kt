package com.example.gamevault.home.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamevault.core.ResponseService
import com.example.gamevault.core.model.FavoriteGame
import com.example.gamevault.core.repositories.FavoritesRepository
import com.example.gamevault.core.repositories.FavoritesService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val service: FavoritesService = FavoritesRepository()
) : ViewModel() {

    private val _favState = MutableStateFlow<ResponseService<List<FavoriteGame>>?>(null)
    val favState: StateFlow<ResponseService<List<FavoriteGame>>?> = _favState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _favState.value = ResponseService.Loading
            _favState.value = service.getFavorites()
        }
    }

    fun remove(gameId: Int) {
        viewModelScope.launch {
            service.removeFavorite(gameId)
            load() // recarga la lista tras borrar
        }
    }
}