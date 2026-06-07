package com.example.gamevault.core.repositories

import com.example.gamevault.core.ResponseService
import com.example.gamevault.core.model.FavoriteGame
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FavoritesRepository : FavoritesService {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // users/{uid}/favorites
    private fun favoritesCollection() =
        firestore.collection("users")
            .document(auth.currentUser?.uid ?: "anon")
            .collection("favorites")

    override suspend fun addFavorite(favorite: FavoriteGame): ResponseService<Unit> =
        withContext(Dispatchers.IO) {
            try {
                favoritesCollection()
                    .document(favorite.id.toString())
                    .set(favorite.copy(addedAt = System.currentTimeMillis()))
                    .await()
                ResponseService.Success(Unit)
            } catch (e: Exception) {
                ResponseService.Error("No se pudo guardar en favoritos")
            }
        }

    override suspend fun removeFavorite(gameId: Int): ResponseService<Unit> =
        withContext(Dispatchers.IO) {
            try {
                favoritesCollection().document(gameId.toString()).delete().await()
                ResponseService.Success(Unit)
            } catch (e: Exception) {
                ResponseService.Error("No se pudo quitar de favoritos")
            }
        }

    override suspend fun isFavorite(gameId: Int): ResponseService<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val doc = favoritesCollection().document(gameId.toString()).get().await()
                ResponseService.Success(doc.exists())
            } catch (e: Exception) {
                ResponseService.Error("No se pudo verificar favoritos")
            }
        }

    override suspend fun getFavorites(): ResponseService<List<FavoriteGame>> =
        withContext(Dispatchers.IO) {
            try {
                val snapshot = favoritesCollection()
                    .orderBy("addedAt", Query.Direction.DESCENDING)
                    .get()
                    .await()
                val list = snapshot.toObjects(FavoriteGame::class.java)
                ResponseService.Success(list)
            } catch (e: Exception) {
                ResponseService.Error("No se pudieron cargar tus favoritos")
            }
        }
}