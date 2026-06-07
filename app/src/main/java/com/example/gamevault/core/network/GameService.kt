package com.example.gamevault.core.network

import com.example.gamevault.core.ResponseService
import com.example.gamevault.core.model.Game
import com.example.gamevault.core.model.GameDetail

interface GameService {
    suspend fun listGames(search: String? = null, page: Int = 1): ResponseService<List<Game>>
    suspend fun getGame(id: Int): ResponseService<GameDetail>
}