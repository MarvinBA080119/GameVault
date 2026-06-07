package com.example.gamevault.core.repositories

import com.example.gamevault.core.ResponseService
import com.example.gamevault.core.model.Game
import com.example.gamevault.core.model.GameDetail
import com.example.gamevault.core.network.ApiClient
import com.example.gamevault.core.network.GameService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GameRepository : GameService {
    private val api = ApiClient.rawgApi

    override suspend fun listGames(search: String?, page: Int): ResponseService<List<Game>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.listGames(
                    key = ApiClient.API_KEY,
                    page = page,
                    pageSize = 20,
                    search = search,
                    // si hay búsqueda dejamos el orden por relevancia; si no, los más añadidos
                    ordering = if (search.isNullOrBlank()) "-added" else null
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) ResponseService.Success(body.results)
                    else ResponseService.Error("Respuesta vacía del servidor")
                } else {
                    ResponseService.Error(messageFor(response.code()))
                }
            } catch (e: Exception) {
                ResponseService.Error("Sin conexión. Revisa tu internet")
            }
        }

    override suspend fun getGame(id: Int): ResponseService<GameDetail> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getGame(id = id, key = ApiClient.API_KEY)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) ResponseService.Success(body)
                    else ResponseService.Error("No se encontró el juego")
                } else {
                    ResponseService.Error(messageFor(response.code()))
                }
            } catch (e: Exception) {
                ResponseService.Error("Sin conexión. Revisa tu internet")
            }
        }

    private fun messageFor(code: Int): String = when (code) {
        401 -> "API key inválida"
        404 -> "Recurso no encontrado"
        429 -> "Demasiadas peticiones, intenta más tarde"
        else -> "Error del servidor ($code)"
    }
}