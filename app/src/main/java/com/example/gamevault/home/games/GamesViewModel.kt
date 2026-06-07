package com.example.gamevault.home.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamevault.core.ResponseService
import com.example.gamevault.core.model.Game
import com.example.gamevault.core.network.GameService
import com.example.gamevault.core.repositories.GameRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class GamesViewModel(
    private val service: GameService = GameRepository()
) : ViewModel() {

    private val _gameState = MutableStateFlow<ResponseService<List<Game>>?>(null)
    val gameState: StateFlow<ResponseService<List<Game>>?> = _gameState.asStateFlow()

    private val _query = MutableStateFlow("")

    init {
        // Escucha el texto del buscador: espera 400ms tras dejar de teclear
        viewModelScope.launch {
            _query
                .debounce(400)
                .distinctUntilChanged()
                .collect { q -> load(q) }
        }
    }

    fun onQueryChanged(text: String) {
        _query.value = text.trim()
    }

    fun load(query: String = _query.value) {
        viewModelScope.launch {
            _gameState.value = ResponseService.Loading
            _gameState.value = service.listGames(search = query.ifBlank { null })
        }
    }
}