package com.example.gamevault.home.gameDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamevault.core.ResponseService
import com.example.gamevault.core.model.FavoriteGame
import com.example.gamevault.core.model.GameDetail
import com.example.gamevault.core.network.GameService
import com.example.gamevault.core.repositories.FavoritesRepository
import com.example.gamevault.core.repositories.FavoritesService
import com.example.gamevault.core.repositories.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameDetailViewModel(
    private val gameService: GameService = GameRepository(),
    private val favoritesService: FavoritesService = FavoritesRepository()
) : ViewModel() {

    private val _detailState = MutableStateFlow<ResponseService<GameDetail>?>(null)
    val detailState: StateFlow<ResponseService<GameDetail>?> = _detailState.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    // Mensajes puntuales (al agregar / quitar)
    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast.asStateFlow()

    fun load(gameId: Int) {
        viewModelScope.launch {
            _detailState.value = ResponseService.Loading
            val result = gameService.getGame(gameId)
            _detailState.value = result
            // Si cargó bien, revisamos si ya está en favoritos
            if (result is ResponseService.Success) {
                val fav = favoritesService.isFavorite(gameId)
                if (fav is ResponseService.Success) _isFavorite.value = fav.data
            }
        }
    }

    fun toggleFavorite(detail: GameDetail) {
        viewModelScope.launch {
            if (_isFavorite.value) {
                val res = favoritesService.removeFavorite(detail.id)
                if (res is ResponseService.Success) {
                    _isFavorite.value = false
                    _toast.value = "Quitado de tu lista"
                } else if (res is ResponseService.Error) {
                    _toast.value = res.error
                }
            } else {
                val favorite = FavoriteGame(
                    id = detail.id,
                    name = detail.name,
                    backgroundImage = detail.backgroundImage,
                    rating = detail.rating,
                    genres = detail.genres.take(2).joinToString(" · ") { it.name }
                )
                val res = favoritesService.addFavorite(favorite)
                if (res is ResponseService.Success) {
                    _isFavorite.value = true
                    _toast.value = "Agregado a tu lista"
                } else if (res is ResponseService.Error) {
                    _toast.value = res.error
                }
            }
        }
    }

    fun clearToast() { _toast.value = null }
}