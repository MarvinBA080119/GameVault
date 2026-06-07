package com.example.gamevault.core.repositories

import com.example.gamevault.core.ResponseService
import com.example.gamevault.core.model.FavoriteGame

interface FavoritesService {
    suspend fun addFavorite(favorite: FavoriteGame): ResponseService<Unit>
    suspend fun removeFavorite(gameId: Int): ResponseService<Unit>
    suspend fun isFavorite(gameId: Int): ResponseService<Boolean>
    suspend fun getFavorites(): ResponseService<List<FavoriteGame>>
}