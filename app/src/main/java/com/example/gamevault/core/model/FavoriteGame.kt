package com.example.gamevault.core.model

data class FavoriteGame(
    val id: Int = 0,
    val name: String = "",
    val backgroundImage: String? = null,
    val rating: Double = 0.0,
    val genres: String = "",
    val addedAt: Long = 0L
)